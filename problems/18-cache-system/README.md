# Cache System (LRU/LFU)


## Problem Statement
Design a generic cache system supporting multiple eviction policies (LRU, LFU, FIFO). The cache provides a simple key-value interface with configurable capacity and optional TTL (Time-To-Live) support.

The system tracks cache statistics (hits, misses, evictions) and supports eviction notifications. The TTL decorator adds time-based expiration to any underlying cache implementation. Each eviction policy is implemented as a separate strategy.

The cache is generic and can store any type of value with string keys.

## Requirements
### Functional Requirements
- Put and get operations with string keys
- Configurable capacity
- LRU eviction (Least Recently Used)
- LFU eviction (Least Frequently Used)
- FIFO eviction (First In First Out)
- TTL support (time-based expiration)
- Cache statistics (hit rate, eviction count)
- Eviction notification

### Non-functional Requirements
- O(1) get/put for LRU
- Thread-safe operations
- Generic value type support
- Extensible eviction policies

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | Eviction policy | Pluggable eviction algorithms |
| Proxy | `DataSource` -> `CachingProxy` | Cache-aside layer fronts a slow backend; same `load(key)` API, transparent to callers |
| Decorator | TTL wrapper | Add TTL without modifying cache implementations |
| Observer | Eviction notification | Notify listeners when entries are evicted |

## Folder Structure
```
18-cache-system/
├── naive/
│   ├── model/      -> CacheEntry, CacheStats, EvictionPolicy
│   ├── service/    -> Cache interface, LRUCache, LFUCache, FIFOCache, TTLCache
│   └── Main.java
└── optimized/
    ├── model/
    ├── service/    -> Generic Cache<K,V>, LRUEviction (HashMap+DLL), LFUEviction (TreeMap)
    ├── strategy/   -> EvictionStrategy<K> interface
    └── Main.java
```

### How to Run
```bash
# Run the naive (simple) version:
cd naive
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main

# Run the optimized version:
cd optimized
mkdir -p out
javac -d out model/*.java strategy/*.java service/*.java Main.java
java -cp out Main
```

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| LRU evict | LinkedHashMap (Java built-in) | HashMap + DoublyLinkedList O(1) |
| LFU evict | HashMap + LinkedHashSet scan | TreeMap O(log n) min-frequency |
| Cache implementation | Separate class per policy | Single Cache<K,V> + pluggable strategy |
| Type safety | Cache<V> (String keys only) | Cache<K,V> (generic keys and values) |

---

## Class Diagram (Text)
```
Cache<V> (Interface)
 ├── LRUCache<V>
 ├── LFUCache<V>
 ├── FIFOCache<V>
 ├── TTLCache<V> (Decorator)
 ├── CacheEntry<V>
 ├── EvictionPolicy (Strategy)
 └── CacheStats
```

## How to Compile and Run
```bash
cd problems/18-cache-system
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== Cache System Demo ===
LRU Cache: put(1, "Hello"), put(2, "World"), put(3, "!")
Get(1): "Hello" (hit)
Put(4): evicts key 2 (least recently used)
Cache stats: hits=5, misses=2, hit rate=71.4%
TTL Cache: entry expired after 100ms
```

## Key Design Decisions
- LRU uses LinkedHashMap (or doubly-linked list + HashMap) for O(1) operations
- LFU tracks frequency with min-heap approach
- FIFO uses simple queue ordering
- TTL decorator wraps any cache implementation transparently
- Statistics tracked per-cache instance

## Interview Tips
- Explain the O(1) LRU implementation using HashMap + Doubly Linked List
- Discuss LFU vs LRU trade-offs
- Talk about TTL implementation (lazy expiration vs active cleanup)
- Mention distributed cache considerations (consistency, invalidation)
- Discuss cache stampede prevention

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Cache stampede — multiple threads requesting same expired key simultaneously.

```bash
cd concurrent
mkdir -p out
javac -d out model/*.java service/*.java Main.java
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ReentrantReadWriteLock | ConcurrentLRUCache | Multiple readers, single writer for cache access |
| Per-key ReentrantLock | StampedeProtection | Only one thread recomputes per expired key |
| Singleflight pattern | StampedeProtection.getOrCompute() | Prevent thundering herd on cache miss |
| Double-check after lock | StampedeProtection | Avoid redundant computation if another thread finished |
| volatile | CacheEntry fields | Ensure visibility of expiration state across threads |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
