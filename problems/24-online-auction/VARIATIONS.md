# Online Auction - Variations

## Variation 1: Dutch Auction (Descending Price)
**Learning Value:** Teaches descending-price mechanics, time-pressure design, and alternative auction theory.

### Additional Requirements
- Price starts high and drops at regular intervals
- First bidder to accept the current price wins
- Configurable price decrement and time interval
- Floor price (auction ends with no sale if reached)
- Multiple units possible (each unit goes to next bidder)

### Design Changes
- Add `DutchAuction` class with descending price timer
- Add `PriceSchedule` defining decrement rate and intervals
- Modify `WinnerStrategy` to support first-accept-wins
- Add `AuctionTimer` that triggers price drops
- Add multi-unit support where multiple bidders can win

### Solution Approach
The auction starts at a high asking price that decreases at fixed time intervals (e.g., $10 every 30 seconds). A timer thread manages the price drops. The first bidder to accept the current price wins the item at that price. If the price drops to the floor with no buyer, the auction ends unsold. For multi-unit Dutch auctions, the first bidder gets their requested quantity at the acceptance price, and the auction continues for remaining units. This format favors speed of decision over bidding wars and is commonly used for perishable goods and IPO share allocation.

### Key Classes to Add
```java
public class DutchAuction extends Auction {
    private double currentPrice;
    private double decrementAmount;
    private long intervalMs;
    private double floorPrice;
    private ScheduledExecutorService timer;
    
    public void start() {
        // Schedule periodic price drops
        timer.scheduleAtFixedRate(this::decrementPrice, intervalMs, intervalMs, MILLISECONDS);
    }
    
    public boolean acceptPrice(Bidder bidder) {
        // First to accept wins at currentPrice
    }
    
    private void decrementPrice() {
        currentPrice -= decrementAmount;
        if (currentPrice <= floorPrice) end();
    }
}
```

---

## Variation 2: Sealed-Bid Auction
**Learning Value:** Introduces sealed-bid evaluation, fairness guarantees, and information-hiding in competitive bidding.

### Additional Requirements
- Bids are hidden from other participants
- All bids submitted before deadline
- Winner revealed only after auction closes
- Support for first-price and second-price (Vickrey) sealed-bid
- Anti-tampering: bids cannot be modified after submission

### Design Changes
- Add `SealedBidAuction` where bids are encrypted/hidden
- Add `BidVault` that stores bids without revealing them
- Add `RevealPhase` that opens all bids simultaneously
- Modify `WinnerStrategy` to support Vickrey (second-price) logic
- Add `BidCommitment` using hash-commit-reveal scheme

### Solution Approach
Bidders submit their bids during the bidding phase without seeing others' bids. Bids are stored in a sealed vault (optionally using cryptographic commitments where bidders submit a hash first, then reveal). After the deadline, all bids are revealed simultaneously. In first-price sealed-bid, the highest bidder wins and pays their bid. In Vickrey (second-price), the highest bidder wins but pays the second-highest bid amount. This incentivizes truthful bidding in the Vickrey variant. The system ensures no bid can be modified after submission through immutable storage or cryptographic commitments.

### Key Classes to Add
```java
public class SealedBidAuction extends Auction {
    private Map<String, SealedBid> vault;
    private AuctionPhase phase; // BIDDING, CLOSED, REVEALED
    private PricingRule pricingRule; // FIRST_PRICE, SECOND_PRICE
    
    public void submitBid(Bidder bidder, double amount) {
        if (phase != AuctionPhase.BIDDING) throw new IllegalStateException();
        vault.put(bidder.getId(), new SealedBid(bidder, amount, Instant.now()));
    }
    
    public AuctionResult reveal() {
        phase = AuctionPhase.REVEALED;
        // Sort bids, determine winner and price based on pricingRule
    }
}

public class SealedBid {
    private Bidder bidder;
    private double amount;
    private Instant submittedAt;
    private String commitHash; // optional: for commit-reveal scheme
}
```

---

## Variation 3: Reserve Price
**Learning Value:** Practices reserve threshold logic, conditional execution, and seller-protection mechanisms.

### Additional Requirements
- Seller sets a minimum threshold (hidden from bidders)
- Auction ends with no sale if reserve not met
- Optional indicator showing "reserve met" or "reserve not met"
- Seller can lower reserve during auction
- Post-auction negotiation if reserve not met but bids exist

### Design Changes
- Add `reservePrice` field to `Auction` (private, not exposed to bidders)
- Add `ReserveStatus` (MET, NOT_MET) indicator
- Modify `Auction.end()` to check reserve before declaring winner
- Add `PostAuctionNegotiation` for near-miss scenarios
- Add seller ability to adjust reserve mid-auction

### Solution Approach
The reserve price is stored as a private field on the auction, visible only to the seller and system. During bidding, an optional indicator tells bidders whether the reserve has been met (without revealing the exact amount). When the auction ends, if the highest bid is below the reserve, no sale occurs. The seller may then choose to contact the highest bidder for post-auction negotiation. The seller can lower (never raise) the reserve during the auction to increase chances of sale. This protects sellers from selling below their minimum acceptable price while maintaining competitive bidding.

