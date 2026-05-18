# Library Management System


## Problem Statement
Design a library management system that handles book cataloging, member management, borrowing/returning of books, fine calculation, search functionality, and book reservations.

The library maintains a catalog of books, each of which can have multiple physical copies. Members can borrow books (up to a limit), return them, and place reservations for books that are currently unavailable. Fines are calculated based on configurable strategies when books are returned late.

Librarians can add/remove books, manage members, and view system statistics. The system supports multiple search strategies (by title, author, ISBN) and notifies members when reserved books become available.

## Requirements

### Functional Requirements
- Add/remove books and physical copies
- Register/manage library members
- Borrow and return books with due dates
- Calculate fines for late returns (daily, weekly strategies)
- Search books by title, author, or ISBN
- Reserve books that are currently checked out
- Notify members when reserved books become available
- Track borrowing history

### Non-functional Requirements
- Efficient search across catalog
- Extensible fine calculation strategies
- Observable notifications for reservations
- Member borrowing limits enforced

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Repository | Book catalog | Centralized book management and search |
| Observer | Reservation notification | Notify members when reserved books are available |
| Strategy | FineStrategy, SearchStrategy | Swappable fine calculation and search algorithms |
| Facade | Library class | Simplified interface for complex subsystem |

## Folder Structure
```
03-library-management/
├── naive/          <- Start here. Linear scan searches.
│   ├── model/      -> Data classes (Book, BookCopy, Member, BorrowRecord, Reservation, Librarian)
│   ├── service/    -> Business logic (Library facade)
│   ├── strategy/   -> Swappable algorithms (FineStrategy, SearchStrategy)
│   └── Main.java   -> Entry point — run this
└── optimized/      <- Production-grade. HashMap-indexed O(1) searches.
    ├── model/
    ├── service/
    ├── strategy/
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java strategy/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java strategy/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized — What Changes?
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Search by title | O(n) linear scan | O(1) HashMap word-index lookup |
| Search by author | O(n) linear scan | O(1) HashMap word-index lookup |
| Search by ISBN | O(n) linear scan | O(1) HashMap direct lookup |
| Index maintenance | None | buildIndex() on add, auto-update |

---

## Class Diagram (Text)
```
Library (Facade)
├── Book (title, author, ISBN)
│   └── BookCopy[] (physical copies, status)
├── Member (name, borrowed books, reservations)
├── Librarian (admin operations)
├── BorrowRecord (copy, member, borrow date, due date, return date)
├── FineStrategy (interface)
│   ├── DailyFine
│   └── WeeklyFine
├── SearchStrategy (interface)
│   ├── TitleSearch
│   ├── AuthorSearch
│   └── ISBNSearch
└── Reservation (member, book, timestamp)
```

## Key Design Decisions
- Book vs BookCopy separation: a Book is a catalog entry, BookCopy is a physical item
- BorrowRecord tracks the full lifecycle of a borrow transaction
- FineStrategy allows different fine models without modifying core logic
- Reservations use a queue (FIFO) per book

## Interview Tips
- Start with entity identification: Book, Member, Copy, Record
- Clarify borrowing rules: max books, loan period, renewal
- Discuss search complexity: linear scan vs. index-based
- Talk about concurrency: two members borrowing the last copy
- Mention extensibility: digital books, inter-library loans

---

## Concurrency Version

**Race condition:** Two members trying to borrow the last copy of a book simultaneously — both see copies > 0 and both succeed, resulting in negative available copies.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicInteger | Book.availableCopies | Lock-free atomic decrement for available copy count |
| CAS with rollback | Book.tryBorrow() | decrementAndGet; if < 0, rollback — ensures exactly N borrows for N copies |
| CountDownLatch | Main.java | All 10 member threads release simultaneously to create real contention |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
