# Movie Ticket Booking - Variations

## Variation 1: Food Combo Add-on
**Learning Value:** Teaches composite product bundling, upsell workflows, and add-on inventory management.

### Additional Requirements
- Popcorn, drinks, and snack combos attached to ticket booking
- Combo deals (popcorn + drink cheaper than individual)
- Pre-order for pickup at counter before showtime
- Inventory management for food items

### Design Changes
- Add `FoodItem` with category (snack, beverage, combo)
- Add `FoodOrder` linked to a `Booking`
- Add `ComboOffer` bundling items at discounted price
- Add `FoodInventory` tracking stock per theater

### Solution Approach
After seat selection, present optional food add-ons. `FoodOrder` is associated with a `Booking` and prepared for counter pickup 15 mins before showtime. `ComboOffer` defines bundles (e.g., Large Popcorn + 2 Drinks = $12 instead of $16). Food inventory is tracked per theater location with daily restocking. Apply same payment flow as tickets. Allow food-only orders for walk-in customers too. Show estimated preparation time based on queue length.

### Key Classes to Add
```java
public class FoodOrder {
    private String orderId;
    private Booking linkedBooking;
    private List<FoodItem> items;
    private OrderStatus status; // PLACED, PREPARING, READY, PICKED_UP
    private double totalAmount;
    private LocalDateTime pickupTime;

    public void applyComboDiscount(ComboOffer combo) {
        if (combo.isApplicable(items)) {
            totalAmount -= combo.getDiscount();
        }
    }
}

public class ComboOffer {
    private String comboName;
    private List<FoodItem> includedItems;
    private double comboPrice;

    public double getDiscount() {
        double individualTotal = includedItems.stream()
            .mapToDouble(FoodItem::getPrice).sum();
        return individualTotal - comboPrice;
    }

    public boolean isApplicable(List<FoodItem> orderItems) {
        return orderItems.containsAll(includedItems);
    }
}

public class FoodInventory {
    private Map<String, Integer> stock; // itemId -> quantity
    private String theaterId;

    public boolean isAvailable(String itemId, int quantity) {
        return stock.getOrDefault(itemId, 0) >= quantity;
    }
}
```

---

## Variation 2: Group Booking with Seat Adjacency
**Learning Value:** Introduces spatial constraint satisfaction, adjacency algorithms, and group seat optimization.

### Additional Requirements
- Ensure a group of N people sit together (adjacent seats in same row)
- Best available algorithm finding optimal adjacent block
- Fallback to nearest-possible if no single row available
- Split group across minimum number of rows

### Design Changes
- Add `GroupBookingRequest` with group size
- Add `SeatFinder` with adjacency-aware search algorithm
- Add `SeatBlock` representing consecutive seats
- Add `SplitStrategy` for handling groups larger than row capacity

### Solution Approach
Given group size N, search all rows for a contiguous block of N empty seats. Rank available blocks by: (1) row preference (middle rows preferred), (2) center-of-row preference. If no single row has N adjacent seats, use `SplitStrategy`: find minimum number of rows needed, maximize adjacency within each split (e.g., group of 7 might split as 4+3 in adjacent rows). Use a sliding window approach within each row to find available blocks efficiently.

### Key Classes to Add
```java
public class SeatFinder {
    public List<Seat> findAdjacentSeats(Screen screen, int groupSize) {
        for (Row row : screen.getRowsByPreference()) {
            List<Seat> block = findContiguousBlock(row, groupSize);
            if (block != null) return block;
        }
        return findBestSplit(screen, groupSize); // fallback
    }

    private List<Seat> findContiguousBlock(Row row, int size) {
        // Sliding window: find 'size' consecutive available seats
        List<Seat> seats = row.getSeats();
        int count = 0;
        for (int i = 0; i < seats.size(); i++) {
            if (seats.get(i).isAvailable()) {
                count++;
                if (count == size) return seats.subList(i - size + 1, i + 1);
            } else {
                count = 0;
            }
        }
        return null;
    }
}

public class SplitStrategy {
    public List<List<Seat>> splitGroup(Screen screen, int groupSize) {
        // Find minimum rows needed, adjacent rows preferred
        // Maximize contiguous seats within each row
        ...
    }
}
```

---

## Variation 3: Dynamic Pricing
**Learning Value:** Practices demand-based pricing models, time-slot multipliers, and real-time rate adjustment.

### Additional Requirements
- Price varies by time: peak hours, weekends, holidays cost more
- Opening weekend premium for anticipated movies
- Demand-based pricing (high booking pace = higher price)
- Seat category pricing (premium rows, recliners)

### Design Changes
- Add `DynamicPricingEngine` with multiple price factors
- Add `DemandTracker` monitoring booking velocity
- Add `PriceFactor` interface for composable pricing rules
- Add `SeatPremium` for location-based pricing within screen

### Solution Approach
Base price is modified by composable `PriceFactor`s: time factor (weekday matinee 0.8x, Friday evening 1.3x, weekend 1.5x), demand factor (if 70%+ booked in first hour = high demand = 1.4x), movie factor (opening weekend 1.5x, first week 1.2x), seat factor (front rows 0.8x, middle "sweet spot" 1.2x, recliners 2x). Factors are multiplicative. Price updates in real-time but locked once user starts checkout (with 10-min hold). Show price breakdown to user.

