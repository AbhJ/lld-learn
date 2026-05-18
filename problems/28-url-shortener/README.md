# URL Shortener


## Problem Statement
Design a URL shortener service like **bit.ly** or **TinyURL**. The service takes a long URL (e.g., `https://www.example.com/some/very/long/path?with=params&and=more`) and returns a short alias (e.g., `https://sho.rt/abc123`). When anyone visits the short URL, the service looks up the original long URL and redirects the browser to it.

The short code must be **unique** — two different long URLs must never map to the same short code, otherwise users would be redirected to the wrong destination. The code should also be **short** (6–8 characters) and use URL-safe characters (Base62: `[a-zA-Z0-9]`, giving ~56 billion combinations with 6 chars). Users may optionally request a **custom alias** (e.g., `sho.rt/my-blog`); the service must reject the alias if it's already taken.

Two encoding approaches are common: (1) generate a **random** code and check the database for collisions, retrying on conflict — simple but unbounded retries under high traffic; or (2) maintain a monotonic **counter** and Base62-encode it — guaranteed unique on the first try, but predictable. The system must also handle high concurrent shorten requests without two shorteners issuing the same code, and must support fast lookups (millions of redirects per second).

## Requirements

### Functional Requirements
- Shorten a long URL → return a short code (6–8 chars, Base62)
- Resolve a short code → return the original long URL
- Reject duplicate codes — every code maps to exactly one URL
- Support custom aliases with collision rejection
- Reverse lookup: given a long URL, return its existing short code (avoid duplicates)
- Optional: track click counts and creation timestamp per short URL
- Optional: TTL / expiry for short URLs

### Non-functional Requirements
- O(1) shorten in optimized version (AtomicLong counter, no retry loop)
- O(1) resolve via HashMap / ConcurrentHashMap lookup
- Thread-safe under concurrent shortens — no two requests get the same code
- Atomic reservation via ConcurrentHashMap.putIfAbsent (no check-then-insert race)
- Deterministic uniqueness in optimized (counter); probabilistic in naive (random + retry)
- Extensible encoding via Strategy pattern (Base62, hash, random, custom)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | EncodingStrategy | Pluggable encoding (Base62, Hash, Random) |
| Singleton | URLShortener (optional) | Single shortener instance |

---

## Folder Structure
```
28-url-shortener/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← URLMapping
│   ├── service/      ← URLShortener (random + collision check)
│   ├── strategy/     ← EncodingStrategy (RandomEncoding)
│   └── Main.java
└── optimized/
    ├── model/        ← URLMapping
    ├── service/      ← URLShortener (AtomicLong counter, no collisions)
    ├── strategy/     ← EncodingStrategy (Base62Encoding)
    └── Main.java
```

### How to Run
```bash
# Naive
cd problems/28-url-shortener/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/28-url-shortener/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Code generation | Random + collision check loop | AtomicLong counter (guaranteed unique) |
| Shorten (worst case) | O(retries) — unbounded | O(1) — always first attempt |
| Thread safety | Not safe (HashMap) | AtomicLong for counter |
| Uniqueness guarantee | Probabilistic | Deterministic (monotonic counter) |

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two requests generating same short URL (hash collision) simultaneously.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicLong | counter | Sequential ID generation — no collisions possible |
| ConcurrentHashMap.putIfAbsent | codeToUrl | Atomic URL registration |
| ConcurrentHashMap | urlToCode | Thread-safe reverse lookup |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
