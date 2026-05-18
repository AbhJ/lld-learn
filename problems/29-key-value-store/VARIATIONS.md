# Key-Value Store - Variations

## Variation 1: Distributed KV (Eventually Consistent)
**Learning Value:** Teaches distributed consensus, consistent hashing, and replication with tunable consistency.

### Additional Requirements
- Consistent hashing for data partitioning across nodes
- Vector clocks for conflict detection
- Quorum reads/writes (R + W > N for strong consistency)
- Hinted handoff for temporary failures
- Anti-entropy with Merkle trees for replica synchronization

### Design Changes
- Add `ConsistentHashRing` for partition assignment
- Add `VectorClock` for versioning and conflict detection
- Add `QuorumPolicy` with configurable R, W, N values
- Add `HintedHandoff` for handling temporarily unavailable nodes
- Add `MerkleTree` for efficient replica comparison

### Solution Approach
Data is partitioned across nodes using a consistent hash ring. Each key hashes to a position on the ring and is stored on N consecutive nodes (replicas). Writes go to all N replicas; the operation succeeds when W acknowledge. Reads query N replicas and succeed when R respond consistently. Vector clocks track causality: each node increments its counter on writes, and conflicts (concurrent writes) are detected when neither vector clock dominates the other. Hinted handoff temporarily stores writes destined for failed nodes on their neighbors. Anti-entropy runs periodically, using Merkle trees to efficiently identify and repair divergent replicas.

### Key Classes to Add
```java
public class ConsistentHashRing {
    private TreeMap<Long, Node> ring;
    private int virtualNodes;
    
    public List<Node> getPreferenceList(String key, int n) {
        long hash = hash(key);
        // Return next N distinct physical nodes clockwise from hash
    }
    
    public void addNode(Node node) { ... }
    public void removeNode(Node node) { ... }
}

public class VectorClock {
    private Map<String, Long> clock; // nodeId -> counter
    
    public void increment(String nodeId) { ... }
    public Ordering compare(VectorClock other) { ... } // BEFORE, AFTER, CONCURRENT
    public VectorClock merge(VectorClock other) { ... }
}

public class QuorumPolicy {
    private int n; // replication factor
    private int r; // read quorum
    private int w; // write quorum
    
    public boolean isStronglyConsistent() { return r + w > n; }
}
```

---

## Variation 2: Sorted Set Operations (Score-Based)
**Learning Value:** Introduces sorted data structures, range queries, and score-based ranking operations.

### Additional Requirements
- Members with associated scores for sorting
- Range queries by score or rank
- Rank lookup for a given member
- Set operations (union, intersection) with score aggregation
- Efficient top-K and bottom-K retrieval

### Design Changes
- Add `SortedSet` data structure with score-based ordering
- Use skip list or balanced BST for O(log N) operations
- Add `RangeQuery` support (by score range or rank range)
- Add set operations (`zunionstore`, `zinterstore`) with aggregation
- Add `Leaderboard` convenience methods (rank, top-N)

### Solution Approach
A sorted set stores unique members each associated with a floating-point score. Internally, it uses a combination of a hash map (member -> score for O(1) lookup) and a skip list (score-ordered for O(log N) range operations). ZADD inserts or updates a member's score. ZRANK finds a member's position by traversing the skip list. ZRANGEBYSCORE retrieves all members with scores in [min, max]. Union and intersection operations combine multiple sorted sets with configurable score aggregation (SUM, MIN, MAX). This powers leaderboards, priority queues, and time-series indexes.

### Key Classes to Add
```java
public class SortedSet {
    private Map<String, Double> memberScores; // O(1) score lookup
    private TreeMap<Double, Set<String>> scoreMembers; // score-ordered
    
    public void zadd(String member, double score) { ... }
    public Double zscore(String member) { ... }
    public Long zrank(String member) { ... }
    public List<String> zrangeByScore(double min, double max) { ... }
    public List<String> zrangeByRank(int start, int stop) { ... }
    public void zrem(String member) { ... }
}

public class SortedSetOperations {
    public SortedSet zunionstore(List<SortedSet> sets, AggregateFunc func) { ... }
    public SortedSet zinterstore(List<SortedSet> sets, AggregateFunc func) { ... }
}

public enum AggregateFunc {
    SUM, MIN, MAX
}
```

---

## Variation 3: Pub-Sub on Key Changes
**Learning Value:** Practices event-driven notifications, key-change subscriptions, and reactive data patterns.

### Additional Requirements
- Watch specific keys for changes (set, delete, expire)
- Reactive notifications pushed to subscribers
- Keyspace events (notify on any operation in a key pattern)
- Pattern-based subscriptions (e.g., "user:*")
- Message delivery guarantees (at-least-once)

### Design Changes
- Add `PubSubManager` for subscription management
- Add `KeyspaceEvent` representing change events
- Add `Subscriber` interface for event callbacks
- Add `PatternMatcher` for wildcard/glob subscriptions
- Modify write operations to publish events after mutations

### Solution Approach
Every mutation operation (SET, DELETE, EXPIRE) emits a keyspace event after completing. Subscribers register interest in specific keys or key patterns (glob-style). The PubSubManager maintains a mapping of key patterns to subscriber lists. When an event fires, the manager matches it against all registered patterns and dispatches to matching subscribers. For reliability, events can be buffered in a per-subscriber queue. Pattern matching supports glob syntax (*, ?, []) for flexible subscriptions. This enables reactive architectures where cache invalidation, real-time UIs, and dependent computations trigger automatically on data changes.

