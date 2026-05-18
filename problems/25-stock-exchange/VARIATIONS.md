# Stock Exchange - Variations

## Variation 1: Options Trading
**Learning Value:** Teaches derivative modeling, mathematical pricing models, and exercise/assignment mechanics.

### Additional Requirements
- Call and put options with strike price and expiry
- Greeks calculation (Delta, Gamma, Theta, Vega)
- Options chain display (all strikes for a given expiry)
- Exercise and assignment mechanics
- Margin requirements for naked options

### Design Changes
- Add `Option` class with type (CALL/PUT), strike, expiry, underlying
- Add `OptionsChain` aggregating all options for a stock
- Add `GreeksCalculator` using Black-Scholes or binomial model
- Add `ExerciseEngine` for ITM option exercise at expiry
- Modify order book to handle options order matching

### Solution Approach
An option is a derivative with a type (call/put), strike price, expiration date, and underlying stock. The system maintains a separate order book for each option contract (unique combination of underlying + type + strike + expiry). Greeks are computed using the Black-Scholes model: Delta measures price sensitivity, Gamma is Delta's rate of change, Theta is time decay, and Vega measures volatility sensitivity. At expiry, in-the-money options are auto-exercised (buyer gets shares at strike) or the holder can exercise early for American-style options. Margin requirements are calculated based on the naked exposure.

### Key Classes to Add
```java
public class Option {
    private String symbol;
    private Stock underlying;
    private OptionType type; // CALL, PUT
    private double strikePrice;
    private LocalDate expiry;
    private double premium;
    
    public boolean isInTheMoney(double currentPrice) { ... }
}

public class GreeksCalculator {
    public Greeks calculate(Option option, double stockPrice, double volatility, double riskFreeRate) {
        // Black-Scholes: d1, d2 calculations
        // Delta = N(d1) for calls, N(d1)-1 for puts
        // Gamma, Theta, Vega derived from d1, d2
    }
}

public class Greeks {
    private double delta, gamma, theta, vega, rho;
}
```

---

## Variation 2: Market Making
**Learning Value:** Introduces continuous quoting strategies, inventory risk management, and spread optimization.

### Additional Requirements
- Continuously quote bid and ask prices
- Manage bid-ask spread for profit
- Inventory management (avoid excess long/short position)
- Risk limits and position caps
- Auto-adjust quotes based on inventory skew

### Design Changes
- Add `MarketMaker` class with quoting strategy
- Add `InventoryManager` tracking current position and P&L
- Add `SpreadCalculator` determining optimal bid-ask spread
- Add `RiskManager` enforcing position limits
- Add `QuoteUpdater` that adjusts prices based on market conditions

### Solution Approach
The market maker continuously places both buy and sell limit orders, profiting from the bid-ask spread. The spread width depends on volatility, inventory, and risk tolerance. When inventory builds up (too long or too short), the market maker skews quotes to attract orders that reduce the position. Risk limits cap the maximum position size and daily loss. Quotes are updated rapidly based on market data feeds and inventory changes. The system uses a feedback loop: favorable fills adjust inventory, which adjusts quote skew, which influences future fills. P&L is tracked in real-time.

### Key Classes to Add
```java
public class MarketMaker {
    private Stock stock;
    private InventoryManager inventory;
    private RiskManager riskManager;
    private SpreadCalculator spreadCalc;
    
    public Quote generateQuote() {
        double mid = getMarketMidPrice();
        double spread = spreadCalc.optimalSpread(inventory.getPosition(), getVolatility());
        double skew = inventory.getSkew();
        return new Quote(mid - spread/2 + skew, mid + spread/2 + skew);
    }
    
    public void onFill(Order order) {
        inventory.update(order);
        refreshQuotes();
    }
}

public class InventoryManager {
    private int position;
    private double averageCost;
    private double realizedPnL;
    
    public double getSkew() { ... } // Bias quotes to reduce inventory
}
```

---

## Variation 3: Dark Pool
**Learning Value:** Practices non-displayed order matching, information hiding, and reference-price-based execution.

### Additional Requirements
- Hidden order book (no visible bids/asks)
- Block trade support for large orders
- Minimal market impact (no price signaling)
- Midpoint matching (execute at midpoint of public best bid/ask)
- Minimum quantity thresholds for orders

### Design Changes
- Add `DarkPool` with non-displayed order book
- Add `MidpointMatcher` that crosses orders at NBBO midpoint
- Add `MinimumQuantity` filter on order acceptance
- Add `BlockTrade` for negotiated large trades
- Add reference price feed from lit exchange (NBBO)

### Solution Approach
A dark pool is a non-displayed venue where orders are not visible to other participants. Orders are matched at the midpoint of the National Best Bid and Offer (NBBO) from lit exchanges, ensuring fair pricing without revealing intent. Large institutional orders benefit because their size is hidden, avoiding market impact (the price movement that occurs when a large order becomes visible). Matching occurs when a buy order's limit price >= midpoint and a sell order's limit price <= midpoint. Minimum quantity thresholds ensure only meaningful block-size orders participate. The system subscribes to a market data feed for real-time NBBO updates.

