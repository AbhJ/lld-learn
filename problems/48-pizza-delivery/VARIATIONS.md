# Pizza Delivery - Variations

## Variation 1: Build-Your-Own with Nutrition
**Learning Value:** Teaches custom product configuration, constraint validation, and real-time nutrition calculation.

### Additional Requirements
- Real-time calorie and macro tracking as toppings are added
- Allergen warnings for each ingredient
- Dietary filters (vegan, gluten-free, keto)
- Nutrition label generation for final pizza

### Design Changes
- Add `NutritionInfo` per ingredient
- Add `AllergenService` checking against user allergies
- Add `DietaryFilter` for menu customization
- Modify `PizzaBuilder` to accumulate nutrition data

### Solution Approach
Each `Topping`, `Crust`, and sauce has associated `NutritionInfo` (calories, protein, fat, carbs) and a list of allergens. As the user builds their pizza via `PizzaBuilder`, the running nutritional total is updated. `AllergenService` checks selected ingredients against the user's allergy profile and warns immediately. `DietaryFilter` hides non-compliant options (e.g., hide meat toppings for vegetarian). The final pizza order includes a complete nutrition label.

### Key Classes to Add
```java
public class NutritionInfo {
    private int calories;
    private double proteinGrams;
    private double fatGrams;
    private double carbsGrams;
    private List<String> allergens; // GLUTEN, DAIRY, NUTS, etc.

    public NutritionInfo add(NutritionInfo other) { /* Combine */ }
}

public class NutritionTrackingBuilder extends PizzaBuilder {
    private NutritionInfo runningTotal;

    @Override
    public PizzaBuilder addTopping(Topping t) {
        runningTotal = runningTotal.add(t.getNutrition());
        return super.addTopping(t);
    }

    public NutritionInfo getCurrentNutrition() { return runningTotal; }
    public List<String> getAllergenWarnings() { /* Collect all allergens */ }
}
```

---

## Variation 2: Group Ordering
**Learning Value:** Introduces shared ordering coordination, payment splitting, and group consensus workflows.

### Additional Requirements
- Shared cart where multiple people add pizzas
- Individual or split payment options
- Majority vote on shared toppings
- Order deadline before submission

### Design Changes
- Add `GroupOrder` aggregating individual selections
- Add `VotingService` for shared decisions
- Add `SplitPayment` handling per-person charges
- Add `OrderDeadline` with timer

### Solution Approach
A `GroupOrder` is created by an organizer who shares a link. Each participant adds their own items to the group cart before the deadline. For shared pizzas, participants vote on toppings via `VotingService` (majority wins). When the deadline hits, the order is finalized. `SplitPayment` calculates each person's share (equal split or pay-for-own) and charges individually. The organizer can see all selections and submit the combined order.

### Key Classes to Add
```java
public class GroupOrder {
    private String id;
    private String organizerId;
    private Map<String, List<Pizza>> participantSelections;
    private LocalDateTime deadline;
    private GroupOrderStatus status;

    public void addParticipantSelection(String userId, Pizza pizza) { /* Add to their list */ }
    public Order finalizeOrder() { /* Combine all selections into one order */ }
}

public class VotingService {
    private Map<String, Map<Topping, Integer>> votes; // pizzaId -> topping -> count

    public void castVote(String pizzaId, String userId, List<Topping> toppings) { /* Record */ }
    public List<Topping> getWinningToppings(String pizzaId) { /* Majority */ }
}

public class SplitPayment {
    public Map<String, Double> calculateEqualSplit(GroupOrder order) { /* Total / people */ }
    public Map<String, Double> calculatePayForOwn(GroupOrder order) { /* Individual totals */ }
}
```

---

## Variation 3: Subscription (Weekly Pizza)
**Learning Value:** Practices subscription lifecycle management, recurring delivery scheduling, and preference learning.

### Additional Requirements
- Schedule recurring deliveries (weekly, biweekly)
- Learn preferences over time
- Surprise/random option for variety
- Pause, skip, or cancel easily

### Design Changes
- Add `PizzaSubscription` with schedule and preferences
- Add `PreferenceLearner` analyzing order history
- Add `SurpriseGenerator` for random combos
- Add `SubscriptionManager` for lifecycle

### Solution Approach
A `PizzaSubscription` stores delivery frequency, preferred day/time, and pizza preferences (favorite toppings, crust, size). The `PreferenceLearner` tracks which pizzas the user rates highly and adjusts future suggestions. The surprise option uses `SurpriseGenerator` to create combinations the user hasn't tried but might like based on their preference profile. Users can skip a week, pause, or modify next delivery from their account.