### Key Classes to Add
```java
public class ReserveAuction extends Auction {
    private double reservePrice;
    private boolean showReserveStatus;
    
    public boolean isReserveMet() {
        return getHighestBid() >= reservePrice;
    }
    
    public AuctionResult end() {
        if (!isReserveMet()) {
            return AuctionResult.noSale(getHighestBid());
        }
        return AuctionResult.sold(getWinner(), getHighestBid());
    }
    
    public void lowerReserve(double newReserve) {
        if (newReserve >= reservePrice) throw new IllegalArgumentException();
        this.reservePrice = newReserve;
    }
}

public class AuctionResult {
    private boolean sold;
    private Bidder winner;
    private double finalPrice;
    private boolean reserveMet;
}
```

---

## Variation 4: Auction with Buyout Option
**Learning Value:** Explores trade-offs between auction excitement and immediate purchase in hybrid selling models.

### Additional Requirements
- Buy-it-now option at a fixed price alongside regular bidding
- Buy-it-now disappears once bidding reaches a threshold
- Immediate purchase ends the auction
- Option for "Best Offer" where buyer proposes a price
- Seller auto-accept/reject rules for offers

### Design Changes
- Add `buyNowPrice` field to `Auction`
- Add `BuyNow` action that immediately ends auction
- Add `buyNowThreshold` after which buy-now is disabled
- Add `BestOffer` mechanism with seller acceptance rules
- Modify auction state machine to handle instant-purchase transitions

### Solution Approach
The auction has both a starting bid price and a buy-it-now price. Bidders can either bid normally or choose to buy immediately at the fixed price. Once a bid is placed above a configurable threshold (e.g., starting price), the buy-it-now option may be removed to encourage competitive bidding. If someone uses buy-it-now, the auction ends immediately with them as the winner. The "Best Offer" variant allows buyers to propose a price below buy-it-now; the seller can set auto-accept thresholds (accept if offer > X, reject if < Y, otherwise review manually).

### Key Classes to Add
```java
public class BuyNowAuction extends Auction {
    private double buyNowPrice;
    private double buyNowDisableThreshold;
    private boolean buyNowAvailable;
    
    public boolean buyNow(Bidder bidder) {
        if (!buyNowAvailable) return false;
        // End auction immediately, bidder wins at buyNowPrice
        declareWinner(bidder, buyNowPrice);
        return true;
    }
    
    @Override
    public boolean placeBid(Bidder bidder, double amount) {
        boolean result = super.placeBid(bidder, amount);
        if (amount >= buyNowDisableThreshold) {
            buyNowAvailable = false;
        }
        return result;
    }
}

public class BestOffer {
    private Bidder buyer;
    private double offerAmount;
    private OfferStatus status; // PENDING, ACCEPTED, REJECTED, COUNTERED
}
```

---

## Variation 5: Charity/Timed Auction
**Learning Value:** Deepens understanding of time-bound event management, charity constraints, and goal-based auction rules.

### Additional Requirements
- Multiple items auctioned in a batch/catalog
- Staggered or simultaneous closing times
- Donation tracking and tax receipt generation
- Anti-sniping rules (extend time on last-second bids)
- Leaderboard showing top donors/bidders

### Design Changes
- Add `AuctionCatalog` managing multiple items in one event
- Add `AntiSnipingRule` that extends close time on late bids
- Add `DonationTracker` for charity amount tracking
- Add `TaxReceipt` generation for winning bids
- Add `Leaderboard` tracking top bidders/donors

### Solution Approach
A charity auction event contains a catalog of items, each with its own bidding. Items can close simultaneously (batch close) or at staggered times. Anti-sniping rules extend the closing time by N minutes if a bid arrives in the final M minutes, preventing last-second sniping and encouraging fair bidding. A donation tracker computes the total raised per event and per donor. Winners receive tax receipts showing the fair market value vs. their winning bid (the difference is the deductible donation). A leaderboard gamifies the experience by showing top bidders and total amounts raised in real-time.

### Key Classes to Add
```java
public class AuctionCatalog {
    private String eventId;
    private String eventName;
    private List<Auction> items;
    private Instant eventStart;
    private Instant eventEnd;
    
    public void startAll() { ... }
    public void closeAll() { ... }
    public double getTotalRaised() { ... }
}

public class AntiSnipingRule {
    private Duration snipeWindow; // e.g., last 5 minutes
    private Duration extensionTime; // e.g., extend by 3 minutes
    
    public boolean shouldExtend(Auction auction, Instant bidTime) {
        Duration remaining = Duration.between(bidTime, auction.getEndTime());
        return remaining.compareTo(snipeWindow) < 0;
    }
}

public class DonationTracker {
    private Map<String, Double> donorTotals;
    
    public void recordWinningBid(String donorId, double amount, double fairMarketValue) { ... }
    public TaxReceipt generateReceipt(String donorId) { ... }
    public List<DonorEntry> getLeaderboard() { ... }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