### Key Classes to Add
```java
public class DarkPool {
    private List<HiddenOrder> buyOrders;
    private List<HiddenOrder> sellOrders;
    private NbboFeed nbboFeed;
    private int minimumQuantity;
    
    public void submitOrder(HiddenOrder order) {
        if (order.getQuantity() < minimumQuantity) reject(order);
        attemptMatch(order);
    }
    
    private void attemptMatch(HiddenOrder incoming) {
        double midpoint = nbboFeed.getMidpoint();
        // Match at midpoint if both sides agree
    }
}

public class HiddenOrder {
    private Trader trader;
    private OrderSide side;
    private int quantity;
    private double limitPrice; // or MARKET for midpoint pegged
    private int minimumFillQuantity;
}
```

---

## Variation 4: Stop-Loss/Take-Profit
**Learning Value:** Explores trade-offs between automation and control in conditional order triggering and management.

### Additional Requirements
- Conditional orders that activate at a trigger price
- Stop-loss: sell when price drops below threshold
- Take-profit: sell when price rises above threshold
- Trailing stop: dynamic stop that follows price movement
- OCO (One-Cancels-Other): linked stop-loss and take-profit

### Design Changes
- Add `ConditionalOrder` with trigger price and condition
- Add `TriggerMonitor` that watches market prices
- Add `TrailingStop` with dynamic trigger recalculation
- Add `OCOGroup` linking two orders (cancel one when other executes)
- Modify `StockExchange` to process triggered orders

### Solution Approach
Conditional orders sit dormant until their trigger condition is met. A TriggerMonitor watches the last traded price (or bid/ask) and evaluates all pending conditional orders. When triggered, the conditional order becomes a regular market or limit order and enters the order book. Trailing stops adjust their trigger price as the market moves favorably (the stop "trails" the price by a fixed amount or percentage). OCO groups link a stop-loss and take-profit; when one triggers and executes, the other is automatically cancelled. The monitor must be efficient (e.g., sorted by trigger price) to handle many conditional orders.

### Key Classes to Add
```java
public class ConditionalOrder {
    private Order underlyingOrder;
    private TriggerCondition condition;
    private double triggerPrice;
    private boolean triggered;
    
    public boolean checkTrigger(double currentPrice) {
        return condition.isMet(currentPrice, triggerPrice);
    }
}

public class TrailingStop extends ConditionalOrder {
    private double trailAmount;
    private double highWaterMark;
    
    public void onPriceUpdate(double price) {
        if (price > highWaterMark) {
            highWaterMark = price;
            setTriggerPrice(highWaterMark - trailAmount);
        }
    }
}

public class OCOGroup {
    private ConditionalOrder stopLoss;
    private ConditionalOrder takeProfit;
    
    public void onOrderTriggered(ConditionalOrder triggered) {
        ConditionalOrder other = (triggered == stopLoss) ? takeProfit : stopLoss;
        other.cancel();
    }
}
```

---

## Variation 5: Pre-Market/After-Hours Trading
**Learning Value:** Deepens understanding of session-based rule switching, order type restrictions, and circuit breaker design.

### Additional Requirements
- Extended trading hours (pre-market 4am-9:30am, after-hours 4pm-8pm)
- Different rules: limit orders only, no market orders
- Potentially wider spreads and lower liquidity
- Separate session order books or carry-over
- Price limits/circuit breakers for volatile moves

### Design Changes
- Add `TradingSession` enum (PRE_MARKET, REGULAR, AFTER_HOURS, CLOSED)
- Add `SessionManager` controlling which operations are allowed
- Modify order validation to restrict order types by session
- Add `CircuitBreaker` for extreme price movements
- Add session-aware order book (separate or unified with restrictions)

### Solution Approach
The exchange operates in distinct sessions with different rules. A SessionManager tracks the current session based on time of day and controls which order types are accepted. During extended hours, only limit orders are allowed (no market orders) to protect against thin liquidity. The order book may be separate per session (pre-market orders don't interact with regular-hours orders) or unified with carry-over. Circuit breakers halt trading if prices move beyond a threshold percentage within a time window. Liquidity is typically lower in extended hours, resulting in wider spreads and potentially less favorable execution.

### Key Classes to Add
```java
public class SessionManager {
    private TradingSession currentSession;
    private Map<TradingSession, SessionRules> rules;
    
    public TradingSession getCurrentSession() {
        // Determine based on current time
    }
    
    public boolean isOrderAllowed(Order order) {
        SessionRules currentRules = rules.get(currentSession);
        return currentRules.isOrderTypeAllowed(order.getType());
    }
}

public class SessionRules {
    private Set<OrderType> allowedOrderTypes;
    private boolean marketOrdersAllowed;
    private double maxPriceDeviation; // circuit breaker threshold
    
    public boolean isOrderTypeAllowed(OrderType type) { ... }
}

public class CircuitBreaker {
    private double threshold; // e.g., 10% move
    private Duration window;
    private double referencePrice;
    
    public boolean shouldHalt(double currentPrice) {
        double move = Math.abs(currentPrice - referencePrice) / referencePrice;
        return move >= threshold;
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
