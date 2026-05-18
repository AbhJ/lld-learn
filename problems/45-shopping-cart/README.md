# Shopping Cart


## Problem Statement
Design a shopping cart that holds items, applies discount strategies, accepts coupon codes with minimum-order rules, adds optional services (gift wrap, express shipping), computes tax, and notifies subscribers when product prices drop.

## Requirements

### Functional Requirements
- Add and remove items with quantities
- Apply discount strategies (percentage, flat, BOGO)
- Apply coupon codes with min-order thresholds
- Attach add-on services that affect total
- Compute tax by region
- Checkout to produce a final total
- Notify observers of product price drops

### Non-functional Requirements
- Subtotal can be maintained as a running total (optimized)
- New discounts and add-ons added without modifying cart

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | DiscountStrategy (Percentage, Flat, BOGO) | Swappable discount calculation |
| Decorator | CartDecorator (GiftWrap, ExpressShipping) | Add optional services that modify total |
| Observer | PriceObserver / PriceDropNotifier | Notify users when product price changes |
| Composite | CompositeDiscount holding List&lt;DiscountStrategy&gt; | Combine multiple discount strategies and apply them as one |

## Folder Structure

```
45-shopping-cart/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Product, CartItem, Coupon
│   ├── service/      ← ShoppingCart, CartDecorator, TaxCalculator, PriceObserver
│   ├── strategy/     ← DiscountStrategy (Percentage, Flat, BOGO)
│   └── Main.java
└── optimized/
    ├── model/        ← Product, CartItem, Coupon
    ├── service/      ← ShoppingCart (running total), CartDecorator, TaxCalculator, PriceObserver
    ├── strategy/     ← DiscountStrategy
    └── Main.java
```

## How to Run

```bash
# Naive
cd problems/45-shopping-cart/naive
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd problems/45-shopping-cart/optimized
mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Get subtotal | O(n) recalculates every call | O(1) running total maintained on add/remove |
| Item lookup | O(n) linear search by product ID | O(1) HashMap index |
| Price change | Manual recalculation needed | Event-driven: auto-updates running total |
| Quantity update | Find + recalculate | O(1) delta adjustment to running total |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** User adds item while checkout process is reading cart — inconsistent total, item missed in order.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| ReentrantReadWriteLock | ShoppingCart.rwLock | Multiple adds (read-lock) concurrent, checkout (write-lock) exclusive |
| CopyOnWriteArrayList | ShoppingCart.items | Safe iteration during modification — checkout gets consistent snapshot |
| volatile boolean | ShoppingCart.checkedOut | Visible to all threads immediately after checkout sets it |
| Write-lock for checkout | ShoppingCart.checkout() | Guarantees no concurrent adds during snapshot — total is consistent |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
