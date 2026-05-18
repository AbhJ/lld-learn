# Social Media Feed - Variations

## Variation 1: Stories (24hr Expiry)
**Learning Value:** Teaches TTL-based content expiry, ephemeral data patterns, and view-tracking systems.

### Additional Requirements
- Ephemeral content that auto-deletes after 24 hours
- View tracking (who viewed your story)
- Story reactions (emoji responses)
- Story highlights (pin past stories permanently)
- Sequential viewing (tap to advance)
- Close friends list for restricted stories

### Design Changes
- Add `Story` class with TTL-based auto-expiry
- Add `StoryViewer` tracker with view timestamps
- Add `StoryReaction` for emoji-based responses
- Add `StoryHighlight` for persisting selected stories
- Add `AudienceList` for close friends restrictions
- Add cleanup scheduler for expired content

### Solution Approach
A `Story` is created with a 24-hour TTL. The `StoryFeed` shows stories from followed users ordered by recency, with unviewed stories prioritized. Each view is recorded in a `StoryViewer` log (viewerId, timestamp) for the author to see. The `ExpiryScheduler` periodically scans and removes expired stories (or uses lazy deletion on access). Stories can be saved to `StoryHighlight` albums before expiry. The `AudienceList` (close friends) filters story visibility. Reactions create lightweight `StoryReaction` objects that notify the author without appearing publicly.

### Key Classes to Add
```java
public class Story {
    private String storyId;
    private User author;
    private String mediaUrl;
    private StoryType type; // IMAGE, VIDEO, TEXT
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<StoryViewer> viewers;
    private List<StoryReaction> reactions;
    private AudienceList audience; // null = everyone, or close friends only

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canView(User viewer) {
        if (audience == null) return true;
        return audience.contains(viewer);
    }

    public void recordView(User viewer) {
        if (!viewer.equals(author)) {
            viewers.add(new StoryViewer(viewer, LocalDateTime.now()));
        }
    }
}
```

---

## Variation 2: Live Streaming
**Learning Value:** Introduces real-time streaming architecture, concurrent viewer management, and live event handling.

### Additional Requirements
- Go-live notification to followers
- Real-time comment stream
- Viewer count tracking (current and peak)
- Stream health monitoring (bitrate, frame drops)
- Gift/donation system during live
- VOD (video on demand) conversion after stream ends

### Design Changes
- Add `LiveStream` class with real-time state management
- Add `CommentStream` for high-throughput real-time comments
- Add `ViewerTracker` for concurrent viewer counting
- Add `GoLiveNotifier` using Observer pattern for follower alerts
- Add `StreamHealth` monitor for quality metrics
- Add `VODConverter` for post-stream processing

### Solution Approach
When a user starts a `LiveStream`, the `GoLiveNotifier` pushes notifications to all followers. The `ViewerTracker` maintains a concurrent count using an atomic counter, tracking joins/leaves to compute peak viewership. The `CommentStream` is a bounded buffer that handles high-throughput messages with rate limiting per user. Comments are distributed to all connected viewers via pub-sub. The `StreamHealth` monitor checks metrics every few seconds and can suggest quality adjustments. When the stream ends, the `VODConverter` processes the recording for replay, and the stream metadata (duration, peak viewers, comments) is preserved.

### Key Classes to Add
```java
public class LiveStream {
    private String streamId;
    private User broadcaster;
    private StreamStatus status; // SCHEDULED, LIVE, ENDED
    private LocalDateTime startedAt;
    private ViewerTracker viewerTracker;
    private CommentStream commentStream;

    public void goLive() {
        this.status = StreamStatus.LIVE;
        this.startedAt = LocalDateTime.now();
        notifyFollowers();
    }

    public void endStream() {
        this.status = StreamStatus.ENDED;
        convertToVOD();
    }

    public StreamStats getStats() {
        return new StreamStats(viewerTracker.getCurrentCount(),
            viewerTracker.getPeakCount(), commentStream.getTotalComments());
    }
}
```

---

## Variation 3: Content Moderation
**Learning Value:** Practices classification pipelines, human-in-the-loop review, and appeals workflow design.

### Additional Requirements
- ML-based content flagging (text, image, video)
- User reporting system with categories
- Moderator queue with priority ranking
- Appeals workflow for removed content
- Automated actions (shadow ban, warning, removal)
- Policy versioning and audit trail

### Design Changes
- Add `ModerationEngine` with pluggable detectors
- Add `ContentReport` with category and severity
- Add `ModeratorQueue` priority queue for human review
- Add `AppealProcess` state machine for contested decisions
- Add `AutoModAction` for automated enforcement
- Add `ModerationPolicy` for configurable rules

### Solution Approach
When content is posted, the `ModerationEngine` runs it through a pipeline of detectors: `TextClassifier` (hate speech, spam, misinformation), `ImageAnalyzer` (nudity, violence), and `BehaviorAnalyzer` (coordinated inauthentic behavior). Each detector returns a confidence score. High-confidence violations trigger automatic removal; medium-confidence items enter the `ModeratorQueue` for human review. User reports add to the content's risk score. The `AppealProcess` allows users to contest decisions, routing to a senior moderator. All actions are logged in an immutable `ModerationAuditLog` for transparency and compliance.

