# Library Management - Variations

## Variation 1: Digital Library with E-books
**Learning Value:** Teaches digital rights management, concurrent access control, and license-based resource sharing.

### Additional Requirements
- E-books with download limits per user
- DRM (Digital Rights Management) protection
- Concurrent reader limits per title
- Offline access with time-based expiry

### Design Changes
- Add `EBook extends Book` with file format and DRM info
- Add `DownloadSession` tracking active downloads
- Add `DRMLicense` with expiry and device limits
- Add `ConcurrentAccessManager` to enforce reader limits

### Solution Approach
Create `EBook` extending `Book` with digital-specific attributes (format, file size, DRM scheme). Each borrow creates a `DRMLicense` tied to the user's device with an expiry. `ConcurrentAccessManager` tracks how many users currently have an active license for a title (e.g., library owns 3 digital "copies" = 3 concurrent readers max). When limit reached, others join waitlist. Downloads decrement a per-user daily/monthly quota tracked by `DownloadQuota`.

### Key Classes to Add
```java
public class EBook extends Book {
    private FileFormat format; // EPUB, PDF, MOBI
    private long fileSizeBytes;
    private DRMScheme drmScheme;
    private int maxConcurrentReaders;
}

public class DRMLicense {
    private String licenseId;
    private Member member;
    private EBook ebook;
    private String deviceId;
    private LocalDateTime expiryTime;
    private boolean isActive;

    public boolean isValid() {
        return isActive && LocalDateTime.now().isBefore(expiryTime);
    }
}

public class ConcurrentAccessManager {
    private Map<String, Set<DRMLicense>> activeLicenses; // bookId -> active licenses

    public boolean canGrantAccess(EBook book) {
        return activeLicenses.getOrDefault(book.getId(), Set.of()).size() 
               < book.getMaxConcurrentReaders();
    }
}
```

---

## Variation 2: Inter-Library Loan
**Learning Value:** Introduces cross-system coordination, shipment state tracking, and inter-organizational agreements.

### Additional Requirements
- Request books from partner libraries
- Shipping/transit tracking between libraries
- Loan period management across institutions
- Cost sharing between libraries

### Design Changes
- Add `PartnerLibrary` with API integration
- Add `InterLibraryLoan` with transit states
- Add `ShipmentTracker` for physical book movement
- Add `LoanAgreement` defining terms between libraries

### Solution Approach
When a book is unavailable locally, search partner libraries via `PartnerLibrary` catalog APIs. Create an `InterLibraryLoan` request that goes through states: REQUESTED -> APPROVED -> IN_TRANSIT -> RECEIVED -> CHECKED_OUT -> RETURNED -> IN_TRANSIT_BACK -> COMPLETED. Track physical shipment via `ShipmentTracker`. Loan agreements define max loan period, cost per loan, and late fee responsibility. Use a `LoanBroker` to find the nearest partner with the book available.

### Key Classes to Add
```java
public class InterLibraryLoan {
    private String loanId;
    private PartnerLibrary sourceLibrary;
    private Book book;
    private Member requestingMember;
    private ILLStatus status;
    private ShipmentTracker shipment;
    private LoanAgreement agreement;

    public void approve() { status = ILLStatus.APPROVED; initiateShipment(); }
    public void markReceived() { status = ILLStatus.RECEIVED; }
}

public class PartnerLibrary {
    private String libraryId;
    private String name;
    private String apiEndpoint;
    private LoanAgreement agreement;

    public boolean checkAvailability(String isbn) { ... }
    public InterLibraryLoan requestBook(String isbn) { ... }
}
```

---

## Variation 3: Subscription Tiers
**Learning Value:** Practices tiered access control, configurable policies, and membership lifecycle management.

### Additional Requirements
- Multiple membership levels: Basic, Premium, Student
- Different borrow limits per tier
- Extended due dates for premium members
- Priority reservations for higher tiers

### Design Changes
- Add `SubscriptionTier` enum with tier-specific rules
- Add `MembershipPlan` with limits and pricing
- Modify `BorrowPolicy` to be tier-aware
- Add `TierUpgradeManager` for tier transitions

### Solution Approach
Define `MembershipPlan` for each tier with configurable limits: max books, borrow duration, reservation priority, fine rates. When a member attempts to borrow, check against their tier's limits. Premium members get longer due dates and can reserve books ahead of basic members. Implement a priority queue for reservations sorted by tier then timestamp. Tier upgrades/downgrades take effect immediately with grace period for exceeding new limits.