### Key Classes to Add
```java
public class PubSubManager {
    private Map<String, List<Subscriber>> exactSubscriptions;
    private Map<String, List<Subscriber>> patternSubscriptions;
    
    public void subscribe(String keyOrPattern, Subscriber subscriber) { ... }
    public void unsubscribe(String keyOrPattern, Subscriber subscriber) { ... }
    public void publish(KeyspaceEvent event) {
        // Match against exact and pattern subscriptions, notify all
    }
}

public class KeyspaceEvent {
    private String key;
    private EventType type; // SET, DELETE, EXPIRE, RENAME
    private Object oldValue;
    private Object newValue;
    private Instant timestamp;
}

public interface Subscriber {
    void onEvent(KeyspaceEvent event);
}

public class PatternMatcher {
    public boolean matches(String pattern, String key) {
        // Glob matching: * = any chars, ? = one char
    }
}
```

---

## Variation 4: Lua Scripting
**Learning Value:** Explores trade-offs between flexibility and safety in embedded scripting and atomic operations.

### Additional Requirements
- Execute multi-operation scripts atomically
- Scripts run in isolation (no interleaving with other commands)
- Access to store read/write operations within scripts
- Script caching (store by SHA, execute by reference)
- Sandboxed execution (no file I/O, network, or infinite loops)

### Design Changes
- Add `ScriptEngine` for Lua/script parsing and execution
- Add `ScriptContext` providing store access within scripts
- Add `ScriptCache` storing compiled scripts by SHA hash
- Add `Sandbox` restricting available operations
- Add atomic execution guarantee (lock store during script)

### Solution Approach
The scripting engine allows users to submit multi-operation scripts that execute atomically. The store is locked (or commands are serialized) during script execution, preventing interleaving. Scripts have access to store operations (GET, SET, DELETE, etc.) via a context object. The sandbox prevents dangerous operations: no file system access, no network calls, and a timeout kills long-running scripts. Scripts are cached by their SHA-256 hash; clients can call EVALSHA with just the hash for repeated execution without retransmitting the script body. This enables complex atomic operations like compare-and-swap, conditional updates, and multi-key transactions.

### Key Classes to Add
```java
public class ScriptEngine {
    private ScriptCache cache;
    private Sandbox sandbox;
    
    public Object eval(String script, List<String> keys, List<String> args) {
        String sha = computeSHA(script);
        cache.store(sha, script);
        return executeAtomically(script, keys, args);
    }
    
    public Object evalSHA(String sha, List<String> keys, List<String> args) {
        String script = cache.get(sha);
        return executeAtomically(script, keys, args);
    }
}

public class ScriptContext {
    private InMemoryStore store;
    
    public String get(String key) { return store.get(key); }
    public void set(String key, String value) { store.set(key, value); }
    public boolean delete(String key) { return store.delete(key); }
}

public class Sandbox {
    private Duration timeout;
    private Set<String> allowedOperations;
    
    public void enforce(Runnable script) { ... }
}
```

---

## Variation 5: Cluster Mode
**Learning Value:** Deepens understanding of cluster topology, data migration, and horizontal scaling strategies.

### Additional Requirements
- Horizontal sharding across multiple nodes
- Hash slot assignment (e.g., 16384 slots across the cluster)
- Online resharding (move slots between nodes without downtime)
- Automatic failover with replica promotion
- Client-side slot routing (MOVED/ASK redirects)

### Design Changes
- Add `Cluster` managing multiple `ShardNode` instances
- Add `SlotMap` assigning hash slots to nodes
- Add `ReshardingManager` for live slot migration
- Add `FailoverController` for replica promotion on master failure
- Add `ClusterClient` handling redirects and slot discovery

### Solution Approach
The key space is divided into fixed hash slots (e.g., 16384). Each key maps to a slot via CRC16(key) % 16384. Slots are assigned to master nodes; each master has one or more replicas. Clients cache the slot-to-node mapping and route commands directly to the correct node. On misroute, the node responds with a MOVED redirect. Resharding moves slots between nodes: the target node imports keys for the slot while the source still serves them; during migration, the source responds with ASK redirects for migrated keys. Failover: when a master fails, its replicas coordinate (via Raft or gossip) to elect a new master, which takes over the master's slots.

### Key Classes to Add
```java
public class Cluster {
    private List<ShardNode> masters;
    private SlotMap slotMap;
    private FailoverController failover;
    
    public ShardNode getNodeForKey(String key) {
        int slot = CRC16(key) % 16384;
        return slotMap.getNode(slot);
    }
    
    public void reshard(int slot, ShardNode from, ShardNode to) { ... }
}

public class SlotMap {
    private ShardNode[] slots; // slots[i] = owning node
    
    public ShardNode getNode(int slot) { return slots[slot]; }
    public void assignSlot(int slot, ShardNode node) { slots[slot] = node; }
}

public class FailoverController {
    private Map<ShardNode, List<ShardNode>> replicas;
    
    public void onMasterFailure(ShardNode failedMaster) {
        ShardNode newMaster = electReplica(failedMaster);
        promoteToMaster(newMaster);
        updateSlotMap(failedMaster, newMaster);
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
