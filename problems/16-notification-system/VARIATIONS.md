# Notification System - Variations

## Variation 1: Preference Management
**Learning Value:** Teaches user preference modeling, opt-in/opt-out management, and channel routing logic.

### Additional Requirements
- Per-channel opt-in/out (email yes, SMS no)
- Per-category preferences (marketing no, transactional yes)
- Quiet hours (no notifications between 10 PM - 8 AM)
- Digest mode (batch notifications into daily summary)
- Frequency caps per category
- Global do-not-disturb mode

### Design Changes
- Add `UserPreferences` with channel and category granularity
- Add `QuietHoursPolicy` checking time-of-day constraints
- Add `PreferenceFilter` in the notification pipeline
- Add `DigestCollector` that holds notifications for batch delivery
- Add `FrequencyCap` per category tracking

### Solution Approach
Before sending any notification, the system checks `UserPreferences` through a `PreferenceFilter`. Preferences are stored hierarchically: global -> category -> specific notification type. The filter checks: (1) Is the channel enabled for this user? (2) Is this category enabled? (3) Is it within quiet hours? (4) Has the frequency cap been reached? If the notification is suppressed due to quiet hours or digest mode, it's queued in the `DigestCollector`. A scheduled job processes the digest at the user's configured time (e.g., 9 AM daily), bundling all held notifications into a single summary email.

### Key Classes to Add
```java
public class UserPreferences {
    private String userId;
    private Map<Channel, Boolean> channelEnabled;
    private Map<Category, ChannelPreference> categoryPreferences;
    private QuietHours quietHours;
    private DigestConfig digestConfig;
    private boolean globalDND;

    public boolean canSend(Notification notification) {
        if (globalDND) return false;
        if (!channelEnabled.getOrDefault(notification.getChannel(), true)) return false;
        if (quietHours != null && quietHours.isActive()) return false;
        ChannelPreference catPref = categoryPreferences.get(notification.getCategory());
        return catPref == null || catPref.isAllowed(notification.getChannel());
    }
}

public class QuietHours {
    private LocalTime start; // e.g., 22:00
    private LocalTime end;   // e.g., 08:00
    private ZoneId timezone;

    public boolean isActive() {
        LocalTime now = LocalTime.now(timezone);
        if (start.isBefore(end)) return now.isAfter(start) && now.isBefore(end);
        return now.isAfter(start) || now.isBefore(end); // spans midnight
    }
}
```

---

## Variation 2: A/B Testing Notifications
**Learning Value:** Introduces experimental design, variant selection, and metrics-driven notification optimization.

### Additional Requirements
- Multiple notification variants (different copy, subject lines)
- Random or stratified user assignment to variants
- Metric tracking (open rate, click-through rate, conversion)
- Statistical significance calculation
- Auto-winner selection after sufficient data
- Holdout groups (control group receives nothing)

### Design Changes
- Add `ABTest` class with variant definitions and allocation
- Add `VariantAllocator` using consistent hashing for user assignment
- Add `MetricTracker` for open/click/conversion events
- Add `StatisticalAnalyzer` for significance testing
- Modify `NotificationService` to route through A/B test layer

### Solution Approach
An `ABTest` defines 2+ variants of a notification (different subject, body, timing, or channel). The `VariantAllocator` assigns users to variants using consistent hashing (ensuring the same user always sees the same variant). When sending, the system resolves which variant to deliver. The `MetricTracker` records events (sent, opened, clicked, converted) per variant. After reaching minimum sample size, the `StatisticalAnalyzer` performs chi-squared or z-test to determine if differences are statistically significant (p < 0.05). The winning variant can be auto-promoted to 100% traffic.

### Key Classes to Add
```java
public class ABTest {
    private String testId;
    private String name;
    private List<Variant> variants; // includes control
    private TestStatus status; // DRAFT, RUNNING, COMPLETED
    private int minimumSampleSize;
    private VariantAllocator allocator;

    public Variant getVariantForUser(String userId) {
        return allocator.allocate(userId, variants);
    }

    public TestResult analyzeResults() {
        Map<Variant, Metrics> results = new HashMap<>();
        for (Variant v : variants) {
            results.put(v, MetricTracker.getMetrics(testId, v.getId()));
        }
        return StatisticalAnalyzer.analyze(results);
    }
}

public class Variant {
    private String variantId;
    private String name; // "Control", "Variant A", "Variant B"
    private double trafficPercent; // 0.33 for 3 variants
    private NotificationTemplate template;
    private Metrics metrics; // sent, opened, clicked, converted
}
```

---

## Variation 3: Localization
**Learning Value:** Practices internationalization patterns, template localization, and locale-aware formatting.

### Additional Requirements
- Multi-language notification templates
- Timezone-aware delivery scheduling
- RTL (right-to-left) language support
- Locale-specific formatting (dates, numbers, currency)
- Fallback language chain (fr-CA -> fr -> en)
- Dynamic language detection from user profile

### Design Changes
- Add `LocalizationService` for template resolution by locale
- Add `LocaleTemplate` with language-specific content
- Add `TimezoneScheduler` for delivery at local appropriate time
- Add `FormatProvider` for locale-specific formatting
- Modify `Template` to support locale keys instead of hardcoded text

### Solution Approach
Templates are stored with locale keys (e.g., "order.shipped.subject") rather than hardcoded text. The `LocalizationService` resolves these keys using a fallback chain: user's specific locale (fr-CA) -> language (fr) -> default (en). The `TimezoneScheduler` converts the intended delivery time to the user's local timezone, ensuring the notification arrives at an appropriate local time. The `FormatProvider` handles locale-specific formatting (e.g., dates as "13/05/2026" vs "05/13/2026", currency as "1.234,56 EUR" vs "$1,234.56"). RTL support adds directional markers to text content.