### Key Classes to Add
```java
public class MembershipPlan {
    private SubscriptionTier tier;
    private int maxBooksAllowed;
    private int borrowDurationDays;
    private double dailyFineRate;
    private int reservationPriority; // lower = higher priority
    private double monthlyFee;

    public static MembershipPlan BASIC = new MembershipPlan(BASIC, 3, 14, 0.50, 3, 0.0);
    public static MembershipPlan PREMIUM = new MembershipPlan(PREMIUM, 10, 30, 0.25, 1, 9.99);
    public static MembershipPlan STUDENT = new MembershipPlan(STUDENT, 5, 21, 0.25, 2, 4.99);
}

public class TierAwareBorrowPolicy {
    public boolean canBorrow(Member member) {
        MembershipPlan plan = member.getMembershipPlan();
        int currentBorrowed = member.getActiveBorrows().size();
        return currentBorrowed < plan.getMaxBooksAllowed();
    }
}
```

---

## Variation 4: Late Return Queue
**Learning Value:** Explores trade-offs between fairness and automation in priority queue and escalation workflows.

### Additional Requirements
- Priority waitlist when books are overdue
- Escalating notifications to overdue borrowers
- Auto-extend if no one is waiting
- Fine calculation with grace periods

### Design Changes
- Add `WaitlistManager` with priority queue
- Add `NotificationEscalator` with configurable escalation levels
- Add `AutoExtensionPolicy` for unwanted books
- Modify fine calculation to include grace period logic

### Solution Approach
Maintain a priority waitlist per book. When a book is overdue AND someone is waiting, escalate notifications to the borrower (email -> SMS -> account restriction). Priority in waitlist based on: how long waiting, membership tier, and whether they've been skipped before. If no one is waiting, auto-extend the loan. Fine calculation: grace period (3 days, no fine) -> standard fine -> escalated fine after 2 weeks -> account suspension after 4 weeks.

### Key Classes to Add
```java
public class WaitlistManager {
    private Map<String, PriorityQueue<WaitlistEntry>> bookWaitlists;

    public void addToWaitlist(String bookId, Member member) { ... }
    public WaitlistEntry getNextInLine(String bookId) { ... }
    public boolean hasWaiters(String bookId) { ... }
}

public class NotificationEscalator {
    private List<EscalationLevel> levels;

    public void escalate(BorrowRecord overdueRecord) {
        int daysOverdue = calculateDaysOverdue(overdueRecord);
        EscalationLevel level = getLevel(daysOverdue);
        level.sendNotification(overdueRecord.getMember());
    }
}

public class EscalationLevel {
    private int triggerDaysOverdue;
    private NotificationType type; // EMAIL, SMS, ACCOUNT_RESTRICTION
    private String messageTemplate;
}
```

---

## Variation 5: Recommendation Engine
**Learning Value:** Deepens understanding of collaborative filtering, content-based recommendations, and user profiling.

### Additional Requirements
- Recommend based on borrow history
- Genre/author preference tracking
- "Readers who borrowed X also borrowed Y"
- Trending books and new arrivals highlights

### Design Changes
- Add `RecommendationEngine` with multiple strategies
- Add `UserProfile` tracking reading preferences
- Add `CollaborativeFilter` for "also borrowed" suggestions
- Add `TrendingTracker` for popular books

### Solution Approach
Build a `UserProfile` from borrow history: track genre frequency, favorite authors, reading pace. Implement multiple recommendation strategies: content-based (same genre/author), collaborative filtering (users with similar history), and trending (most borrowed recently). Combine scores with configurable weights. Use a simple co-occurrence matrix: for each pair of books, count how many users borrowed both. Cache recommendations and refresh periodically.

### Key Classes to Add
```java
public class RecommendationEngine {
    private List<RecommendationStrategy> strategies;
    private Map<String, UserProfile> userProfiles;

    public List<Book> getRecommendations(Member member, int count) {
        UserProfile profile = userProfiles.get(member.getId());
        return strategies.stream()
            .flatMap(s -> s.recommend(profile).stream())
            .distinct()
            .sorted(Comparator.comparing(Book::getRelevanceScore).reversed())
            .limit(count)
            .collect(Collectors.toList());
    }
}

public class UserProfile {
    private Map<Genre, Integer> genreFrequency;
    private Set<String> favoriteAuthors;
    private List<String> recentlyBorrowed;

    public void updateFromBorrow(Book book) { ... }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
