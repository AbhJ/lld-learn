# In-Memory File System - Variations

## Variation 1: Distributed File System (Chunk-Based)
**Learning Value:** Teaches data replication, chunk-based storage, and fault tolerance in distributed architectures.

### Additional Requirements
- Files split into fixed-size chunks (64MB/128MB)
- Chunk replication across multiple data nodes (default 3x)
- Master/NameNode tracks chunk locations and metadata
- Heartbeat mechanism for node health monitoring
- Rack-aware placement for fault tolerance

### Design Changes
- Add `ChunkServer` class to manage local chunk storage
- Add `MasterNode` class for namespace and chunk-location mapping
- Add `ReplicationManager` for maintaining replication factor
- Introduce `HeartbeatService` for liveness detection
- Use `ConsistentHashing` for chunk distribution

### Solution Approach
The master node maintains the file namespace and maps each file to an ordered list of chunk handles. Each chunk is stored on multiple chunk servers for redundancy. Clients contact the master for metadata, then read/write directly to chunk servers. The master monitors chunk server health via periodic heartbeats and re-replicates chunks when servers fail. Write operations use a lease mechanism where one replica is designated primary to serialize mutations. Large files benefit from sequential reads, so chunks are typically 64MB+.

### Key Classes to Add
```java
public class ChunkServer {
    private String serverId;
    private Map<String, byte[]> chunks; // chunkId -> data
    
    public byte[] readChunk(String chunkId) { ... }
    public void writeChunk(String chunkId, byte[] data) { ... }
    public void sendHeartbeat(MasterNode master) { ... }
}

public class MasterNode {
    private Map<String, FileMetadata> namespace;
    private Map<String, List<String>> chunkLocations; // chunkId -> serverIds
    
    public List<String> getChunkServers(String chunkId) { ... }
    public void handleHeartbeat(String serverId) { ... }
    public void reReplicate(String chunkId) { ... }
}
```

---

## Variation 2: Versioned File System
**Learning Value:** Introduces version history management, snapshot isolation, and space-efficient delta storage.

### Additional Requirements
- Track all changes to files with immutable snapshots
- Diff between any two versions of a file
- Branch and merge support (like Git)
- Space-efficient storage using deltas or content-addressable storage
- Rollback to any previous version

### Design Changes
- Add `Version` class with content hash and parent pointer
- Add `VersionTree` for branch/merge history (DAG structure)
- Add `DiffEngine` to compute differences between versions
- Modify `File` to reference version history chain
- Add `ContentStore` using content-addressable storage (SHA-based)

### Solution Approach
Each file write creates a new immutable version object rather than modifying in place. Versions form a directed acyclic graph where each node points to its parent(s). Content is stored by hash (content-addressable), so identical content is stored only once. Branches are simply named pointers to version nodes. Merging combines two branches by creating a version with two parents, using a three-way merge algorithm. Diffs are computed by comparing the content trees of two versions.

### Key Classes to Add
```java
public class Version {
    private String hash;
    private String contentHash;
    private List<String> parentHashes;
    private long timestamp;
    private String author;
    
    public String computeHash() { ... }
}

public class VersionedFile extends File {
    private List<Version> history;
    private Map<String, Version> branches; // branchName -> headVersion
    
    public Version commit(String content, String message) { ... }
    public String diff(Version v1, Version v2) { ... }
    public Version merge(String branch1, String branch2) { ... }
}
```

---

## Variation 3: Access Control Lists (ACL)
**Learning Value:** Practices permission modeling, inheritance hierarchies, and fine-grained access control.

### Additional Requirements
- User, group, and role-based permissions
- Permission inheritance from parent directories
- Support for read, write, execute, and admin permissions
- Sudo/elevation mechanism for temporary privilege escalation
- Audit logging of permission changes

### Design Changes
- Add `ACL` class attached to each file/directory node
- Add `Permission` enum (READ, WRITE, EXECUTE, ADMIN)
- Add `Principal` hierarchy (User, Group, Role)
- Modify `FileSystem` operations to check permissions before access
- Add `AuditLog` for tracking permission changes

### Solution Approach
Each file system node carries an ACL that maps principals (users/groups/roles) to permission sets. When checking access, the system resolves the effective permissions by walking up the directory tree and combining inherited ACLs with explicit ones (explicit deny overrides inherited allow). Groups allow batch permission assignment. Roles provide temporary elevated access. The system uses a `SecurityContext` that tracks the current user and their group memberships. Every access check is O(depth) in the directory tree due to inheritance resolution.