### Key Classes to Add
```java
public class LocalizationService {
    private Map<String, Map<Locale, String>> translations; // key -> locale -> text
    private List<Locale> fallbackChain;

    public String resolve(String key, Locale userLocale, Map<String, String> variables) {
        String template = findTranslation(key, userLocale);
        return interpolate(template, variables, userLocale);
    }

    private String findTranslation(String key, Locale locale) {
        // Try exact match, then language-only, then default
        Map<Locale, String> options = translations.get(key);
        if (options.containsKey(locale)) return options.get(locale);
        Locale langOnly = new Locale(locale.getLanguage());
        if (options.containsKey(langOnly)) return options.get(langOnly);
        return options.get(Locale.ENGLISH); // fallback
    }

    private String interpolate(String template, Map<String, String> vars, Locale locale) {
        // Replace {{var}} with locale-formatted values
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            String formatted = FormatProvider.format(entry.getValue(), locale);
            template = template.replace("{{" + entry.getKey() + "}}", formatted);
        }
        return template;
    }
}
```

---

## Variation 4: Batch/Digest Mode
**Learning Value:** Explores trade-offs between timeliness and noise reduction in batching and digest algorithms.

### Additional Requirements
- Aggregate multiple notifications into a single digest
- Configurable digest frequency (hourly, daily, weekly)
- Smart grouping (by source, category, priority)
- Digest template with summary counts
- Immediate delivery for critical notifications (bypass digest)
- Unread badge count management

### Design Changes
- Add `DigestAggregator` collecting notifications over time windows
- Add `DigestScheduler` for periodic batch delivery
- Add `DigestTemplate` for rendering grouped notifications
- Add `GroupingStrategy` interface for different aggregation approaches
- Modify `NotificationService` to route non-critical notifications to aggregator

### Solution Approach
Non-critical notifications are routed to the `DigestAggregator` instead of immediate delivery. The aggregator buffers notifications per user, grouped by `GroupingStrategy` (by source app, by category, or by thread/conversation). The `DigestScheduler` triggers at the user's configured frequency, pulling buffered notifications from the aggregator. The `DigestTemplate` renders a summary: "You have 5 new comments, 3 likes, and 2 mentions" with expandable details. Critical/urgent notifications bypass the digest entirely and deliver immediately. Each digest includes a "view all" deep link and manages the unread badge count.

### Key Classes to Add
```java
public class DigestAggregator {
    private Map<String, List<Notification>> userBuffers; // userId -> buffered notifications
    private GroupingStrategy groupingStrategy;

    public void buffer(Notification notification) {
        userBuffers.computeIfAbsent(notification.getUserId(), k -> new ArrayList<>())
            .add(notification);
    }

    public Digest buildDigest(String userId) {
        List<Notification> buffered = userBuffers.remove(userId);
        if (buffered == null || buffered.isEmpty()) return null;
        Map<String, List<Notification>> groups = groupingStrategy.group(buffered);
        return new Digest(userId, groups, LocalDateTime.now());
    }
}

public class DigestScheduler {
    private Map<String, DigestFrequency> userSchedules;

    public void processDigests() {
        for (Map.Entry<String, DigestFrequency> entry : userSchedules.entrySet()) {
            if (entry.getValue().isDue()) {
                Digest digest = aggregator.buildDigest(entry.getKey());
                if (digest != null) {
                    digestRenderer.renderAndSend(digest);
                }
            }
        }
    }
}
```

---

## Variation 5: Rich Notifications
**Learning Value:** Deepens understanding of rich content rendering, platform-specific formatting, and interactive payloads.

### Additional Requirements
- Images and media thumbnails in notifications
- Action buttons (Reply, Mark as Read, Archive)
- Deep links to specific app screens
- Carousel/expandable content
- Interactive elements (quick reply, rating)
- Platform-specific rendering (iOS, Android, Web)

### Design Changes
- Add `RichContent` class with media attachments and actions
- Add `ActionButton` with deep link and callback handling
- Add `PlatformRenderer` interface for platform-specific formatting
- Add `CarouselItem` for multi-item expandable content
- Add `InteractiveElement` for inline responses
- Modify `Notification` to include rich content payload

### Solution Approach
The `Notification` class is extended with an optional `RichContent` payload containing media URLs, action buttons, and interactive elements. The `PlatformRenderer` adapts the rich content to each platform's capabilities (iOS supports rich media and actions natively, Android uses notification channels, Web uses the Notifications API). `ActionButton` objects define both the display label and the deep link URL or API callback triggered on tap. For interactive elements like quick reply, the response is sent back to the server via the `InteractionHandler`. The `CarouselItem` list enables swipeable notification content on supported platforms.

### Key Classes to Add
```java
public class RichContent {
    private String imageUrl;
    private String thumbnailUrl;
    private List<ActionButton> actions;
    private List<CarouselItem> carousel;
    private InteractiveElement interactiveElement;

    public Map<String, Object> toPlatformPayload(Platform platform) {
        PlatformRenderer renderer = PlatformRendererFactory.get(platform);
        return renderer.render(this);
    }
}

public class ActionButton {
    private String label;       // "Reply", "Mark Read"
    private String actionId;    // unique identifier
    private String deepLink;    // app://messages/123
    private ActionType type;    // DEEP_LINK, API_CALLBACK, DISMISS

    public void onTap(String userId) {
        if (type == ActionType.API_CALLBACK) {
            InteractionHandler.handle(userId, actionId);
        }
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
