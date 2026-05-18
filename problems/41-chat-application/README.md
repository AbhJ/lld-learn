# Chat Application


## Problem Statement
Design a chat application that supports direct one-to-one chats and group chats. Users register, set online status, send text/image/file messages, and receive notifications for new messages. The system tracks typing indicators and full message history per room.

## Requirements

### Functional Requirements
- Register users and update online status
- Create direct and group chat rooms
- Send messages of multiple types (text, image, file)
- Persist and retrieve message history per room
- Typing indicators (start/stop)
- Notify online participants of new messages

### Non-functional Requirements
- Notifications skip offline users
- Per-room history retrieval is read-only and ordered by time

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Facade | ChatService | Single API over users, rooms, messages, notifications |
| Observer | MessageListener / ConsoleMessageListener | NotificationManager fans out new-message events to per-user listeners |
| Repository | MessageStore | Stores and retrieves messages per room |
| Polymorphism | ChatRoom -> DirectChat / GroupChat | Different room types share a common interface |

## Folder Structure

```
41-chat-application/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← User, Message, OnlineStatus, TypingIndicator
│   ├── service/      ← ChatService, ChatRoom, MessageStore, NotificationManager
│   └── Main.java
└── optimized/
    ├── model/        ← User, Message, OnlineStatus, TypingIndicator
    ├── service/      ← ChatService, ChatRoom, MessageStore, NotificationManager
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/41-chat-application/naive
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/41-chat-application/optimized
mkdir -p out && javac -d out model/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Message search | Linear scan through ArrayList | TreeMap index by timestamp + HashMap by sender for O(log n) range and O(1) sender queries |
| Notification delivery | Synchronous, blocks sender | Async delivery with CompletableFuture |
| Online status check | Traverse user objects | ConcurrentHashMap for O(1) thread-safe status lookup |
| Participant lookup | ArrayList.contains O(n) | HashSet.contains O(1) |
| Typing indicator | HashMap (not thread-safe) | ConcurrentHashMap (thread-safe, O(1)) |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Two users sending messages to same group chat simultaneously — message ordering inconsistent, messages lost.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ConcurrentLinkedQueue | ChatRoom.messageBuffer | Lock-free message buffer allows concurrent sends without blocking |
| AtomicLong | ChatRoom.sequenceGenerator | Total ordering of messages via atomic sequence numbers |
| CountDownLatch | Main (startLatch) | Ensures all 10 sender threads start simultaneously for max contention |
| Immutable Message | Message class | Thread-safe by construction — no mutable shared state |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