### Key Classes to Add
```java
public class PizzaSubscription {
    private String userId;
    private Frequency frequency; // WEEKLY, BIWEEKLY
    private DayOfWeek deliveryDay;
    private List<Pizza> defaultPizzas;
    private boolean surpriseEnabled;
    private SubscriptionStatus status;
}

public class PreferenceLearner {
    private Map<String, List<Integer>> ratings; // pizzaConfig -> ratings

    public Pizza suggestNext(String userId) { /* Based on history */ }
    public void recordRating(String userId, Pizza pizza, int rating) { /* Learn */ }
}

public class SurpriseGenerator {
    private PreferenceLearner learner;
    public Pizza generateSurprise(String userId) {
        // Novel combination aligned with learned preferences
    }
}
```

---

## Variation 4: Dark Kitchen Optimization
**Learning Value:** Explores trade-offs between delivery speed and kitchen utilization in virtual kitchen routing.

### Additional Requirements
- Multiple brands served from one kitchen
- Prep time optimization for parallel orders
- Kitchen capacity and station management
- Order batching for efficiency

### Design Changes
- Add `DarkKitchen` with multiple brand menus
- Add `PrepOptimizer` scheduling cook tasks
- Add `KitchenStation` (oven, prep, packaging)
- Add `OrderBatcher` grouping nearby deliveries

### Solution Approach
A `DarkKitchen` serves multiple virtual brands (Italian, Indian, Burgers) from one physical location. `PrepOptimizer` schedules cooking tasks across `KitchenStation` resources (ovens, prep counters, packaging stations) to minimize total order completion time. Orders are batched by `OrderBatcher` when deliveries go to the same area, reducing driver trips. Each station has capacity limits; the optimizer uses a priority queue to schedule tasks without overloading any station.

### Key Classes to Add
```java
public class DarkKitchen {
    private String id;
    private List<String> brands;
    private List<KitchenStation> stations;
    private PrepOptimizer optimizer;
}

public class PrepOptimizer {
    private List<KitchenStation> stations;

    public PrepSchedule optimizeOrders(List<Order> pendingOrders) {
        // Assign tasks to stations, minimize total completion time
    }

    public Duration estimatePrepTime(Order order) { /* Based on items and current load */ }
}

public class KitchenStation {
    private String id;
    private StationType type; // OVEN, PREP, PACKAGING
    private int capacity;
    private Queue<PrepTask> taskQueue;
    public boolean isAvailable() { return taskQueue.size() < capacity; }
}
```

---

## Variation 5: Dynamic Delivery Zones
**Learning Value:** Deepens understanding of geospatial zone management, dynamic boundary adjustment, and coverage optimization.

### Additional Requirements
- Adjust delivery radius based on current demand
- Factor in driver availability
- Surge pricing for far deliveries
- Estimated delivery time per zone

### Design Changes
- Add `DeliveryZoneService` computing dynamic radius
- Add `DemandTracker` monitoring order volume
- Add `SurgePricing` based on distance and demand
- Add `ETACalculator` per zone

### Solution Approach
`DeliveryZoneService` dynamically computes the serviceable radius based on current conditions: when demand is high and drivers are scarce, shrink the radius; when idle, expand it. `SurgePricing` adds a distance-based delivery fee that increases during peak hours. `ETACalculator` estimates delivery time using distance, current driver locations, prep time, and traffic conditions. Zones are represented as concentric rings from the kitchen, each with different fees and ETAs.

### Key Classes to Add
```java
public class DeliveryZoneService {
    private DemandTracker demandTracker;
    private int availableDrivers;
    private double baseRadiusKm;

    public double getCurrentRadius() {
        // Expand/shrink based on demand vs driver supply
    }

    public boolean isInDeliveryZone(Location customerLocation, Location kitchenLocation) {
        return distance(customerLocation, kitchenLocation) <= getCurrentRadius();
    }
}

public class SurgePricing {
    private double baseDeliveryFee;
    public double calculateFee(double distanceKm, double demandMultiplier) {
        return baseDeliveryFee + (distanceKm * demandMultiplier);
    }
}

public class ETACalculator {
    public Duration estimateDelivery(Location kitchen, Location customer, Duration prepTime) {
        // prep + travel time based on distance and conditions
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
