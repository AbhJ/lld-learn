# Spreadsheet


## Problem Statement
Design a spreadsheet application similar to Excel or Google Sheets. The spreadsheet is a 2D grid of cells addressed by column-letter and row-number (e.g., `A1`, `B7`, `AA42`). Each cell holds either a literal value (number or string) or a formula that references other cells (e.g., `=A1+B2`, `=SUM(A1:A5)`).

When a cell's value changes, every cell that depends on it — directly or transitively — must be recalculated and reflect the new value. The system must detect circular dependencies (e.g., `A1 = B1 + 1` and `B1 = A1 + 1`) and reject them, since they cannot be resolved. Recalculation should propagate in the correct order so that a dependent is never evaluated before its dependencies.

The user can edit cells freely, and the spreadsheet should support **undo and redo** of edits. Observers (like a UI layer) should be notified whenever a cell's displayed value changes, including cells that updated as a side effect of another cell's edit.

## Requirements

### Functional Requirements
- Set a cell to a literal value (number or string)
- Set a cell to a formula that references other cells (e.g., `=A1+B2`, `=SUM(A1:A5)`)
- Evaluate formulas with operators `+`, `-`, `*`, `/` and a `SUM` function
- Detect and reject circular dependencies
- Recalculate dependent cells when a referenced cell changes
- Undo and redo cell edits
- Notify observers when cell values change (including cascade updates)
- Iterate over all populated cells

### Non-functional Requirements
- Recompute only affected cells, not the entire sheet (topological order in optimized)
- Cached cell values with dirty-flag invalidation for lazy evaluation
- Thread-safe concurrent edits with versioned reads to detect stale recalculations
- Extensible formula functions

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Command | CellCommand | Undo/redo of cell edits |
| Memento | CellMemento | Save/restore cell state |
| Observer | CellObserver / LoggingCellObserver | Subscribers notified on cell value change and dependent recalc |

---

## Folder Structure
```
22-spreadsheet/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Cell, CellValue, CellMemento, Formula
│   ├── service/      ← Spreadsheet, DependencyGraph, SpreadsheetIterator
│   ├── command/      ← CellCommand (undo/redo)
│   └── Main.java
└── optimized/
    ├── model/        ← Cell with cached computed value + dirty flag
    ├── service/      ← Spreadsheet with topological sort recalculation
    ├── command/      ← CellCommand
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/22-spreadsheet/naive
mkdir -p out && javac -d out model/*.java service/*.java command/*.java Main.java && java -cp out Main

# Optimized
cd problems/22-spreadsheet/optimized
mkdir -p out && javac -d out model/*.java service/*.java command/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Cell change propagation | Recalculates ALL dependents (BFS) | Topological sort — only affected cells |
| Formula evaluation | Re-evaluates every time | Cached value with dirty flag (lazy eval) |
| Dependency detection | BFS over all transitive deps | Same detection, but minimal recalc set |
| Memory per cell | Just value | Value + cached numeric + dirty bit |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two users editing cells that depend on each other — circular recalculation, stale values.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ReentrantReadWriteLock | Cell.lock | Many readers, exclusive writer per cell |
| AtomicLong | Cell.version | Detect stale reads during recalculation |
| ConcurrentHashMap | Spreadsheet.cells | Thread-safe cell registry |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
