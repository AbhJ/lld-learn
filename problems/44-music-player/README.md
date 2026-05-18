# Music Player


## Problem Statement
Design a music player that loads playlists, plays/pauses/stops songs, navigates next/previous, supports a separate user queue, shuffles, and repeats. The player exposes its state changes to UI observers.

The naive variant uses an iterator over an ArrayList. The optimized variant uses a doubly-linked song list for O(1) skip and remove.

## Requirements

### Functional Requirements
- Load a playlist and play/pause/stop
- Next/previous navigation
- User queue prepended ahead of playlist order
- Shuffle (random or weighted)
- Repeat modes: NONE, ONE, ALL
- Notify observers (UI) on state changes

### Non-functional Requirements
- O(1) next/previous (optimized)
- Player state transitions are well-defined (Playing/Paused/Stopped)

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| State | PlayerState (Playing, Paused, Stopped) | Behavior of play/pause/stop depends on current state |
| Strategy | ShuffleStrategy (Random, Weighted, Fisher-Yates) | Pluggable shuffle algorithms |
| Observer | PlayerObserver / ConsolePlayerObserver | UI reacts to player events |
| Iterator | PlaylistIterator | Encapsulates traversal under repeat/shuffle (naive) |

## Folder Structure

```
44-music-player/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Song, Playlist, Queue, RepeatMode
│   ├── service/      ← MusicPlayer, PlaylistIterator, PlayerObserver
│   ├── strategy/     ← ShuffleStrategy (Random, Weighted)
│   ├── state/        ← PlayerState (Playing, Paused, Stopped)
│   └── Main.java
└── optimized/
    ├── model/        ← Song, SongNode, Playlist (doubly-linked), Queue, RepeatMode
    ├── service/      ← MusicPlayer, PlayerObserver
    ├── strategy/     ← ShuffleStrategy (Fisher-Yates, Weighted)
    ├── state/        ← PlayerState
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/44-music-player/naive
mkdir -p out && javac -d out model/*.java strategy/*.java state/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/44-music-player/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java state/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Next/Previous | Array index arithmetic | O(1) doubly-linked list pointer traversal |
| Shuffle | Collections.shuffle (may reallocate) | Fisher-Yates in-place O(n) with n-1 swaps |
| Repeat ALL | Iterator index wraps manually | Circular linked list (tail->head) |
| Play queue | Separate concern | Separate queue that takes priority over playlist |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** User presses skip while current track's "onComplete" callback fires — player jumps ahead twice.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicReference<Song> | MusicPlayer.currentSong | Always reflects the true current track for identity checks |
| ReentrantLock | MusicPlayer.transitionLock | Skip, complete, and pause are mutually exclusive state transitions |
| Identity check in onComplete | MusicPlayer.onComplete() | Only advances if expected song is still current — prevents double-advance |
| AtomicInteger | MusicPlayer.currentIndex | Tracks position atomically, verified against advance count |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
