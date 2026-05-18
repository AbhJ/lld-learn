# Music Player - Variations

## Variation 1: Recommendation Engine
**Learning Value:** Teaches collaborative filtering, listening history analysis, and personalized content discovery.

### Additional Requirements
- Collaborative filtering based on user similarity
- Listening history analysis
- Discover weekly / personalized playlists
- Genre and mood-based recommendations

### Design Changes
- Add `RecommendationEngine` with multiple strategies
- Add `ListeningHistory` per user
- Add `UserProfile` with music preferences
- Add `SimilarityCalculator` for collaborative filtering

### Solution Approach
Track every play event in `ListeningHistory` (song, duration, skip, timestamp). Build a `UserProfile` capturing genre preferences, favorite artists, and listening patterns. The `RecommendationEngine` uses collaborative filtering: find users with similar listening patterns and recommend songs they liked that the current user hasn't heard. Combine with content-based filtering (similar genres/artists). Generate "Discover Weekly" by running the engine periodically and caching results.

### Key Classes to Add
```java
public class RecommendationEngine {
    private ListeningHistory history;
    private SimilarityCalculator similarity;

    public List<Song> getRecommendations(String userId, int count) {
        // Find similar users, get their top songs, filter already-heard
    }

    public Playlist generateDiscoverWeekly(String userId) {
        // Blend collaborative + content-based filtering
    }
}

public class ListeningHistory {
    private String userId;
    private List<PlayEvent> events;

    public Map<String, Double> getGenreAffinity() { /* Compute from history */ }
    public List<Song> getTopSongs(int period) { /* Most played */ }
}
```

---

## Variation 2: Social Features
**Learning Value:** Introduces social graph integration, activity feeds, and shared playlist collaboration.

### Additional Requirements
- Share songs/playlists with friends
- Collaborative playlists (multiple editors)
- Friend activity feed (what friends are listening to)
- Social listening (listen together remotely)

### Design Changes
- Add `SocialService` for friend connections
- Add `CollaborativePlaylist` with permissions
- Add `ActivityFeed` publishing play events
- Add `ListeningSession` for shared playback

### Solution Approach
Users can follow/friend each other via `SocialService`. When a user plays a song, an event is published to their friends' `ActivityFeed` (privacy settings permitting). `CollaborativePlaylist` extends `Playlist` with a list of editors and add/remove permissions. For social listening, a `ListeningSession` synchronizes playback state across participants — one user controls play/pause/skip and all participants' players stay in sync via real-time events.

### Key Classes to Add
```java
public class SocialService {
    private Map<String, Set<String>> friendGraph;

    public void follow(String userId, String targetId) { /* Add connection */ }
    public List<ActivityEvent> getFriendActivity(String userId) { /* Aggregate feed */ }
    public void shareContent(String fromUser, String toUser, Shareable content) { /* Send */ }
}

public class CollaborativePlaylist extends Playlist {
    private Set<String> editors;
    public void addEditor(String userId) { /* Grant access */ }
    public boolean canEdit(String userId) { /* Check permission */ }
}
```

---

## Variation 3: Offline Mode
**Learning Value:** Practices offline-first architecture, sync conflict resolution, and download management.

### Additional Requirements
- Download songs for offline playback
- Storage quota management
- Sync state when back online
- Download quality settings

### Design Changes
- Add `DownloadManager` for download queue
- Add `OfflineStorage` with quota tracking
- Add `SyncService` for online/offline transitions
- Add `DownloadQuality` enum (LOW, MEDIUM, HIGH)

### Solution Approach
The `DownloadManager` accepts download requests, queues them, and downloads audio files to local storage. `OfflineStorage` tracks downloaded files and enforces a storage quota (e.g., 10GB). When the quota is near, suggest removing least-recently-played downloads. The `SyncService` detects connectivity changes — when offline, the player serves from local cache; when online again, it syncs play history and new downloads. Quality settings affect file size.

### Key Classes to Add
```java
public class DownloadManager {
    private Queue<DownloadTask> downloadQueue;
    private OfflineStorage storage;

    public void download(Song song, DownloadQuality quality) { /* Queue download */ }
    public void cancelDownload(String songId) { /* Remove from queue */ }
    public double getProgress(String songId) { /* 0.0 to 1.0 */ }
}

public class OfflineStorage {
    private long quotaBytes;
    private long usedBytes;
    private Map<String, File> downloadedFiles;

    public boolean hasSpace(long fileSize) { /* Check quota */ }
    public List<Song> getDownloadedSongs() { /* List offline content */ }
    public void cleanup(long bytesNeeded) { /* Remove LRU files */ }
}
```

---

## Variation 4: Equalizer / Audio Effects
**Learning Value:** Explores trade-offs between audio quality and processing overhead in real-time DSP chain design.

### Additional Requirements
- Parametric equalizer with frequency bands
- Preset EQ profiles (Rock, Pop, Classical, etc.)
- Crossfade between tracks
- Gapless playback

### Design Changes
- Add `Equalizer` with frequency bands and gains
- Add `EQPreset` for saved configurations
- Add `CrossfadeEngine` for smooth transitions
- Add `AudioPipeline` processing chain

### Solution Approach
The `AudioPipeline` processes audio through a chain of effects before output. The `Equalizer` applies gain adjustments to frequency bands (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz). Users can adjust manually or select presets. `CrossfadeEngine` handles transitions: as one song ends, it fades out while fading in the next song, overlapping for the configured duration. Gapless playback pre-buffers the next track and eliminates the gap by decoding ahead.

### Key Classes to Add
```java
public class Equalizer {
    private Map<Integer, Double> bands; // frequency -> gain in dB
    private String presetName;

    public void setBandGain(int frequency, double gainDb) { /* Adjust */ }
    public void applyPreset(EQPreset preset) { /* Load all bands */ }
    public double[] process(double[] audioSamples) { /* Apply EQ */ }
}

public class CrossfadeEngine {
    private int crossfadeDurationMs;

    public double[] crossfade(double[] endingSong, double[] startingSong) {
        // Linear or equal-power crossfade
    }
}
```

---

## Variation 5: Podcast Support
**Learning Value:** Deepens understanding of heterogeneous content modeling, episode tracking, and subscription feed management.

### Additional Requirements
- Podcast episodes with progress tracking
- Variable playback speed (0.5x to 3x)
- Chapter markers and navigation
- Auto-download new episodes

### Design Changes
- Add `Podcast` and `Episode` classes
- Add `ProgressTracker` persisting playback position
- Add `PlaybackSpeed` control
- Add `Chapter` navigation within episodes
- Add `PodcastSubscription` with auto-download

### Solution Approach
`Podcast` represents a show with a list of `Episode` objects. Each episode tracks playback progress (timestamp where user stopped). `PlaybackSpeed` allows tempo adjustment without pitch change. `Chapter` markers let users jump to sections within long episodes. `PodcastSubscription` monitors RSS feeds for new episodes and auto-downloads based on user preferences (WiFi only, storage limit). The player UI adapts for podcast mode (skip 30s, speed control visible).

### Key Classes to Add
```java
public class Episode {
    private String id;
    private String podcastId;
    private String title;
    private Duration duration;
    private long progressMs; // resume position
    private List<Chapter> chapters;
    private boolean downloaded;
}

public class PodcastSubscription {
    private String podcastId;
    private boolean autoDownload;
    private int keepEpisodeCount;

    public void checkForNewEpisodes() { /* Poll RSS, download if auto */ }
}

public class PlaybackSpeedController {
    private double speed; // 0.5 to 3.0
    public double[] adjustSpeed(double[] samples, double speed) { /* Time-stretch */ }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
