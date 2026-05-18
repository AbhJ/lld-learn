/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
//
// Local dev server: serves the static site AND handles POST /api/run
// for compile + execute of a Java program.
//
// Run:  node server.js   (requires Node 18+ and a Java JDK on PATH)
// Then: open http://localhost:8080/
//
// Pure built-in modules only — no npm install required.

'use strict';

const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');
const { spawn } = require('child_process');
const { randomBytes } = require('crypto');

const PORT = parseInt(process.env.PORT || '8080', 10);
const STATIC_ROOT = __dirname;
const RUN_TIMEOUT_MS = 8000;
const COMPILE_TIMEOUT_MS = 15000;
const MAX_BODY_BYTES = 5 * 1024 * 1024; // 5 MB

// ---------------------------------------------------------------------------
// Utility: run a command with a timeout, capture stdout/stderr.
// ---------------------------------------------------------------------------
function runCmd(cmd, args, opts = {}) {
    return new Promise((resolve) => {
        const child = spawn(cmd, args, { cwd: opts.cwd, env: process.env });
        let stdout = '';
        let stderr = '';
        let timedOut = false;
        const timer = setTimeout(() => {
            timedOut = true;
            child.kill('SIGKILL');
        }, opts.timeout || RUN_TIMEOUT_MS);

        child.stdout.on('data', (chunk) => { stdout += chunk.toString('utf8'); });
        child.stderr.on('data', (chunk) => { stderr += chunk.toString('utf8'); });

        if (opts.stdin) {
            child.stdin.write(opts.stdin);
            child.stdin.end();
        } else {
            child.stdin.end();
        }

        child.on('close', (code) => {
            clearTimeout(timer);
            resolve({
                code: code === null ? -1 : code,
                stdout,
                stderr,
                timedOut,
            });
        });
        child.on('error', (err) => {
            clearTimeout(timer);
            resolve({ code: -1, stdout, stderr: stderr + '\n' + err.message, timedOut });
        });
    });
}

// ---------------------------------------------------------------------------
// Walk a directory, return all .java file paths.
// ---------------------------------------------------------------------------
function walkJavaFiles(dir) {
    const out = [];
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
        const full = path.join(dir, entry.name);
        if (entry.isDirectory()) {
            out.push(...walkJavaFiles(full));
        } else if (entry.name.endsWith('.java')) {
            out.push(full);
        }
    }
    return out;
}

// ---------------------------------------------------------------------------
// Static file serving for the website.
// ---------------------------------------------------------------------------
const MIME = {
    '.html': 'text/html; charset=utf-8',
    '.js':   'application/javascript; charset=utf-8',
    '.css':  'text/css; charset=utf-8',
    '.json': 'application/json; charset=utf-8',
    '.svg':  'image/svg+xml',
    '.png':  'image/png',
    '.jpg':  'image/jpeg',
    '.ico':  'image/x-icon',
    '.txt':  'text/plain; charset=utf-8',
    '.md':   'text/markdown; charset=utf-8',
};

function serveStatic(req, res) {
    let urlPath = req.url.split('?')[0];
    if (urlPath === '/') urlPath = '/index.html';
    // Prevent directory traversal
    const safePath = path.normalize(urlPath).replace(/^(\.\.[\/\\])+/, '');
    const filePath = path.join(STATIC_ROOT, safePath);
    if (!filePath.startsWith(STATIC_ROOT)) {
        res.writeHead(403); res.end('Forbidden'); return;
    }
    fs.stat(filePath, (err, stat) => {
        if (err || !stat.isFile()) {
            res.writeHead(404); res.end('Not found'); return;
        }
        const ext = path.extname(filePath).toLowerCase();
        res.writeHead(200, {
            'Content-Type': MIME[ext] || 'application/octet-stream',
            'Cache-Control': 'no-cache', // dev mode
        });
        fs.createReadStream(filePath).pipe(res);
    });
}