### Key Classes to Add
```java
public class ACL {
    private Map<Principal, EnumSet<Permission>> entries;
    private boolean inheritFromParent;
    
    public boolean hasPermission(Principal principal, Permission perm) { ... }
    public void grant(Principal principal, Permission perm) { ... }
    public void revoke(Principal principal, Permission perm) { ... }
}

public class SecurityContext {
    private User currentUser;
    private Set<Group> groups;
    private Role elevatedRole;
    
    public boolean checkAccess(FileSystemNode node, Permission perm) { ... }
    public void elevate(Role role, Duration duration) { ... }
}
```

---

## Variation 4: Encrypted File System
**Learning Value:** Explores trade-offs between security and performance in encryption-at-rest and key management.

### Additional Requirements
- Per-file encryption with unique keys
- Key management with master key hierarchy
- Secure deletion (overwrite + key destruction)
- Transparent encryption/decryption during read/write
- Support for key rotation without re-encrypting all data

### Design Changes
- Add `EncryptionService` with pluggable cipher algorithms
- Add `KeyManager` for key hierarchy (master key -> file keys)
- Add `EncryptedFile` wrapper that auto-encrypts/decrypts
- Modify `File.read()`/`write()` to pass through encryption layer
- Add `SecureDelete` that overwrites data before removing

### Solution Approach
The system uses a two-tier key hierarchy: a master key encrypts individual file keys, and each file key encrypts that file's content. This allows key rotation at the master level without re-encrypting all files. Read/write operations transparently encrypt/decrypt through an encryption layer. Secure deletion destroys the file key first (making content unrecoverable) then overwrites the ciphertext. Keys are stored in a protected vault separate from file data. The system supports multiple cipher algorithms (AES-256-GCM recommended) via a strategy pattern.

### Key Classes to Add
```java
public class KeyManager {
    private SecretKey masterKey;
    private Map<String, SecretKey> fileKeys; // fileId -> encrypted file key
    
    public SecretKey getFileKey(String fileId) { ... }
    public SecretKey rotateFileKey(String fileId) { ... }
    public void destroyKey(String fileId) { ... }
}

public class EncryptionService {
    private CipherStrategy cipher;
    
    public byte[] encrypt(byte[] plaintext, SecretKey key) { ... }
    public byte[] decrypt(byte[] ciphertext, SecretKey key) { ... }
    public void secureDelete(String fileId) { ... }
}
```

---

## Variation 5: Cloud Storage (Object-Based)
**Learning Value:** Deepens understanding of object storage abstractions, eventual consistency, and metadata management.

### Additional Requirements
- Bucket-based flat namespace (no true directories)
- Object storage with metadata and tagging
- Pre-signed URLs for temporary access
- Multipart upload for large objects
- Eventual consistency model with strong read-after-write for new objects

### Design Changes
- Replace tree structure with flat `Bucket` + prefix-based listing
- Add `S3Object` with content, metadata, and version ID
- Add `PreSignedURLGenerator` for time-limited access tokens
- Add `MultipartUpload` state machine for chunked uploads
- Add `StorageTier` enum (STANDARD, INFREQUENT, GLACIER)

### Solution Approach
Objects are stored in buckets with flat keys (the "/" in keys is just convention, not real directories). Each object has content, user-defined metadata, and system metadata. Pre-signed URLs embed an HMAC signature with an expiration time, allowing unauthenticated access within the time window. Multipart uploads split large objects into parts that can be uploaded independently and in parallel; a complete/abort API finalizes or cancels the upload. List operations support prefix and delimiter parameters to simulate directory browsing. Storage classes determine durability/availability/cost tradeoffs.

### Key Classes to Add
```java
public class Bucket {
    private String name;
    private Map<String, StorageObject> objects;
    
    public StorageObject getObject(String key) { ... }
    public void putObject(String key, byte[] content, Map<String,String> metadata) { ... }
    public List<String> listByPrefix(String prefix, String delimiter) { ... }
}

public class MultipartUpload {
    private String uploadId;
    private String key;
    private Map<Integer, byte[]> parts;
    private UploadState state; // INITIATED, IN_PROGRESS, COMPLETED, ABORTED
    
    public String initiate(String key) { ... }
    public void uploadPart(int partNumber, byte[] data) { ... }
    public StorageObject complete() { ... }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
