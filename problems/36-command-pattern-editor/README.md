# Command Pattern Editor


## Problem Statement
Design a text editor that records every edit as a reversible command, so the user can undo and redo arbitrary sequences of changes. Operations include insert, delete, replace, and macros that group multiple commands as one.

The naive variant keeps an unbounded undo Stack. The optimized variant uses a bounded circular buffer so memory stays capped on long sessions.

## Requirements

### Functional Requirements
- Insert, delete, replace text at a position
- Undo and redo any executed command
- Macro commands that group multiple edits as one undo unit
- Clipboard support (cut/copy/paste in naive)
- Bounded history size (optimized)

### Non-functional Requirements
- O(1) push/pop per command
- Bounded memory for long edit sessions (optimized)
- Each command stores enough state to invert itself

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Command | Command, InsertCommand, DeleteCommand, ReplaceCommand | Encapsulate edits as objects with execute/undo |
| Composite | MacroCommand | A single command made of many child commands |
| Memento | DocumentMemento + Document.save()/restore() (both variants) | Originator (Document) emits opaque DocumentMemento; caretaker (history / BoundedHistory) holds it but cannot read its private state — only Document's package-private accessor can |
| Circular Buffer | BoundedHistory (optimized) | Cap memory by overwriting oldest commands |

## Folder Structure

```
36-command-pattern-editor/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Document, Clipboard
│   ├── command/      ← Command, InsertCommand, DeleteCommand, ReplaceCommand, MacroCommand
│   ├── service/      ← CommandHistory (unlimited Stack), Editor
│   └── Main.java
└── optimized/
    ├── model/        ← Document (with snapshot/restore)
    ├── command/      ← Command, InsertCommand, DeleteCommand, MacroCommand
    ├── service/      ← BoundedHistory (circular buffer), Editor
    └── Main.java
```

## How to Run

```bash
# Naive
cd naive && mkdir -p out && javac -d out model/*.java command/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd optimized && mkdir -p out && javac -d out model/*.java command/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| History storage | Unbounded `Stack` (memory leak risk) | Circular buffer with fixed max size |
| Memory | Grows indefinitely with edits | O(maxSize) cap regardless of session length |
| Eviction | None — old commands accumulate forever | Oldest commands naturally overwritten |
| Snapshot | Not needed | Lazy snapshot at eviction boundary |
| Macro | Supported | Composite macro counts as single undo step |
| Production risk | Long sessions can OOM | Bounded, predictable memory footprint |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two users editing same document — undo by one user undoes the other's changes.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicInteger | Document version | CAS-based optimistic locking |
| compareAndSet | applyEdit() | Only apply if version matches — detects conflicts |
| CopyOnWriteArrayList | Content and history | Safe concurrent reads during iteration |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