// ---------------------------------------------------------------------------
// POST /api/run — compile + run a set of Java files, return result.
//
// Request body: {
//   files: [ { name: "service/Foo.java", content: "..." }, ... ],
//   mainClass: "Main",
//   stdin: "",
// }
// Response: {
//   stage: "compile" | "run",
//   exitCode: number,
//   stdout: string,
//   stderr: string,
//   timedOut: boolean,
//   compileMs: number,
//   runMs: number,
// }
// ---------------------------------------------------------------------------
async function handleRun(req, res) {
    let body = '';
    let bytes = 0;
    let aborted = false;
    req.on('data', (chunk) => {
        bytes += chunk.length;
        if (bytes > MAX_BODY_BYTES) {
            aborted = true;
            res.writeHead(413); res.end('Payload too large');
            req.destroy();
            return;
        }
        body += chunk.toString('utf8');
    });
    req.on('end', async () => {
        if (aborted) return;
        let payload;
        try { payload = JSON.parse(body); }
        catch { res.writeHead(400); res.end('Invalid JSON'); return; }

        const files = Array.isArray(payload.files) ? payload.files : [];
        const mainClass = String(payload.mainClass || 'Main');
        const stdin = String(payload.stdin || '');
        if (!files.length) { res.writeHead(400); res.end('No files'); return; }

        // Make a temp dir for this run
        const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'lld-run-' + randomBytes(4).toString('hex') + '-'));
        const result = {
            stage: 'compile',
            exitCode: 0,
            stdout: '',
            stderr: '',
            timedOut: false,
            compileMs: 0,
            runMs: 0,
        };
        try {
            // Write files into tmp dir
            for (const f of files) {
                if (typeof f.name !== 'string' || typeof f.content !== 'string') continue;
                // Sanitize: no parent traversal, no absolute paths
                const safe = f.name.replace(/^[/\\]/, '').replace(/\.\.+/g, '_');
                const filePath = path.join(tmpDir, safe);
                if (!filePath.startsWith(tmpDir)) continue;
                fs.mkdirSync(path.dirname(filePath), { recursive: true });
                fs.writeFileSync(filePath, f.content);
            }

            const javaFiles = walkJavaFiles(tmpDir);
            if (!javaFiles.length) {
                result.stderr = 'No .java files found after writing payload.';
                result.exitCode = -1;
                sendJson(res, 400, result);
                return;
            }

            // Compile
            const compileStart = Date.now();
            const classDir = path.join(tmpDir, 'classes');
            fs.mkdirSync(classDir);
            const compile = await runCmd('javac', ['-d', classDir, ...javaFiles], {
                cwd: tmpDir,
                timeout: COMPILE_TIMEOUT_MS,
            });
            result.compileMs = Date.now() - compileStart;
            if (compile.timedOut) {
                result.timedOut = true;
                result.exitCode = compile.code;
                result.stderr = compile.stderr + '\n[server] compile timed out';
                sendJson(res, 200, result);
                return;
            }
            if (compile.code !== 0) {
                result.exitCode = compile.code;
                result.stdout = compile.stdout;
                result.stderr = compile.stderr;
                sendJson(res, 200, result);
                return;
            }

            // Run
            result.stage = 'run';
            const runStart = Date.now();
            const run = await runCmd('java', ['-cp', classDir, mainClass], {
                cwd: tmpDir,
                timeout: RUN_TIMEOUT_MS,
                stdin,
            });
            result.runMs = Date.now() - runStart;
            result.exitCode = run.code;
            result.stdout = run.stdout;
            result.stderr = run.stderr;
            result.timedOut = run.timedOut;
            sendJson(res, 200, result);
        } catch (e) {
            result.stderr = (result.stderr || '') + '\n[server] ' + e.message;
            result.exitCode = -1;
            sendJson(res, 500, result);
        } finally {
            // Cleanup
            fs.rm(tmpDir, { recursive: true, force: true }, () => {});
        }
    });
}

function sendJson(res, status, obj) {
    const body = JSON.stringify(obj);
    res.writeHead(status, {
        'Content-Type': 'application/json; charset=utf-8',
        'Content-Length': Buffer.byteLength(body),
    });
    res.end(body);
}

// ---------------------------------------------------------------------------
// Main request router
// ---------------------------------------------------------------------------
const server = http.createServer((req, res) => {
    if (req.method === 'POST' && req.url === '/api/run') {
        handleRun(req, res).catch((e) => {
            res.writeHead(500); res.end('Server error: ' + e.message);
        });
        return;
    }
    if (req.method === 'GET') {
        serveStatic(req, res);
        return;
    }
    res.writeHead(405); res.end('Method not allowed');
});

server.listen(PORT, () => {
    console.log(`LLD site + run server: http://localhost:${PORT}/`);
    console.log(`POST /api/run for compile + execute.`);
});
