# LLD Problems Website

A single-page application that displays all 50 Low-Level Design problems with their solutions, code, and explanations.

## Quick Start

```bash
# 1. Generate data.json from the problem directories
python3 build.py

# 2. Serve the website AND the run server (single Node process)
node server.js

# 3. Open in browser
open http://localhost:8080
```

The Node server replaces `python3 -m http.server` — it serves the static site
**and** exposes `POST /api/run` for compile + execute (used by the in-browser
editor's Run and Tests buttons).

Requirements:
- Node 18+ (no `npm install` — built-in modules only)
- A Java JDK on PATH (`javac`, `java`)

If you only want to browse code and don't need execution, `python3 -m http.server 8080`
still works; Edit/Run/Tests will fail gracefully when the API is unavailable.

## In-browser editor + tests

When viewing a problem's code, three new buttons live in the code header:

- **Edit** — Opens a Monaco editor (LeetCode's editor) with Java syntax highlighting. Edits are kept in memory per file; refresh discards them.
- **Run** — Sends all files of the current variant (with your edits applied) to `POST /api/run`. Server compiles in a temp dir, runs `Main`, returns stdout/stderr/exit code.
- **Tests** — Loads `tests/<problem-id>.json` and runs each case as its own JVM. Each case supplies its own `Main.java` driver and asserts on stdout/exit code. Currently defined for `01-parking-lot`; add more by dropping JSON files in `tests/`.

## Features

- Browse all 50 LLD problems with search and filtering
- Filter by difficulty (Easy/Medium/Hard) or design pattern
- Tabbed code viewer with syntax highlighting (highlight.js)
- Markdown rendering of problem statements (marked.js)
- Dark/light theme toggle
- Progress tracker (mark problems as studied, saved in localStorage)
- Responsive design (works on mobile)
- Copy code button for each source file
- Deep linking via URL hash (e.g., `#01-parking-lot`)

## Files

| File | Purpose |
|------|---------|
| `build.py` | Scans `problems/` directory, generates `data.json` |
| `index.html` | Main HTML page |
| `style.css` | Styling (dark sidebar, light content, responsive) |
| `app.js` | Application logic (SPA, filtering, tabs, theme) |
| `data.json` | Generated data file (not checked in) |

## Rebuilding

Run `python3 build.py` whenever problem content changes. The script:
- Reads all `problems/XX-name/` directories
- Extracts README.md, VARIATIONS.md (if present), and all .java files
- Outputs `data.json` with structured problem data

## Deployment to GitHub Pages

1. Run `python3 build.py` to generate `data.json`
2. Commit the `website/` directory (including `data.json`)
3. In GitHub repo settings, set Pages source to the `website/` folder
4. The site will be available at `https://<user>.github.io/<repo>/website/`

Alternatively, use a GitHub Action to run `build.py` on each push.

## Dependencies (CDN)

- [highlight.js](https://highlightjs.org/) - Code syntax highlighting
- [marked.js](https://marked.js.org/) - Markdown rendering

No npm install or build tools required.

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
