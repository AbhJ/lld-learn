# Social Media Feed


## Problem Statement
Design a social media feed system similar to Twitter/Instagram. Users can create posts, follow other users, like and comment on posts. The system generates personalized feeds using configurable ranking strategies.

The feed supports both chronological ordering and engagement-based ranking. Posts can be iterated through with pagination support. Users receive notifications when someone they follow creates a new post, or when someone interacts with their content.

The system demonstrates clean separation between social graph management, content creation, and feed generation.

## Requirements
### Functional Requirements
- User registration and profiles
- Create text posts
- Follow/unfollow users
- Like and comment on posts
- Generate user feed from followed users' posts
- Feed ranking strategies (chronological, engagement-based)
- Paginated feed iteration
- Activity notifications

### Non-functional Requirements
- Efficient feed generation
- Scalable follower management
- Extensible ranking algorithms
- Clean notification mechanism

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Observer | New post notification | Notify followers of new content |
| Strategy | Feed ranking | Pluggable feed sorting algorithms |
| Iterator | Feed pagination | Lazy page-by-page feed access |
| Decorator | PostComponent → PostDecorator → TrendingBadge / VerifiedBadge / MetadataDecorator | Stack runtime post enrichments without modifying Post |

## Folder Structure
```
15-social-media-feed/
├── naive/
│   ├── model/      -> User, Post, Comment, Notification
│   ├── service/    -> Feed, FeedIterator, FollowManager, SocialNetwork
│   ├── strategy/   -> FeedStrategy (Chronological, Ranked)
│   └── Main.java
└── optimized/
    ├── model/
    ├── service/    -> FeedCache (fan-out-on-write), FollowManager, SocialNetwork
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

### Naive vs Optimized
| Operation | Naive | Optimized |
|-----------|-------|-----------|
| Generate feed | O(p) scan all posts every time | O(1) read pre-computed feed |
| New post publish | O(1) add to list | O(f) fan-out to f followers |
| Pagination | SubList (copies) | Cursor-based O(pageSize) |
| Unfollow cleanup | Rebuild feed next read | Immediate removal from cache |

---

## Class Diagram (Text)
```
SocialNetwork (Facade)
 ├── User
 ├── Post
 ├── Comment
 ├── Feed
 │    └── FeedStrategy (Interface)
 │         ├── ChronologicalFeed
 │         └── RankedFeed
 ├── FeedIterator
 ├── Notification
 └── FollowManager
```

## How to Compile and Run
```bash
cd problems/15-social-media-feed
mkdir -p out
javac -d out *.java
java -cp out Main
```

## Expected Output
```
=== Social Media Feed Demo ===
Alice posted: "Hello World! My first post!"
Bob followed Alice.
Bob's feed (chronological): [Alice's post, ...]
Alice's post liked by Bob (3 likes total).
```

## Key Design Decisions
- Observer pattern for follower notifications avoids polling
- Strategy pattern makes feed ranking easily swappable
- Iterator pattern enables efficient pagination without loading all posts
- FollowManager centralizes social graph logic

## Interview Tips
- Discuss feed generation: push model vs pull model
- Explain how ranked feed could use ML signals in production
- Talk about pagination and cursor-based iteration
- Mention fan-out on write vs fan-out on read trade-offs

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** User posts while another user's feed is being generated — inconsistent feed state, missing or duplicate posts.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| CopyOnWriteArrayList | User.followers/following | Safe iteration during follow/unfollow modification |
| ConcurrentLinkedDeque | User.posts | Safe concurrent append + read (no ConcurrentModificationException) |
| ConcurrentHashMap | FeedService.users | Thread-safe user registry |
| AtomicLong | Post ID generation | Unique post IDs across concurrent writers |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
