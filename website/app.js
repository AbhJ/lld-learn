/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */

/**
 * LLD Problems — Low Level Design in Java
 * Single Page Application (Navy + Brass theme)
 */

(function () {
    'use strict';

    // =========================================================================
    // State
    // =========================================================================
    let problems = [];
    let filteredProblems = [];
    let allPatterns = [];
    let completed = new Set();
    let currentProblemId = null;
    let currentFilePath = null;
    let expandedProblemId = null; // sidebar tree expansion (independent of main-view selection)
    let activeFilters = { difficulty: '', pattern: '', search: '' };
    let searchTimeout = null;

    // =========================================================================
    // DOM References
    // =========================================================================
    const $ = (sel) => document.querySelector(sel);
    const $$ = (sel) => document.querySelectorAll(sel);

    const dom = {
        sidebar: $('#sidebar'),
        sidebarOverlay: $('#sidebar-overlay'),
        hamburger: $('#mobile-hamburger'),
        searchInput: $('#search-input'),
        difficultyChips: $('#difficulty-chips'),
        patternChips: $('#pattern-chips'),
        progressFill: $('#progress-fill'),
        progressText: $('#progress-text'),
        folderTree: $('#folder-tree'),
        mainContent: $('#main-content'),
        viewWelcome: $('#view-welcome'),
        viewProblem: $('#view-problem'),
        viewCode: $('#view-code'),
        welcomeStats: $('#welcome-stats'),
        problemTitle: $('#problem-title'),
        problemMeta: $('#problem-meta'),
        problemBody: $('#problem-body'),
        btnProgress: $('#btn-progress'),
        codeBreadcrumb: $('#code-breadcrumb'),
        codeContent: $('#code-content'),
        btnCopy: $('#btn-copy'),
        btnEdit: $('#btn-edit'),
        btnRun: $('#btn-run'),
        btnTests: $('#btn-tests'),
        codeEditor: $('#code-editor'),
        runPanel: $('#run-panel'),
        runPanelTitle: $('#run-panel-title'),
        runPanelBody: $('#run-panel-body'),
        runPanelClose: $('#run-panel-close'),
    };

    // Monaco editor instance & per-file edit buffers
    let monacoLoaded = false;
    let monacoEditor = null;
    let editMode = false;
    // Map: "<problemId>::<filePath>" -> edited content (preserved across navigation)
    const editBuffers = new Map();
    const bufferKey = (pid, fp) => pid + '::' + fp;

    // =========================================================================
    // Initialize
    // =========================================================================
    async function init() {
        loadCompleted();
        await loadData();
        collectPatterns();
        renderPatternChips();
        renderFolderTree();
        renderWelcomeStats();
        updateProgress();
        setupListeners();
        handleRoute();
    }

    // =========================================================================
    // Data Loading
    // =========================================================================
    async function loadData() {
        try {
            const resp = await fetch('data.json');
            const data = await resp.json();
            problems = data.problems || [];
            filteredProblems = [...problems];
        } catch (err) {
            console.error('Failed to load data.json:', err);
            problems = [];
            filteredProblems = [];
        }
    }

    function collectPatterns() {
        const patternSet = new Set();
        problems.forEach(p => p.patterns.forEach(pat => patternSet.add(pat)));
        allPatterns = [...patternSet].sort();
    }

    // =========================================================================
    // LocalStorage — Progress
    // =========================================================================
    function loadCompleted() {
        try {
            const stored = localStorage.getItem('lld-completed');
            if (stored) {
                completed = new Set(JSON.parse(stored));
            }
        } catch (e) { /* ignore */ }
    }

    function saveCompleted() {
        localStorage.setItem('lld-completed', JSON.stringify([...completed]));
    }

    function toggleCompleted(problemId) {
        if (completed.has(problemId)) {
            completed.delete(problemId);
        } else {
            completed.add(problemId);
        }
        saveCompleted();
        updateProgress();
        updateTreeCheckmarks();
        updateProblemProgressBtn();
    }

    // =========================================================================
    // Filtering
    // =========================================================================
    function applyFilters() {
        const { difficulty, pattern, search } = activeFilters;
        const q = search.toLowerCase().trim();

        filteredProblems = problems.filter(p => {
            if (difficulty && p.difficulty !== difficulty) return false;
            if (pattern && !p.patterns.includes(pattern)) return false;
            if (q) {
                const haystack = (p.title + ' ' + p.id + ' ' + p.patterns.join(' ')).toLowerCase();
                if (!haystack.includes(q)) return false;
            }
            return true;
        });

        renderFolderTree();
    }

    // =========================================================================
    // Render: Welcome Stats
    // =========================================================================
    function renderWelcomeStats() {
        const totalFiles = problems.reduce((sum, p) => sum + p.files.length, 0);
        dom.welcomeStats.innerHTML = `
            <div class="stat-card">
                <div class="stat-number">${problems.length}</div>
                <div class="stat-label">Problems</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">${allPatterns.length}</div>
                <div class="stat-label">Patterns</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">${totalFiles}</div>
                <div class="stat-label">Java Files</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">${completed.size}</div>
                <div class="stat-label">Completed</div>
            </div>
        `;
    }

    // =========================================================================
    // Render: Progress
    // =========================================================================
    function updateProgress() {
        const total = problems.length || 1;
        const done = completed.size;
        const pct = Math.round((done / total) * 100);
        dom.progressFill.style.width = pct + '%';
        dom.progressText.textContent = `${done} / ${total} completed`;
    }

    // =========================================================================
    // Render: Pattern Chips
    // =========================================================================
    function renderPatternChips() {
        // Show top patterns (most common)
        const patternCounts = {};
        problems.forEach(p => p.patterns.forEach(pat => {
            patternCounts[pat] = (patternCounts[pat] || 0) + 1;
        }));
        const topPatterns = allPatterns
            .sort((a, b) => (patternCounts[b] || 0) - (patternCounts[a] || 0))
            .slice(0, 8);

        dom.patternChips.innerHTML = topPatterns.map(pat =>
            `<button class="chip" data-pattern="${pat}">${pat}</button>`
        ).join('');
    }

    // =========================================================================
    // Render: Folder Tree
    // =========================================================================
    function renderFolderTree() {
        dom.folderTree.innerHTML = filteredProblems.map(p => {
            const isActive = p.id === currentProblemId;
            const isExpanded = p.id === expandedProblemId;
            const isCompleted = completed.has(p.id);
            const diffClass = p.difficulty.toLowerCase();

            return `
                <div class="tree-problem" data-problem-id="${p.id}">
                    <div class="tree-problem-header${isActive ? ' active' : ''}" data-id="${p.id}">
                        <span class="tree-arrow${isExpanded ? ' expanded' : ''}">&#9654;</span>
                        <span class="tree-difficulty ${diffClass}"></span>
                        <span class="tree-problem-name">${p.title}</span>
                        <svg class="tree-check${isCompleted ? ' completed' : ''}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6L9 17l-5-5"></path></svg>
                    </div>
                    <div class="tree-files${isExpanded ? ' open' : ''}" id="tree-files-${p.id}">
                        ${buildFileTree(p)}
                    </div>
                </div>
            `;
        }).join('');

        // Attach event listeners — header click toggles sidebar expansion;
        // expanding also navigates to that problem, collapsing leaves the main view alone.
        dom.folderTree.querySelectorAll('.tree-problem-header').forEach(el => {
            el.addEventListener('click', () => {
                const id = el.dataset.id;
                if (expandedProblemId === id) {
                    collapseProblemInSidebar(id);
                } else {
                    expandedProblemId = id;
                    navigateTo(id);
                }
            });
        });

        dom.folderTree.querySelectorAll('.tree-file').forEach(el => {
            el.addEventListener('click', (e) => {
                e.stopPropagation();
                const problemId = el.dataset.problemId;
                const filePath = el.dataset.file;
                navigateTo(problemId, filePath);
            });
        });

        dom.folderTree.querySelectorAll('.tree-folder-label').forEach(el => {
            el.addEventListener('click', (e) => {
                e.stopPropagation();
                const children = el.nextElementSibling;
                if (children) {
                    children.classList.toggle('open');
                    // Toggle arrow
                    const arrow = el.querySelector('.tree-arrow');
                    if (arrow) arrow.classList.toggle('expanded');
                }
            });
        });
    }

    function buildFileTree(problem) {
        // Build a nested tree from file paths like "naive/model/Vehicle.java"
        const tree = {};

        problem.files.forEach(file => {
            const parts = file.name.split('/');
            let node = tree;
            for (let i = 0; i < parts.length - 1; i++) {
                if (!node[parts[i]]) node[parts[i]] = {};
                node = node[parts[i]];
            }
            // Leaf: store as string marker
            node[parts[parts.length - 1]] = '__FILE__';
        });

        return renderTreeNode(tree, problem.id, '');
    }

    function renderTreeNode(node, problemId, path) {
        let html = '';
        const entries = Object.entries(node).sort((a, b) => {
            // Folders first, then files
            const aIsFolder = a[1] !== '__FILE__';
            const bIsFolder = b[1] !== '__FILE__';
            if (aIsFolder !== bIsFolder) return bIsFolder ? 1 : -1;
            return a[0].localeCompare(b[0]);
        });

        for (const [name, value] of entries) {
            const fullPath = path ? path + '/' + name : name;

            if (value === '__FILE__') {
                const isActive = currentFilePath === fullPath && currentProblemId === problemId;
                html += `
                    <div class="tree-file${isActive ? ' active' : ''}" data-problem-id="${problemId}" data-file="${fullPath}">
                        <svg class="tree-file-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                            <polyline points="14 2 14 8 20 8"></polyline>
                        </svg>
                        <span>${name}</span>
                    </div>
                `;
            } else {
                const isProblemActive = currentProblemId === problemId;
                const hasActiveChild = currentFilePath && currentFilePath.startsWith(fullPath + '/') && isProblemActive;
                const shouldExpand = isProblemActive || hasActiveChild;
                html += `
                    <div class="tree-folder">
                        <div class="tree-folder-label">
                            <span class="tree-arrow${shouldExpand ? ' expanded' : ''}">&#9654;</span>
                            <span>${name}/</span>
                        </div>
                        <div class="tree-folder-children${shouldExpand ? ' open' : ''}">
                            ${renderTreeNode(value, problemId, fullPath)}
                        </div>
                    </div>
                `;
            }
        }
        return html;
    }

    /**
     * Collapse the sidebar tree for a problem without touching the main view.
     * Toggles the .open class so CSS animates the close, and rotates the arrow.
     */
    function collapseProblemInSidebar(problemId) {
        expandedProblemId = null;
        const filesEl = document.getElementById('tree-files-' + problemId);
        if (filesEl) filesEl.classList.remove('open');
        const headerEl = dom.folderTree.querySelector(`.tree-problem-header[data-id="${problemId}"]`);
        if (headerEl) {
            const arrow = headerEl.querySelector('.tree-arrow');
            if (arrow) arrow.classList.remove('expanded');
        }
    }

    function updateTreeCheckmarks() {
        dom.folderTree.querySelectorAll('.tree-check').forEach(el => {
            const header = el.closest('.tree-problem-header');
            if (header) {
                const id = header.dataset.id;
                el.classList.toggle('completed', completed.has(id));
            }
        });
    }

    // =========================================================================
    // Render: Problem Detail
    // =========================================================================
    function showProblem(problem) {
        dom.viewWelcome.style.display = 'none';
        dom.viewCode.style.display = 'none';
        dom.viewProblem.style.display = 'block';
        dom.viewProblem.classList.remove('view');
        void dom.viewProblem.offsetWidth; // trigger reflow
        dom.viewProblem.classList.add('view');

        // Title
        dom.problemTitle.textContent = problem.title;

        // Progress button
        updateProblemProgressBtn();

        // Meta: difficulty + patterns
        const diffClass = problem.difficulty.toLowerCase();
        let metaHtml = `<span class="badge badge-${diffClass}">${problem.difficulty}</span>`;
        problem.patterns.forEach(pat => {
            metaHtml += `<span class="pattern-tag">${pat}</span>`;
        });
        dom.problemMeta.innerHTML = metaHtml;

        // Body: README as markdown
        let bodyHtml = '';


        // Show top-level folder structure (variants) as navigable links
        const topFolders = new Set();
        problem.files.forEach(f => {
            const firstPart = f.name.split('/')[0];
            topFolders.add(firstPart);
        });
        if (topFolders.size > 0) {
            bodyHtml += `<div class="quick-files"><span>Browse:</span>`;
            [...topFolders].sort().forEach(folder => {
                bodyHtml += `<a href="#${problem.id}/${folder}">📁 ${folder}/</a>`;
            });
            bodyHtml += `</div>`;
        }

        // Quick-open links for key files (e.g. Main.java in each variant)
        const quickFiles = problem.files
            .filter(f => /Main\.java$/.test(f.name))
            .map(f => f.name);
        if (quickFiles.length > 0) {
            bodyHtml += `<div class="quick-files"><span>Run:</span>`;
            quickFiles.forEach(filePath => {
                bodyHtml += `<a href="#${problem.id}/${filePath}">${filePath}</a>`;
            });
            bodyHtml += `</div>`;
        }

        // Render README markdown
        const readmeHtml = renderMarkdown(problem.readme);
        bodyHtml += `<div class="markdown-content">${readmeHtml}</div>`;

        // Variations section
        if (problem.variations) {
            bodyHtml += `
                <div class="variations-section">
                    <h2>Variations</h2>
                    <div class="markdown-content">${renderMarkdown(problem.variations)}</div>
                </div>
            `;
        }

        dom.problemBody.innerHTML = bodyHtml;

        // Highlight any code blocks in the markdown
        dom.problemBody.querySelectorAll('pre code').forEach(block => {
            Prism.highlightElement(block);
        });
    }

    function updateProblemProgressBtn() {
        if (!currentProblemId) return;
        const btn = dom.btnProgress;
        btn.classList.toggle('completed', completed.has(currentProblemId));
    }

    // =========================================================================
    // Render: Code Viewer
    // =========================================================================

    /** Restore the code container's <pre><code> structure if it was replaced by folder listing */
    function restoreCodeContainer() {
        const codeContainer = document.querySelector('.code-container');
        if (!codeContainer) return;
        // If the code container doesn't have a <pre> child, it was replaced by folder listing
        if (!codeContainer.querySelector('pre')) {
            codeContainer.innerHTML = '<pre class="line-numbers"><code class="language-java" id="code-content"></code></pre>';
            // Re-bind dom reference
            dom.codeContent = document.getElementById('code-content');
        }
    }

    function showCode(problem, filePath) {
        // Exit edit mode whenever we navigate to a different file
        if (editMode) exitEditMode();
        hideRunPanel();
        // Check if this is a folder path (no file extension) — show folder view
        const file = problem.files.find(f => f.name === filePath);
        if (!file) {
            // Could be a folder path — check if any files start with this prefix
            const folderPrefix = filePath.endsWith('/') ? filePath : filePath + '/';
            const folderFiles = problem.files.filter(f => f.name.startsWith(folderPrefix));
            if (folderFiles.length > 0) {
                showFolderView(problem, filePath);
                return;
            }
            showProblem(problem);
            return;
        }

        dom.viewWelcome.style.display = 'none';
        dom.viewProblem.style.display = 'none';
        dom.viewCode.style.display = 'block';
        dom.viewCode.classList.remove('view');
        void dom.viewCode.offsetWidth;
        dom.viewCode.classList.add('view');

        // Restore code container if it was replaced by folder listing
        restoreCodeContainer();

        // Show copy button (may have been hidden by folder view)
        dom.btnCopy.style.display = '';

        // Breadcrumb: problem / path / file — each segment is clickable (navigates to folder)
        const parts = filePath.split('/');
        const fileName = parts.pop();
        let breadcrumbHtml = `<a class="breadcrumb-segment breadcrumb-link" href="#${problem.id}">${problem.id}</a>`;
        let pathSoFar = '';
        parts.forEach(part => {
            pathSoFar += (pathSoFar ? '/' : '') + part;
            breadcrumbHtml += `<span class="breadcrumb-separator">/</span><a class="breadcrumb-segment breadcrumb-link" href="#${problem.id}/${pathSoFar}">${part}</a>`;
        });
        breadcrumbHtml += `<span class="breadcrumb-separator">/</span><span class="breadcrumb-file">${fileName}</span>`;

        // Show design patterns used in this problem
        if (problem.patterns && problem.patterns.length > 0) {
            breadcrumbHtml += `<span class="breadcrumb-patterns">`;
            problem.patterns.forEach(pat => {
                breadcrumbHtml += `<span class="breadcrumb-pattern-tag">${pat}</span>`;
            });
            breadcrumbHtml += `</span>`;
        }

        dom.codeBreadcrumb.innerHTML = breadcrumbHtml;

        // Code content
        dom.codeContent.textContent = file.content;
        dom.codeContent.className = 'language-java';
        Prism.highlightElement(dom.codeContent);

        // IDE features: clickable class names + hover tooltips
        applyIDEFeatures(problem, filePath);
        applyKeywordTooltips();

        // Reset copy button
        dom.btnCopy.classList.remove('copied');
        dom.btnCopy.querySelector('span').textContent = 'Copy';
    }

    /**
     * Show a GitHub-style folder listing when navigating to a directory path.
     * Lists subfolders and files in that directory, each clickable to navigate deeper.
     */
    function showFolderView(problem, folderPath) {
        dom.viewWelcome.style.display = 'none';
        dom.viewProblem.style.display = 'none';
        dom.viewCode.style.display = 'block';
        dom.viewCode.classList.remove('view');
        void dom.viewCode.offsetWidth;
        dom.viewCode.classList.add('view');

        const folderPrefix = folderPath.endsWith('/') ? folderPath : folderPath + '/';

        // Build breadcrumb — each segment is clickable
        const parts = folderPath.split('/');
        let breadcrumbHtml = `<a class="breadcrumb-segment breadcrumb-link" href="#${problem.id}">${problem.id}</a>`;
        let pathSoFar = '';
        parts.forEach((part, i) => {
            pathSoFar += (pathSoFar ? '/' : '') + part;
            breadcrumbHtml += `<span class="breadcrumb-separator">/</span>`;
            if (i === parts.length - 1) {
                breadcrumbHtml += `<span class="breadcrumb-file">${part}</span>`;
            } else {
                breadcrumbHtml += `<a class="breadcrumb-segment breadcrumb-link" href="#${problem.id}/${pathSoFar}">${part}</a>`;
            }
        });

        // Show design patterns
        if (problem.patterns && problem.patterns.length > 0) {
            breadcrumbHtml += `<span class="breadcrumb-patterns">`;
            problem.patterns.forEach(pat => {
                breadcrumbHtml += `<span class="breadcrumb-pattern-tag">${pat}</span>`;
            });
            breadcrumbHtml += `</span>`;
        }

        dom.codeBreadcrumb.innerHTML = breadcrumbHtml;

        // Find immediate children (subfolders and files)
        const children = new Map(); // name -> 'folder' | 'file'
        problem.files.forEach(f => {
            if (!f.name.startsWith(folderPrefix)) return;
            const remainder = f.name.slice(folderPrefix.length);
            const slashIdx = remainder.indexOf('/');
            if (slashIdx === -1) {
                // Direct file child
                children.set(remainder, 'file');
            } else {
                // Subfolder
                const folderName = remainder.slice(0, slashIdx);
                if (!children.has(folderName)) {
                    children.set(folderName, 'folder');
                }
            }
        });

        // Sort: folders first, then files
        const sorted = [...children.entries()].sort((a, b) => {
            if (a[1] !== b[1]) return a[1] === 'folder' ? -1 : 1;
            return a[0].localeCompare(b[0]);
        });

        // Render folder listing in the code area
        let listingHtml = `<div class="folder-listing">`;
        listingHtml += `<div class="folder-listing-header">
            <svg class="folder-listing-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
            </svg>
            <span>${folderPath}</span>
            <span class="folder-listing-count">${sorted.length} items</span>
        </div>`;

        sorted.forEach(([name, type]) => {
            const targetPath = folderPrefix + name;
            if (type === 'folder') {
                listingHtml += `
                    <a class="folder-listing-item folder-listing-folder" href="#${problem.id}/${targetPath}">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
                        </svg>
                        <span>${name}</span>
                    </a>`;
            } else {
                // Detect design pattern from file content for badge
                const fullFile = problem.files.find(f => f.name === targetPath);
                const patternBadge = detectFilePattern(fullFile);
                listingHtml += `
                    <a class="folder-listing-item folder-listing-file" href="#${problem.id}/${targetPath}">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                            <polyline points="14 2 14 8 20 8"></polyline>
                        </svg>
                        <span>${name}</span>
                        ${patternBadge ? `<span class="folder-listing-badge">${patternBadge}</span>` : ''}
                    </a>`;
            }
        });

        listingHtml += `</div>`;

        // Hide the copy button and show listing in code area
        dom.btnCopy.style.display = 'none';
        const codeContainer = document.querySelector('.code-container');
        if (codeContainer) {
            codeContainer.innerHTML = listingHtml;
        }
    }

    /**
     * Detect the primary design pattern in a file from its comments/content.
     * Returns a short badge string like "Strategy", "Observer", "Singleton", etc.
     * First checks the explicit "// DESIGN PATTERN:" header comment, then falls back to heuristics.
     */
    function detectFilePattern(file) {
        if (!file || !file.content) return '';
        const content = file.content.slice(0, 600); // Check first 600 chars

        // Primary: explicit DESIGN PATTERN comment (added to key files)
        const patternComment = content.match(/\/\/\s*DESIGN PATTERN:\s*(.+)/);
        if (patternComment) {
            return patternComment[1].trim();
        }

        // Fallback: heuristic detection from content
        if (/\bSingleton\b/i.test(content) && /\bgetInstance\b/.test(file.content)) return 'Singleton';
        if (/\bObserver\b/i.test(content) || /\binterface\b.*Observer\b/.test(content)) return 'Observer';
        if (/\bStrategy\b/i.test(content) || /\binterface\b.*Strategy\b/.test(content)) return 'Strategy';
        if (/\bFactory\b/i.test(content) && /\bcreate\b/.test(file.content)) return 'Factory';
        if (/\bState\s+pattern\b/i.test(content) || /\binterface\b.*State\b/.test(content)) return 'State';
        if (/\bCommand\b/i.test(content) && /\bexecute\b/.test(file.content)) return 'Command';
        if (/\bFacade\b/i.test(content) || /\bFACADE\b/.test(content)) return 'Facade';
        if (/\bBuilder\b/i.test(content) && /\bbuild\(\)/.test(file.content)) return 'Builder';
        if (/\bDecorator\b/i.test(content)) return 'Decorator';
        if (/\bIterator\b/i.test(content)) return 'Iterator';
        if (/\bMediator\b/i.test(content)) return 'Mediator';

        return '';
    }

    // =========================================================================
    // IDE Features: IntelliJ-style — click any class/interface/enum name to
    // navigate to its definition. Hover for preview tooltip.
    // Works on ALL occurrences in the code, not just Prism-tagged ones.
    // =========================================================================

    /**
     * Build a symbol index from all files in the current problem.
     * Maps class/interface/enum names to their file path and a short preview.
     * Prioritizes files from the same variant (naive/optimized/concurrent)
     * as the file being viewed.
     */
    function buildSymbolIndex(problem, currentFile) {
        const index = {}; // symbolName -> { file, preview, kind, line }

        // Determine current variant (naive, optimized, concurrent)
        const currentVariant = currentFile ? currentFile.split('/')[0] : '';

        // Sort files: same variant first, then others
        const sortedFiles = [...problem.files].sort((a, b) => {
            const aVariant = a.name.split('/')[0];
            const bVariant = b.name.split('/')[0];
            const aMatch = aVariant === currentVariant ? 0 : 1;
            const bMatch = bVariant === currentVariant ? 0 : 1;
            return aMatch - bMatch;
        });

        sortedFiles.forEach(file => {
            const content = file.content;
            const lines = content.split('\n');

            for (let i = 0; i < lines.length; i++) {
                const line = lines[i];

                // Match: class Foo, interface Foo, enum Foo, abstract class Foo
                const match = line.match(/\b(class|interface|enum)\s+([A-Z]\w*)/);
                if (match) {
                    const kind = match[1];
                    const name = match[2];

                    // Grab preview: up to 8 lines from the declaration
                    const previewLines = lines.slice(i, Math.min(i + 8, lines.length));
                    const preview = previewLines.join('\n');

                    // First definition wins (same variant prioritized by sort above)
                    if (!index[name]) {
                        index[name] = { file: file.name, preview, kind, line: i + 1 };
                    }
                }
            }
        });

        return index;
    }

    /**
     * After Prism highlights, walk ALL text nodes in the code element.
     * For every known symbol name found in text, wrap it in a clickable span.
     * This catches everything: field types, return types, generics, casts, etc.
     */
    // Java stdlib types we don't want to make clickable
    const IGNORE_SYMBOLS = new Set([
        'Main', 'String', 'Integer', 'Long', 'Double', 'Float', 'Boolean',
        'Object', 'System', 'Math', 'Thread', 'Runnable', 'Exception',
        'RuntimeException', 'IllegalArgumentException', 'NullPointerException',
        'IndexOutOfBoundsException', 'Override', 'Deprecated',
        'List', 'ArrayList', 'LinkedList', 'Map', 'HashMap', 'TreeMap',
        'Set', 'HashSet', 'TreeSet', 'Queue', 'Deque', 'Stack',
        'Collections', 'Arrays', 'Optional', 'Stream',
        'StringBuilder', 'StringBuffer', 'Comparable', 'Iterable', 'Iterator',
        'LocalDate', 'LocalDateTime', 'LocalTime', 'Duration', 'Instant',
    ]);

    function applyIDEFeatures(problem, currentFile) {
        const symbolIndex = buildSymbolIndex(problem, currentFile);
        const symbolNames = Object.keys(symbolIndex).filter(name =>
            name.length > 1 && !IGNORE_SYMBOLS.has(name)
        );
        if (symbolNames.length === 0) return;

        const codeEl = dom.codeContent;

        // Build a regex matching any known symbol as a whole word
        // Sort by length descending so longer names match first (e.g. ParkingSpot before Spot)
        symbolNames.sort((a, b) => b.length - a.length);
        const pattern = new RegExp('\\b(' + symbolNames.map(escapeRegex).join('|') + ')\\b', 'g');

        // Walk all text nodes and wrap matches
        const walker = document.createTreeWalker(codeEl, NodeFilter.SHOW_TEXT, null);
        const textNodes = [];
        while (walker.nextNode()) {
            textNodes.push(walker.currentNode);
        }

        textNodes.forEach(textNode => {
            const text = textNode.nodeValue;
            pattern.lastIndex = 0;
            if (!pattern.test(text)) return;
            pattern.lastIndex = 0;

            // Skip if inside an ide-link, comment, or string token
            const parent = textNode.parentElement;
            if (!parent) return;
            if (parent.classList.contains('ide-link')) return;
            if (parent.classList.contains('comment')) return;
            if (parent.classList.contains('string')) return;

            // Split text by matches and build replacement fragment
            const frag = document.createDocumentFragment();
            let lastIndex = 0;
            let match;

            pattern.lastIndex = 0;
            while ((match = pattern.exec(text)) !== null) {
                const symbolName = match[1];
                const symbolInfo = symbolIndex[symbolName];
                if (!symbolInfo) continue;

                // Text before the match
                if (match.index > lastIndex) {
                    frag.appendChild(document.createTextNode(text.slice(lastIndex, match.index)));
                }

                // Create the clickable span
                const span = document.createElement('span');
                span.textContent = symbolName;
                span.className = 'ide-link';
                span.setAttribute('data-symbol', symbolName);
                span.setAttribute('data-target-file', symbolInfo.file);

                const isSameFile = symbolInfo.file === currentFile;

                // Click → navigate to definition file (tooltip vanishes immediately)
                if (!isSameFile) {
                    span.addEventListener('click', (e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        hideIDETooltip();
                        navigateTo(problem.id, symbolInfo.file);
                    });
                } else {
                    span.classList.add('ide-link-same');
                }

                // Hover → show tooltip (with grace period to move into tooltip)
                span.addEventListener('mouseenter', (e) => {
                    clearTimeout(tooltipHideTimeout);
                    showIDETooltip(e.target, symbolInfo, isSameFile);
                });
                span.addEventListener('mouseleave', () => {
                    tooltipHideTimeout = setTimeout(hideIDETooltip, 150);
                });

                frag.appendChild(span);
                lastIndex = match.index + match[0].length;
            }

            // Remaining text after last match
            if (lastIndex < text.length) {
                frag.appendChild(document.createTextNode(text.slice(lastIndex)));
            }

            // Only replace if we found at least one match
            if (lastIndex > 0) {
                textNode.parentNode.replaceChild(frag, textNode);
            }
        });
    }

    function escapeRegex(str) {
        return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    /** Show a floating tooltip near the hovered token */
    let tooltipHideTimeout = null;

    function showIDETooltip(target, symbolInfo, isSameFile) {
        clearTimeout(tooltipHideTimeout);
        hideIDETooltip();

        const tooltip = document.createElement('div');
        tooltip.className = 'ide-tooltip';
        tooltip.id = 'ide-tooltip';

        const kindBadge = symbolInfo.kind;
        const fileLabel = symbolInfo.file;
        const actionHint = isSameFile
            ? '<span class="ide-tooltip-hint">Defined in this file</span>'
            : '<span class="ide-tooltip-hint">Click to go to definition</span>';

        tooltip.innerHTML = `
            <div class="ide-tooltip-header">
                <span class="ide-tooltip-kind">${kindBadge}</span>
                <span class="ide-tooltip-file">${fileLabel}:${symbolInfo.line}</span>
            </div>
            <pre class="ide-tooltip-code"><code>${escapeHtml(symbolInfo.preview)}</code></pre>
            ${actionHint}
        `;

        // Allow hovering INTO the tooltip (keep it alive)
        tooltip.addEventListener('mouseenter', () => {
            clearTimeout(tooltipHideTimeout);
        });
        tooltip.addEventListener('mouseleave', () => {
            tooltipHideTimeout = setTimeout(hideIDETooltip, 100);
        });

        document.body.appendChild(tooltip);

        // Position near the token
        const rect = target.getBoundingClientRect();
        const tooltipRect = tooltip.getBoundingClientRect();

        let top = rect.bottom + 8;
        let left = rect.left;

        if (top + tooltipRect.height > window.innerHeight - 20) {
            top = rect.top - tooltipRect.height - 8;
        }
        if (left + tooltipRect.width > window.innerWidth - 20) {
            left = window.innerWidth - tooltipRect.width - 20;
        }
        if (left < 10) left = 10;

        tooltip.style.top = top + 'px';
        tooltip.style.left = left + 'px';
        tooltip.style.opacity = '1';
    }

    function hideIDETooltip() {
        const existing = document.getElementById('ide-tooltip');
        if (existing) existing.remove();
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // =========================================================================
    // IDE Feature: Keyword Tooltips
    // =========================================================================
    const KEYWORD_TOOLTIPS = {
        'private':       'Only this class can access this member. Encapsulates internal state.',
        'public':        'Any code anywhere can access this. Used for APIs and entry points.',
        'protected':     'This class + subclasses can access. Used for inheritance hierarchies.',
        'static':        'Belongs to the class, not any instance. Shared across all objects.',
        'final':         'Cannot be reassigned after initialization. Ensures immutability and thread-safe publication.',
        'abstract':      'No implementation here — subclasses MUST provide their own version.',
        'synchronized':  'Only one thread can execute this block/method at a time. Prevents race conditions.',
        'volatile':      'All threads see the latest value immediately. No caching in CPU registers.',
        'interface':     'A contract — classes that implement this must define all its methods.',
        'implements':    'This class fulfills an interface contract. Must provide all interface methods.',
        'extends':       'Inherits fields and methods from a parent class. IS-A relationship.',
        'enum':          'A fixed set of constants. Type-safe alternative to magic strings/ints.',
        'new':           'Creates a new object instance on the heap.',
        'this':          'Refers to the current object instance.',
        'super':         'Refers to the parent class. Used to call parent constructors/methods.',
        'return':        'Exits the method and optionally sends a value back to the caller.',
        'void':          'This method returns nothing.',
        'null':          'No object — the reference points to nothing.',
        'throws':        'Declares that this method might throw an exception the caller must handle.',
        'throw':         'Immediately raises an exception.',
        'try':           'Attempts to execute code that might fail. Paired with catch/finally.',
        'catch':         'Handles a specific exception type if the try block fails.',
        'finally':       'Always runs, whether try succeeded or failed. Used for cleanup.',
        'import':        'Makes a class from another package available in this file.',
        'class':         'Defines a blueprint for creating objects. Contains fields and methods.',
        'default':       'Fallback case in a switch statement, or default access (package-private).',
        'break':         'Exits the current loop or switch statement immediately.',
        'continue':      'Skips the rest of this loop iteration and starts the next one.',
        'for':           'Repeats a block of code a set number of times (or over a collection).',
        'while':         'Repeats a block as long as a condition is true.',
        'if':            'Executes a block only when a condition is true.',
        'else':          'Executes when the if-condition is false.',
        'switch':        'Selects one of many code paths based on a value. Cleaner than many if-elses.',
        'case':          'One possible value in a switch statement.',
        'instanceof':    'Checks if an object is of a specific type at runtime.',
        'boolean':       'A true/false value. Used for flags and conditions.',
        'int':           'A 32-bit integer. Most common numeric type in Java.',
        'long':          'A 64-bit integer. Used for large numbers or timestamps.',
        'double':        'A 64-bit decimal number. Used for prices, measurements.',
        'char':          'A single Unicode character.',
        'byte':          'An 8-bit integer (-128 to 127).',
        'short':         'A 16-bit integer.',
        'float':         'A 32-bit decimal. Less precise than double.',
    };

    const ANNOTATION_TOOLTIPS = {
        '@Override':       'Tells the compiler you intend to override a parent method. Catches typos at compile time.',
        '@Deprecated':    'Marks this as outdated — still works, but shouldn\'t be used in new code.',
        '@SuppressWarnings': 'Silences specific compiler warnings. Use sparingly.',
        '@FunctionalInterface': 'Declares this interface has exactly one abstract method (can be used as a lambda).',
    };

    /** Apply keyword + annotation tooltips after Prism highlights */
    function applyKeywordTooltips() {
        const codeEl = dom.codeContent;

        // Keywords
        const keywordTokens = codeEl.querySelectorAll('.token.keyword');
        keywordTokens.forEach(token => {
            const keyword = token.textContent.trim();
            const tooltip = KEYWORD_TOOLTIPS[keyword];
            if (!tooltip) return;

            token.classList.add('ide-keyword-hoverable');

            token.addEventListener('mouseenter', (e) => {
                showKeywordTooltip(e.target, keyword, tooltip);
            });

            token.addEventListener('mouseleave', () => {
                hideIDETooltip();
            });
        });

        // Annotations (@Override, etc.)
        const annotationTokens = codeEl.querySelectorAll('.token.annotation');
        annotationTokens.forEach(token => {
            const text = token.textContent.trim();
            const tooltip = ANNOTATION_TOOLTIPS[text];
            if (!tooltip) return;

            token.classList.add('ide-keyword-hoverable');

            token.addEventListener('mouseenter', (e) => {
                showKeywordTooltip(e.target, text, tooltip);
            });

            token.addEventListener('mouseleave', () => {
                hideIDETooltip();
            });
        });
    }

    function showKeywordTooltip(target, keyword, description) {
        hideIDETooltip();

        const tooltip = document.createElement('div');
        tooltip.className = 'ide-tooltip ide-tooltip-keyword';
        tooltip.id = 'ide-tooltip';

        tooltip.innerHTML = `
            <div class="ide-tooltip-header">
                <span class="ide-tooltip-kind">keyword</span>
                <span class="ide-tooltip-file">${keyword}</span>
            </div>
            <div class="ide-tooltip-desc">${description}</div>
        `;

        document.body.appendChild(tooltip);

        const rect = target.getBoundingClientRect();
        const tooltipRect = tooltip.getBoundingClientRect();

        let top = rect.bottom + 6;
        let left = rect.left;

        if (top + tooltipRect.height > window.innerHeight - 20) {
            top = rect.top - tooltipRect.height - 6;
        }
        if (left + tooltipRect.width > window.innerWidth - 20) {
            left = window.innerWidth - tooltipRect.width - 20;
        }
        if (left < 10) left = 10;

        tooltip.style.top = top + 'px';
        tooltip.style.left = left + 'px';
        tooltip.style.opacity = '1';
    }

    // =========================================================================
    // Markdown Rendering
    // =========================================================================
    function renderMarkdown(text) {
        if (!text) return '';
        // Configure marked
        marked.setOptions({
            breaks: false,
            gfm: true,
            headerIds: false,
            mangle: false,
        });
        return marked.parse(text);
    }

    // =========================================================================
    // Routing
    // =========================================================================
    function navigateTo(problemId, filePath) {
        let hash = '#' + problemId;
        if (filePath) {
            hash += '/' + filePath;
        }
        window.location.hash = hash;
    }

    function handleRoute() {
        const hash = window.location.hash.slice(1); // remove #
        if (!hash) {
            showWelcome();
            currentProblemId = null;
            currentFilePath = null;
            expandedProblemId = null;
            renderFolderTree();
            return;
        }

        // Parse: problemId/optional/file/path
        const firstSlash = hash.indexOf('/');
        let problemId, filePath;

        if (firstSlash === -1) {
            problemId = hash;
            filePath = null;
        } else {
            problemId = hash.slice(0, firstSlash);
            filePath = hash.slice(firstSlash + 1);
        }

        const problem = problems.find(p => p.id === problemId);
        if (!problem) {
            showWelcome();
            currentProblemId = null;
            currentFilePath = null;
            expandedProblemId = null;
            renderFolderTree();
            return;
        }

        currentProblemId = problemId;
        currentFilePath = filePath || null;
        expandedProblemId = problemId;

        if (filePath) {
            showCode(problem, filePath);
        } else {
            showProblem(problem);
        }

        renderFolderTree();
        closeMobileSidebar();
    }

    function showWelcome() {
        dom.viewWelcome.style.display = 'block';
        dom.viewProblem.style.display = 'none';
        dom.viewCode.style.display = 'none';
    }

    // =========================================================================
    // Event Listeners
    // =========================================================================
    function setupListeners() {
        // Hash change (back/forward)
        window.addEventListener('hashchange', handleRoute);

        // Search with debounce
        dom.searchInput.addEventListener('input', () => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                activeFilters.search = dom.searchInput.value;
                applyFilters();
            }, 200);
        });

        // Difficulty chips
        dom.difficultyChips.addEventListener('click', (e) => {
            const chip = e.target.closest('.chip');
            if (!chip) return;
            dom.difficultyChips.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            activeFilters.difficulty = chip.dataset.difficulty || '';
            applyFilters();
        });

        // Pattern chips
        dom.patternChips.addEventListener('click', (e) => {
            const chip = e.target.closest('.chip');
            if (!chip) return;
            const isActive = chip.classList.contains('active');
            dom.patternChips.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
            if (!isActive) {
                chip.classList.add('active');
                activeFilters.pattern = chip.dataset.pattern;
            } else {
                activeFilters.pattern = '';
            }
            applyFilters();
        });

        // Progress button
        dom.btnProgress.addEventListener('click', () => {
            if (currentProblemId) {
                toggleCompleted(currentProblemId);
                renderWelcomeStats();
            }
        });

        // Copy button
        dom.btnCopy.addEventListener('click', () => {
            const code = dom.codeContent.textContent;
            navigator.clipboard.writeText(code).then(() => {
                dom.btnCopy.classList.add('copied');
                dom.btnCopy.querySelector('span').textContent = 'Copied!';
                setTimeout(() => {
                    dom.btnCopy.classList.remove('copied');
                    dom.btnCopy.querySelector('span').textContent = 'Copy';
                }, 2000);
            });
        });

        // Edit / Run / Tests / Run-panel-close
        dom.btnEdit.addEventListener('click', toggleEditMode);
        dom.btnRun.addEventListener('click', runCurrentCode);
        dom.btnTests.addEventListener('click', runTests);
        dom.runPanelClose.addEventListener('click', hideRunPanel);

        // Mobile hamburger
        dom.hamburger.addEventListener('click', toggleMobileSidebar);
        dom.sidebarOverlay.addEventListener('click', closeMobileSidebar);

        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            // Escape: close file view or mobile sidebar
            if (e.key === 'Escape') {
                if (dom.sidebar.classList.contains('open')) {
                    closeMobileSidebar();
                } else if (currentFilePath && currentProblemId) {
                    navigateTo(currentProblemId);
                }
            }
            // Ctrl/Cmd + K: focus search
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                dom.searchInput.focus();
            }
        });

        // Nav links
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const nav = link.dataset.nav;
                document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
                link.classList.add('active');

                if (nav === 'problems') {
                    window.location.hash = '';
                } else if (nav === 'patterns') {
                    // Show all, clear filters
                    activeFilters = { difficulty: '', pattern: '', search: '' };
                    dom.searchInput.value = '';
                    dom.difficultyChips.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
                    dom.difficultyChips.querySelector('[data-difficulty=""]').classList.add('active');
                    applyFilters();
                    window.location.hash = '';
                } else if (nav === 'progress') {
                    // Show only completed
                    window.location.hash = '';
                }
            });
        });
    }

    // =========================================================================
    // Mobile Sidebar
    // =========================================================================
    function toggleMobileSidebar() {
        dom.sidebar.classList.toggle('open');
        dom.sidebarOverlay.classList.toggle('open');
    }

    function closeMobileSidebar() {
        dom.sidebar.classList.remove('open');
        dom.sidebarOverlay.classList.remove('open');
    }

    // =========================================================================
    // Code execution: Edit (Monaco) + Run (local /api/run) + Tests
    // =========================================================================

    function loadMonaco() {
        if (monacoLoaded) return Promise.resolve();
        return new Promise((resolve, reject) => {
            if (typeof window.require === 'undefined') {
                reject(new Error('Monaco loader.js not present'));
                return;
            }
            window.require.config({
                paths: { 'vs': 'https://cdn.jsdelivr.net/npm/monaco-editor@0.45.0/min/vs' }
            });
            window.require(['vs/editor/editor.main'], () => {
                monacoLoaded = true;
                resolve();
            });
        });
    }

    async function enterEditMode() {
        if (!currentProblemId || !currentFilePath) return;
        try {
            await loadMonaco();
        } catch (e) {
            showRunPanel('fail', 'Failed to load editor: ' + e.message);
            return;
        }
        const problem = problems.find(p => p.id === currentProblemId);
        if (!problem) return;
        const file = problem.files.find(f => f.name === currentFilePath);
        if (!file) return;

        // Get edited content if any, otherwise the original
        const key = bufferKey(currentProblemId, currentFilePath);
        const initialContent = editBuffers.get(key) ?? file.content;

        document.querySelector('.code-container pre').style.display = 'none';
        dom.codeEditor.style.display = 'block';

        if (monacoEditor) {
            monacoEditor.dispose();
            monacoEditor = null;
        }
        monacoEditor = window.monaco.editor.create(dom.codeEditor, {
            value: initialContent,
            language: 'java',
            theme: 'vs-dark',
            automaticLayout: true,
            fontSize: 13,
            minimap: { enabled: false },
            scrollBeyondLastLine: false,
            tabSize: 4,
        });
        // Save edits to the buffer on every change
        monacoEditor.onDidChangeModelContent(() => {
            editBuffers.set(key, monacoEditor.getValue());
        });

        editMode = true;
        dom.btnEdit.classList.add('active');
        dom.btnEdit.querySelector('span').textContent = 'View';
    }

    function exitEditMode() {
        if (monacoEditor) {
            monacoEditor.dispose();
            monacoEditor = null;
        }
        dom.codeEditor.style.display = 'none';
        const pre = document.querySelector('.code-container pre');
        if (pre) pre.style.display = '';
        editMode = false;
        dom.btnEdit.classList.remove('active');
        dom.btnEdit.querySelector('span').textContent = 'Edit';
    }

    function toggleEditMode() {
        if (editMode) exitEditMode();
        else enterEditMode();
    }

    /**
     * Build the file list for /api/run. Uses each file's original content
     * unless the user has edited it (in which case the buffer wins).
     * Files are flattened so paths like "naive/service/Foo.java" become "service/Foo.java"
     * relative to the variant root the user is currently in.
     */
    function buildRunPayload(problem, filePath) {
        // Determine the variant root (e.g. "naive", "optimized", "concurrent") from filePath
        const variant = filePath.split('/')[0];
        const variantPrefix = variant + '/';
        const variantFiles = problem.files.filter(f => f.name.startsWith(variantPrefix));
        const payloadFiles = variantFiles.map(f => {
            const key = bufferKey(problem.id, f.name);
            const content = editBuffers.has(key) ? editBuffers.get(key) : f.content;
            // Strip the variant prefix from the file path for the server's tmp dir layout
            return { name: f.name.slice(variantPrefix.length), content };
        });
        return { files: payloadFiles, mainClass: 'Main', variant };
    }

    function showRunPanel(status, body) {
        dom.runPanel.style.display = '';
        dom.runPanelTitle.className = 'run-panel-title' + (status ? ' ' + status : '');
        if (typeof body === 'string') {
            dom.runPanelBody.textContent = body;
        } else {
            dom.runPanelBody.innerHTML = '';
            dom.runPanelBody.appendChild(body);
        }
    }

    function hideRunPanel() {
        dom.runPanel.style.display = 'none';
    }

    async function runCurrentCode() {
        if (!currentProblemId || !currentFilePath) return;
        const problem = problems.find(p => p.id === currentProblemId);
        if (!problem) return;
        const payload = buildRunPayload(problem, currentFilePath);
        if (!payload.files.length) {
            showRunPanel('fail', 'No files in this variant.');
            return;
        }

        dom.btnRun.disabled = true;
        dom.runPanelTitle.textContent = 'Running ' + payload.variant + '/Main…';
        showRunPanel(null, 'Compiling and running on local server…');

        try {
            const resp = await fetch('/api/run', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    files: payload.files,
                    mainClass: payload.mainClass,
                }),
            });
            if (!resp.ok) {
                showRunPanel('fail', 'Server returned ' + resp.status + ' ' + resp.statusText
                    + '\n(Is the run server up? Start with: node website/server.js)');
                return;
            }
            const result = await resp.json();
            renderRunResult(result, payload.variant);
        } catch (e) {
            showRunPanel('fail', 'Network error: ' + e.message
                + '\n\nMake sure you started the server: node website/server.js');
        } finally {
            dom.btnRun.disabled = false;
        }
    }

    function renderRunResult(result, variant) {
        const ok = result.exitCode === 0 && !result.timedOut;
        dom.runPanelTitle.textContent = (ok ? '✓ ' : '✗ ')
            + (result.stage === 'compile' && !ok ? 'Compile error' : 'Run finished')
            + ' — ' + variant
            + ' (compile ' + result.compileMs + 'ms, run ' + result.runMs + 'ms, exit ' + result.exitCode + ')';

        const container = document.createElement('div');
        if (result.timedOut) {
            const t = document.createElement('div');
            t.className = 'run-panel-section run-panel-stderr';
            t.textContent = '[server] Process timed out and was killed.';
            container.appendChild(t);
        }
        if (result.stdout) {
            const sec = document.createElement('div');
            sec.className = 'run-panel-section';
            sec.innerHTML = '<div class="run-panel-section-label">stdout</div>';
            const pre = document.createElement('div');
            pre.textContent = result.stdout;
            sec.appendChild(pre);
            container.appendChild(sec);
        }
        if (result.stderr) {
            const sec = document.createElement('div');
            sec.className = 'run-panel-section run-panel-stderr';
            sec.innerHTML = '<div class="run-panel-section-label">stderr</div>';
            const pre = document.createElement('div');
            pre.textContent = result.stderr;
            sec.appendChild(pre);
            container.appendChild(sec);
        }
        if (!result.stdout && !result.stderr) {
            container.textContent = '(no output)';
        }
        showRunPanel(ok ? 'ok' : 'fail', container);
    }

    /**
     * Run all test cases for the current problem.
     * Tests live at /tests/<problemId>.json (relative to website root) and have shape:
     *   { variant: "naive" | "optimized", cases: [
     *       { name: "test 1", driver: "...full Main.java...", expectedStdoutContains: [...], ... }
     *   ] }
     * Each case replaces Main.java with the case's driver, then runs.
     */
    async function runTests() {
        if (!currentProblemId) return;
        const problem = problems.find(p => p.id === currentProblemId);
        if (!problem) return;

        dom.btnTests.disabled = true;
        showRunPanel(null, 'Loading test cases…');
        let testSpec;
        try {
            const resp = await fetch('tests/' + problem.id + '.json');
            if (!resp.ok) {
                showRunPanel('fail', 'No tests defined for this problem yet.\n'
                    + '(File expected at website/tests/' + problem.id + '.json)');
                dom.btnTests.disabled = false;
                return;
            }
            testSpec = await resp.json();
        } catch (e) {
            showRunPanel('fail', 'Could not load tests: ' + e.message);
            dom.btnTests.disabled = false;
            return;
        }

        const variant = testSpec.variant || (currentFilePath ? currentFilePath.split('/')[0] : 'naive');
        const cases = testSpec.cases || [];
        if (!cases.length) {
            showRunPanel('fail', 'Test file has no cases.');
            dom.btnTests.disabled = false;
            return;
        }

        dom.runPanelTitle.textContent = 'Running ' + cases.length + ' tests on ' + variant + '…';
        const container = document.createElement('div');
        showRunPanel(null, container);

        let pass = 0, fail = 0;
        for (let i = 0; i < cases.length; i++) {
            const tc = cases[i];
            const row = document.createElement('div');
            row.className = 'test-case';
            row.innerHTML = `<div class="test-case-status skip">…</div>
                             <div><div class="test-case-name">${tc.name || 'case ' + (i+1)}</div></div>`;
            container.appendChild(row);
            const result = await runOneTestCase(problem, variant, tc);
            const status = row.querySelector('.test-case-status');
            const detail = document.createElement('div');
            detail.className = 'test-case-detail';
            if (result.pass) {
                pass++;
                status.textContent = 'PASS';
                status.className = 'test-case-status pass';
                if (result.stdout && tc.showOutputOnPass) {
                    detail.textContent = result.stdout.slice(0, 400);
                    row.querySelector('div:nth-child(2)').appendChild(detail);
                }
            } else {
                fail++;
                status.textContent = 'FAIL';
                status.className = 'test-case-status fail';
                detail.textContent = result.reason
                    + (result.stdout ? '\n--- stdout ---\n' + result.stdout.slice(0, 600) : '')
                    + (result.stderr ? '\n--- stderr ---\n' + result.stderr.slice(0, 400) : '');
                row.querySelector('div:nth-child(2)').appendChild(detail);
            }
        }

        dom.runPanelTitle.textContent = `${pass}/${cases.length} passed` + (fail ? `, ${fail} failed` : '');
        dom.runPanelTitle.className = 'run-panel-title ' + (fail ? 'fail' : 'ok');
        dom.btnTests.disabled = false;
    }

    async function runOneTestCase(problem, variant, tc) {
        // Build payload: all variant files, but Main.java replaced with the case's driver.
        const variantPrefix = variant + '/';
        const variantFiles = problem.files.filter(f => f.name.startsWith(variantPrefix));
        const payloadFiles = variantFiles.map(f => {
            const key = bufferKey(problem.id, f.name);
            let content = editBuffers.has(key) ? editBuffers.get(key) : f.content;
            const stripped = f.name.slice(variantPrefix.length);
            // Replace Main.java content with the driver if provided
            if (stripped === 'Main.java' && tc.driver) {
                content = tc.driver;
            }
            return { name: stripped, content };
        });
        // Also support tc.replaceFiles: { "service/Foo.java": "..." } to override any file
        if (tc.replaceFiles) {
            for (const [name, content] of Object.entries(tc.replaceFiles)) {
                const idx = payloadFiles.findIndex(f => f.name === name);
                if (idx >= 0) payloadFiles[idx].content = content;
                else payloadFiles.push({ name, content });
            }
        }

        try {
            const resp = await fetch('/api/run', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    files: payloadFiles,
                    mainClass: 'Main',
                    stdin: tc.stdin || '',
                }),
            });
            if (!resp.ok) {
                return { pass: false, reason: 'Server error ' + resp.status };
            }
            const result = await resp.json();
            return evaluateTestCase(tc, result);
        } catch (e) {
            return { pass: false, reason: 'Network error: ' + e.message };
        }
    }

    function evaluateTestCase(tc, result) {
        if (result.timedOut) return { pass: false, reason: 'Timed out', stdout: result.stdout, stderr: result.stderr };
        if (result.stage === 'compile' && result.exitCode !== 0) {
            return { pass: false, reason: 'Compile error', stdout: result.stdout, stderr: result.stderr };
        }
        if (tc.expectedExitCode !== undefined && result.exitCode !== tc.expectedExitCode) {
            return { pass: false, reason: `Expected exit ${tc.expectedExitCode}, got ${result.exitCode}`, stdout: result.stdout, stderr: result.stderr };
        }
        if (tc.expectedStdoutContains) {
            for (const needle of tc.expectedStdoutContains) {
                if (!result.stdout.includes(needle)) {
                    return { pass: false, reason: `stdout missing: "${needle}"`, stdout: result.stdout, stderr: result.stderr };
                }
            }
        }
        if (tc.expectedStdoutEquals !== undefined) {
            if (result.stdout.trim() !== tc.expectedStdoutEquals.trim()) {
                return { pass: false, reason: 'stdout did not match expected (trimmed)', stdout: result.stdout, stderr: result.stderr };
            }
        }
        if (tc.expectedStdoutNotContains) {
            for (const needle of tc.expectedStdoutNotContains) {
                if (result.stdout.includes(needle)) {
                    return { pass: false, reason: `stdout should NOT contain: "${needle}"`, stdout: result.stdout, stderr: result.stderr };
                }
            }
        }
        return { pass: true, stdout: result.stdout, stderr: result.stderr };
    }

    // =========================================================================
    // Boot
    // =========================================================================
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