### Key Classes to Add
```java
public class DynamicPricingEngine {
    private List<PriceFactor> factors;

    public double calculatePrice(Show show, Seat seat) {
        double basePrice = show.getBasePrice();
        double multiplier = factors.stream()
            .mapToDouble(f -> f.getMultiplier(show, seat))
            .reduce(1.0, (a, b) -> a * b);
        return basePrice * multiplier;
    }
}

public interface PriceFactor {
    double getMultiplier(Show show, Seat seat);
}

public class DemandFactor implements PriceFactor {
    @Override
    public double getMultiplier(Show show, Seat seat) {
        double bookingRate = show.getBookingsInLastHour() / (double) show.getTotalSeats();
        if (bookingRate > 0.1) return 1.4; // high demand
        if (bookingRate > 0.05) return 1.2; // moderate
        return 1.0;
    }
}

public class TimeFactor implements PriceFactor {
    @Override
    public double getMultiplier(Show show, Seat seat) {
        DayOfWeek day = show.getShowTime().getDayOfWeek();
        int hour = show.getShowTime().getHour();
        if (day == SATURDAY || day == SUNDAY) return 1.5;
        if (hour >= 18) return 1.3; // evening
        if (hour < 12) return 0.8; // morning matinee
        return 1.0;
    }
}
```

---

## Variation 4: Subscription Model (Unlimited Pass)
**Learning Value:** Explores trade-offs between usage limits and user value in subscription-based access models.

### Additional Requirements
- Monthly subscription with N movies included
- Eligible theaters and show restrictions
- Cooldown between consecutive bookings
- Peak vs off-peak allocation in subscription

### Design Changes
- Add `Subscription` with plan details and usage tracking
- Add `SubscriptionPlan` defining limits and eligibility
- Add `UsageTracker` monitoring monthly consumption
- Add `EligibilityChecker` validating theater/time restrictions

### Solution Approach
Define `SubscriptionPlan` tiers: Basic (3 movies/month, weekday only, standard screens), Premium (unlimited, any day, includes IMAX). `UsageTracker` counts movies seen this billing cycle. On booking attempt, `EligibilityChecker` validates: (1) remaining quota, (2) eligible theater, (3) eligible show time, (4) cooldown met (e.g., 24 hours between bookings). Subscription covers ticket cost; food/upgrades are extra. Handle edge cases: what if subscription expires between booking and showtime?

### Key Classes to Add
```java
public class Subscription {
    private String subscriptionId;
    private Guest member;
    private SubscriptionPlan plan;
    private LocalDate startDate;
    private LocalDate renewalDate;
    private UsageTracker usage;

    public boolean canBook(Show show, Theater theater) {
        return isActive() 
            && usage.hasRemainingQuota()
            && plan.isEligibleTheater(theater)
            && plan.isEligibleShowTime(show.getShowTime())
            && usage.isCooldownMet();
    }
}

public class SubscriptionPlan {
    private String planName;
    private int monthlyMovieLimit; // -1 for unlimited
    private Set<TheaterType> eligibleTheaters;
    private boolean weekendsIncluded;
    private boolean premiumFormatsIncluded;
    private Duration cooldownBetweenBookings;
    private double monthlyPrice;
}

public class UsageTracker {
    private int moviesThisMonth;
    private LocalDateTime lastBookingTime;
    private int monthlyLimit;
    private Duration cooldown;

    public boolean hasRemainingQuota() {
        return monthlyLimit == -1 || moviesThisMonth < monthlyLimit;
    }
    public boolean isCooldownMet() {
        return lastBookingTime == null || 
               Duration.between(lastBookingTime, LocalDateTime.now()).compareTo(cooldown) >= 0;
    }
}
```

---

## Variation 5: Review/Rating System
**Learning Value:** Deepens understanding of aggregation systems, weighted scoring, and user-generated content moderation.

### Additional Requirements
- Post-movie ratings (1-5 stars) from verified ticket holders
- Separate critic vs audience scores
- Review text with spoiler flags
- Aggregate ratings influencing movie recommendations

### Design Changes
- Add `Review` with rating, text, spoiler flag
- Add `RatingAggregator` computing various scores
- Add `CriticReview` with weighted scoring
- Add `VerifiedPurchase` linking review to actual booking

### Solution Approach
After a show's end time passes, prompt the ticket holder to rate/review. `Review` must be linked to a verified `Booking` (prevent fake reviews). Separate aggregation for critics (weighted by critic reputation) and audience (simple average). Spoiler reviews are hidden behind a click. `RatingAggregator` computes: average score, distribution (how many 1-star, 2-star, etc.), freshness score (% of reviews >= 3.5 stars). Use these scores in recommendation engine for suggesting movies to users based on their rating history.

### Key Classes to Add
```java
public class Review {
    private String reviewId;
    private Booking verifiedBooking; // proves they watched the movie
    private Movie movie;
    private Guest reviewer;
    private int rating; // 1-5
    private String reviewText;
    private boolean containsSpoilers;
    private LocalDateTime postedAt;
    private int helpfulVotes;

    public boolean isVerified() { return verifiedBooking != null; }
}

public class RatingAggregator {
    private List<Review> reviews;

    public double getAverageRating() {
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
    }

    public double getFreshnessScore() {
        long positive = reviews.stream().filter(r -> r.getRating() >= 4).count();
        return (double) positive / reviews.size() * 100;
    }

    public Map<Integer, Long> getRatingDistribution() {
        return reviews.stream().collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
    }
}

public class CriticReview extends Review {
    private String publication;
    private double criticWeight; // higher weight for established critics

    public double getWeightedScore() {
        return getRating() * criticWeight;
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