### Key Classes to Add
```java
public class ModerationEngine {
    private List<ContentDetector> detectors;
    private ModerationPolicy policy;
    private ModeratorQueue reviewQueue;

    public ModerationResult evaluate(Content content) {
        double maxConfidence = 0;
        ViolationType violation = null;
        for (ContentDetector detector : detectors) {
            DetectionResult result = detector.analyze(content);
            if (result.getConfidence() > maxConfidence) {
                maxConfidence = result.getConfidence();
                violation = result.getViolationType();
            }
        }
        if (maxConfidence > policy.getAutoRemoveThreshold()) {
            return ModerationResult.autoRemove(violation);
        } else if (maxConfidence > policy.getReviewThreshold()) {
            reviewQueue.enqueue(content, violation, maxConfidence);
            return ModerationResult.pendingReview();
        }
        return ModerationResult.approved();
    }
}
```

---

## Variation 4: Hashtag/Trending
**Learning Value:** Explores trade-offs between recency and popularity in velocity-based trending algorithms.

### Additional Requirements
- Hashtag extraction and indexing
- Trending algorithm (velocity of usage, not just count)
- Explore page with personalized trending topics
- Hashtag following (subscribe to a hashtag)
- Geographic trending (trending in your area)
- Anti-gaming measures (prevent artificial trending)

### Design Changes
- Add `HashtagService` for extraction, indexing, and search
- Add `TrendingAlgorithm` with velocity-based scoring
- Add `ExplorePage` with personalization layer
- Add `HashtagSubscription` for follow notifications
- Add `TrendingAntiGaming` for fraud detection
- Add `GeoTrending` for location-based trends

### Solution Approach
The `HashtagService` extracts hashtags from posts and maintains an inverted index (hashtag -> list of posts). The `TrendingAlgorithm` scores hashtags not by total count but by velocity (rate of increase over a sliding window, e.g., last 1 hour vs. previous 4 hours). This surfaces emerging trends rather than permanently popular topics. The `ExplorePage` combines global trending with personalized topics based on the user's interests and follows. `GeoTrending` partitions hashtag counts by location. `TrendingAntiGaming` detects suspicious patterns (many new accounts using the same hashtag, coordinated timing) and excludes them from trending calculations.

### Key Classes to Add
```java
public class TrendingAlgorithm {
    private SlidingWindowCounter windowCounter; // hashtag counts per time window
    private int shortWindowMinutes = 60;
    private int longWindowMinutes = 240;

    public List<TrendingTopic> calculateTrending(String region, int limit) {
        Map<String, Double> scores = new HashMap<>();
        for (String hashtag : windowCounter.getActiveHashtags(region)) {
            double recentCount = windowCounter.getCount(hashtag, shortWindowMinutes);
            double baselineCount = windowCounter.getCount(hashtag, longWindowMinutes) / 4.0;
            double velocity = (baselineCount == 0) ? recentCount : recentCount / baselineCount;
            scores.put(hashtag, velocity);
        }
        return scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(limit)
            .map(e -> new TrendingTopic(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
}
```

---

## Variation 5: Reels/Short Video
**Learning Value:** Deepens understanding of recommendation engines, engagement scoring, and content distribution systems.

### Additional Requirements
- Video upload with transcoding to multiple resolutions
- Recommendation algorithm based on engagement
- Engagement scoring (watch time, replays, shares, likes)
- Creator monetization (ad revenue share, gifts)
- Duets/stitches (reference other videos)
- Sound/music library for video creation

### Design Changes
- Add `VideoService` for upload, transcode, and CDN distribution
- Add `RecommendationEngine` with collaborative filtering
- Add `EngagementScorer` tracking watch duration, completion rate
- Add `MonetizationService` for creator earnings
- Add `SoundLibrary` for music/audio clips
- Add `VideoRelation` for duets and stitches

### Solution Approach
Videos are uploaded to `VideoService` which triggers transcoding into multiple resolutions (360p, 720p, 1080p) and stores them on CDN. The `RecommendationEngine` uses a two-phase approach: candidate generation (collaborative filtering based on similar users' watch history) and ranking (scored by predicted engagement). `EngagementScorer` weights signals: watch completion (40%), replay (25%), share (20%), like (10%), comment (5%). The recommendation model updates in near-real-time as new engagement data flows in. Videos with high early engagement get boosted distribution ("viral potential" detection).

### Key Classes to Add
```java
public class RecommendationEngine {
    private UserInterestModel interestModel;
    private EngagementScorer scorer;
    private int candidatePoolSize = 500;
    private int resultSize = 20;

    public List<Video> getRecommendations(User user) {
        // Phase 1: Candidate generation
        List<Video> candidates = interestModel.getCandidates(user, candidatePoolSize);

        // Phase 2: Ranking by predicted engagement
        return candidates.stream()
            .map(v -> new ScoredVideo(v, scorer.predictEngagement(user, v)))
            .sorted(Comparator.comparingDouble(ScoredVideo::getScore).reversed())
            .limit(resultSize)
            .map(ScoredVideo::getVideo)
            .collect(Collectors.toList());
    }
}

public class EngagementScorer {
    public double predictEngagement(User user, Video video) {
        double watchTimeScore = predictWatchCompletion(user, video) * 0.40;
        double replayScore = predictReplay(user, video) * 0.25;
        double shareScore = predictShare(user, video) * 0.20;
        double likeScore = predictLike(user, video) * 0.10;
        double commentScore = predictComment(user, video) * 0.05;
        return watchTimeScore + replayScore + shareScore + likeScore + commentScore;
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
