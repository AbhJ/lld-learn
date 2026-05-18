# Document Editor


## Problem Statement
Design a collaborative document editor that supports text insertion/deletion, formatting, undo/redo, named version snapshots, and multiple collaborators with their own cursors.

The naive variant stores text as a list of segments and uses string copies for snapshots. The optimized variant uses a gap buffer for O(1) edits at the cursor and reverse-diff version storage.

## Requirements

### Functional Requirements
- Insert and delete text at any position
- Apply formatting (bold, italic, etc.) to a range
- Undo and redo edits
- Save and restore named versions
- Multiple collaborators with independent cursors

### Non-functional Requirements
- Efficient edits near the cursor (optimized: gap buffer)
- Bounded version storage via diffs (optimized)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Command | EditCommand (InsertText, DeleteText, FormatText) | Each edit is undoable/redoable |
| Memento | Version / VersionHistory | Save and restore named document snapshots |
| Gap Buffer | GapBuffer (optimized) | Efficient localized edits |
| Facade | DocumentEditor | Single entry point over document, history, collaborators |

## Folder Structure

```
43-document-editor/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Document, TextSegment, Formatting, Collaborator, Cursor, Version
│   ├── service/      ← DocumentEditor, EditCommand, VersionHistory
│   └── Main.java
└── optimized/
    ├── model/        ← GapBuffer, Formatting, Collaborator, Cursor, Version
    ├── service/      ← DocumentEditor, EditCommand (reverse-diff), VersionHistory (indexed)
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/43-document-editor/naive
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/43-document-editor/optimized
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Text insertion | O(n) String concatenation rebuilds content | O(1) amortized via GapBuffer at cursor |
| Undo mechanism | Full document snapshot per operation | Reverse-diff: stores only deleted text |
| Version lookup | O(n) linear search by ID | O(1) HashMap index by version ID |
| Memory for undo | O(n * edits) — full copy each time | O(chars_changed) per operation |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two collaborators editing same paragraph — one's changes overwrite the other's.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicInteger version | DocumentSegment.version | Optimistic concurrency — detect conflicts via version mismatch |
| CAS (compareAndSet) | DocumentSegment.tryEdit() | Lock-free edit: only succeeds if version unchanged since read |
| AtomicReference<String> | DocumentSegment.content | Safe content swap after successful CAS |
| CAS retry loop | CollaborativeEditor.editWithRetry() | Guarantees all edits eventually apply — no lost updates |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
