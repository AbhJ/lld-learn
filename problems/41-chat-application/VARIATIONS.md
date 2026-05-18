# Chat Application - Variations

## Variation 1: End-to-End Encryption
**Learning Value:** Teaches cryptographic protocol design, key exchange mechanisms, and secure message lifecycle.

### Additional Requirements
- Key exchange between users before messaging
- Message encryption/decryption at client side
- Forward secrecy (new keys per session)
- Key storage and rotation

### Design Changes
- Add `EncryptionService` with key management
- Add `KeyPair` and `SessionKey` classes
- Modify `Message` to store encrypted payload
- Add `KeyExchangeProtocol` (Diffie-Hellman)

### Solution Approach
Implement a Double Ratchet algorithm (simplified). Each user generates a key pair on registration. When two users start a chat, they perform a Diffie-Hellman key exchange to derive a shared secret. Each message is encrypted with a symmetric key derived from the shared secret. Forward secrecy is achieved by ratcheting the key after each message, so compromising one key doesn't reveal past messages. Store only encrypted blobs server-side.

### Key Classes to Add
```java
public class EncryptionService {
    private Map<String, KeyPair> userKeys;
    private Map<String, SessionKey> sessionKeys;

    public SessionKey performKeyExchange(String userId1, String userId2) { /* DH exchange */ }
    public byte[] encrypt(String message, SessionKey key) { /* AES encrypt */ }
    public String decrypt(byte[] ciphertext, SessionKey key) { /* AES decrypt */ }
    public void ratchetKey(String sessionId) { /* Derive next key */ }
}
```

---

## Variation 2: Message Reactions and Threads
**Learning Value:** Introduces threaded conversation structures, inline reactions, and nested message hierarchies.

### Additional Requirements
- React to any message with emoji
- Threaded replies under a parent message
- Thread notifications for participants
- Reaction count aggregation

### Design Changes
- Add `Reaction` class (emoji, user, timestamp)
- Add `Thread` class extending message chain
- Modify `Message` to have `parentMessageId` and `reactions` list
- Add `ThreadNotificationService`

### Solution Approach
Extend the `Message` class with a nullable `parentMessageId` field. When a user replies to a message, the reply stores the parent's ID, forming a thread. A `ThreadService` manages thread participants (anyone who replied). Reactions are stored as a list on each message with emoji type and user. Use the Observer pattern to notify thread participants when new replies arrive. Aggregate reactions by emoji for display.

### Key Classes to Add
```java
public class Reaction {
    private String emoji;
    private String userId;
    private LocalDateTime timestamp;
}

public class Thread {
    private String rootMessageId;
    private List<Message> replies;
    private Set<String> participants;

    public void addReply(Message message) { /* Add and notify */ }
    public void subscribe(String userId) { /* Add to participants */ }
}
```

---

## Variation 3: File/Media Sharing
**Learning Value:** Practices file upload handling, media transcoding, and chunked transfer for large payloads.

### Additional Requirements
- Upload files/images/videos within chat
- Thumbnail generation for media preview
- CDN-based delivery for large files
- File size limits and type validation

### Design Changes
- Add `FileUploadService` with chunked upload
- Add `MediaProcessor` for thumbnails and compression
- Add `CDNService` for storage and delivery URLs
- Modify `Message` to include `Attachment` type

### Solution Approach
When a user uploads a file, the `FileUploadService` validates size/type, then streams it in chunks to cloud storage. For images and videos, `MediaProcessor` generates thumbnails asynchronously. The file is registered with `CDNService` which returns a signed URL. The message stores an `Attachment` object with metadata (filename, size, type, thumbnail URL, download URL). Receivers get the thumbnail immediately and can download the full file on demand.

### Key Classes to Add
```java
public class FileUploadService {
    private CDNService cdn;
    private MediaProcessor processor;

    public Attachment upload(InputStream file, String filename, String mimeType) { /* Validate, store, process */ }
    public InputStream download(String attachmentId) { /* Signed URL fetch */ }
}

public class Attachment {
    private String id;
    private String filename;
    private long sizeBytes;
    private String mimeType;
    private String thumbnailUrl;
    private String downloadUrl;
}
```

---

## Variation 4: Search Across Messages
**Learning Value:** Explores trade-offs between search speed and storage cost in full-text indexing of conversations.

### Additional Requirements
- Full-text search across all messages
- Filters by sender, date range, channel
- Search result ranking by relevance
- Search within threads and attachments

### Design Changes
- Add `SearchService` with inverted index
- Add `SearchQuery` with filter criteria
- Add `SearchResult` with relevance scoring
- Integrate indexing into `MessageStore`

### Solution Approach
Build an inverted index that maps tokens to message IDs. When a message is stored, tokenize its content and update the index. `SearchService` accepts a `SearchQuery` with text and optional filters (sender, date range, channel). It looks up tokens in the index, applies filters, and scores results by TF-IDF or recency. Return paginated `SearchResult` objects. For scalability, this could be backed by Elasticsearch, but the LLD focuses on the indexing and query interface.

### Key Classes to Add
```java
public class SearchService {
    private Map<String, Set<String>> invertedIndex; // token -> messageIds
    private MessageStore messageStore;

    public void indexMessage(Message message) { /* Tokenize and index */ }
    public List<SearchResult> search(SearchQuery query) { /* Lookup, filter, rank */ }
}

public class SearchQuery {
    private String text;
    private String senderId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String channelId;
}
```

---

## Variation 5: Disappearing Messages
**Learning Value:** Deepens understanding of time-based content expiry, secure deletion, and ephemeral messaging patterns.

### Additional Requirements
- TTL (time-to-live) per message
- Auto-delete after TTL expires
- Read-once mode (delete after first read)
- Disable screenshots/forwarding for ephemeral messages

### Design Changes
- Add `EphemeralPolicy` class (TTL, read-once flag)
- Add `MessageCleanupService` with scheduled deletion
- Modify `Message` to include optional `EphemeralPolicy`
- Add `ReadReceipt` tracking for read-once logic

### Solution Approach
Extend `Message` with an optional `EphemeralPolicy` containing a TTL duration and a read-once flag. A background `MessageCleanupService` runs periodically, scanning for messages past their expiry (send time + TTL) and deleting them. For read-once messages, the system checks `ReadReceipt` — once the recipient reads it, it's marked for immediate deletion. The cleanup service uses a priority queue sorted by expiry time for efficiency.

### Key Classes to Add
```java
public class EphemeralPolicy {
    private Duration ttl;
    private boolean readOnce;
}

public class MessageCleanupService {
    private PriorityQueue<Message> expiryQueue;
    private MessageStore messageStore;

    public void scheduleCleanup(Message message) { /* Add to queue */ }
    public void runCleanup() { /* Delete expired messages */ }
    public void onMessageRead(String messageId, String readerId) { /* Handle read-once */ }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
