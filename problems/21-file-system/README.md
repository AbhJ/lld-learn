# In-Memory File System


## Problem Statement
Design an in-memory file system that mimics the behavior of a Unix-like file system, supporting hierarchical directories, files with content, and standard operations such as create, read, write, delete, list, and search. The file system has a single root directory `/`, and every file or directory is identified by an absolute path (e.g., `/home/user/notes.txt`).

Files store text or binary content and have associated metadata: name, size, creation timestamp, and last-modified timestamp. Directories contain other files and sub-directories, and the same name cannot be reused for two entries within the same parent. Operations should treat files and directories uniformly when traversing the tree (e.g., computing total size, searching by name).

The system should support recursive operations (delete a directory and all its contents), efficient path lookup, size aggregation across subtrees, and pattern-based search by file name. It must also enforce simple permission rules (read/write) per entry.

## Requirements

### Functional Requirements
- Create files and directories at any path (mkdir, touch)
- Read and write file content
- Delete files and directories (recursive delete for non-empty directories)
- List the contents of a directory
- Move/rename files and directories
- Search for entries by name across the tree
- Compute total size of a directory (sum of all descendant file sizes)
- Track metadata: name, size, created/modified timestamps
- Support read/write permission flags per entry
- Reject duplicate names within the same parent directory

### Non-functional Requirements
- O(1) absolute-path lookup in the optimized version (HashMap path index)
- O(1) size queries via cached aggregates with dirty-flag invalidation
- Thread-safe concurrent file/directory creation and deletion (concurrent version)
- Extensible: new operations should be addable without modifying tree node classes
- Memory-efficient — no full-tree scans for common operations

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Composite | FileSystemEntry/File/Directory | Uniform treatment of files and directories |
| Visitor | SearchVisitor, SizeVisitor | Add operations without modifying tree nodes |
| Iterator | Directory traversal | Standard traversal interface |

---

## Folder Structure
```
21-file-system/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← FileSystemEntry, File, Directory, Path, Permission
│   ├── service/      ← FileSystem, SearchVisitor, SizeVisitor
│   ├── strategy/     ← FileSystemVisitor interface
│   └── Main.java
└── optimized/
    ├── model/        ← Same entities with cached sizes
    ├── service/      ← FileSystem with HashMap path index
    ├── strategy/     ← FileSystemVisitor interface
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/21-file-system/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/21-file-system/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Path resolution | O(depth) tree traversal | O(1) HashMap lookup |
| Size calculation | O(n) full subtree traversal | O(1) cached (dirty flag invalidation) |
| Search by name | O(n) recursive visitor | O(n) flat index scan (cache-friendly) |
| Delete | O(depth) + child removal | O(1) lookup + recursive index cleanup |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two threads creating/deleting files in same directory simultaneously — directory listing corruption.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ConcurrentHashMap | FileNode.children | Lock-free directory child management |
| AtomicReference | FileNode.content | Read-write file content without torn reads |
| putIfAbsent | FileNode.addChild() | Atomic file creation — no duplicates |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
