# Payment Gateway - Variations

## Variation 1: Recurring Payments (Subscriptions)
**Learning Value:** Teaches subscription lifecycle management, billing cycle handling, and retry logic for failed payments.

### Additional Requirements
- Billing cycles (monthly, yearly, custom intervals)
- Automatic retry on payment failure with backoff
- Dunning management (escalating notifications before cancellation)
- Proration for mid-cycle plan changes
- Grace period before service suspension

### Design Changes
- Add `Subscription` class with plan, billing cycle, and status
- Add `BillingScheduler` to trigger charges at intervals
- Add `RetryPolicy` with configurable backoff strategy
- Add `DunningManager` for failed payment escalation
- Add `ProrationCalculator` for plan upgrade/downgrade

### Solution Approach
A subscription links a customer to a plan with a billing cycle. The BillingScheduler checks daily for subscriptions due for charge and initiates payment. On failure, the RetryPolicy schedules retries with exponential backoff (e.g., retry after 1, 3, 7 days). The DunningManager sends escalating notifications: payment failed, payment method expiring, service will be suspended. After all retries are exhausted, the subscription moves to a grace period, then cancellation. Proration calculates the partial charges when a customer upgrades or downgrades mid-cycle based on days remaining.

### Key Classes to Add
```java
public class Subscription {
    private String id;
    private String customerId;
    private Plan plan;
    private BillingCycle cycle;
    private SubscriptionStatus status; // ACTIVE, PAST_DUE, CANCELLED
    private LocalDate currentPeriodEnd;
    
    public void charge() { ... }
    public void changePlan(Plan newPlan) { ... }
    public void cancel(boolean immediate) { ... }
}

public class RetryPolicy {
    private int maxRetries;
    private Duration[] backoffIntervals;
    private int currentAttempt;
    
    public Duration getNextRetryDelay() { ... }
    public boolean hasRetriesRemaining() { ... }
}

public class DunningManager {
    public void onPaymentFailed(Subscription sub, int attemptNumber) {
        // Send escalating notifications
        // Eventually suspend/cancel
    }
}
```

---

## Variation 2: Multi-Currency
**Learning Value:** Introduces currency conversion, exchange rate management, and multi-currency settlement.

### Additional Requirements
- Accept payments in customer's local currency
- FX rate locking at time of charge
- Settlement in merchant's preferred currency
- Cross-border fee calculation
- Currency conversion transparency (show customer the rate)

### Design Changes
- Add `CurrencyService` for real-time FX rates
- Add `FXQuote` with locked rate and expiration
- Add `SettlementConfig` per merchant (preferred currency)
- Add `FeeCalculator` for cross-border and conversion fees
- Modify `Transaction` to store both charge and settlement currencies

### Solution Approach
When a customer pays in their local currency, the system locks an FX rate for a short window (e.g., 30 seconds). The transaction records both the charge amount/currency and the settlement amount/currency. The merchant receives funds in their configured settlement currency after conversion. Cross-border fees are calculated based on currency pair, payment method, and region. The customer sees the exact exchange rate and any fees before confirming. Settlement happens in batches, aggregating transactions and converting at the locked rates. Rate margins provide revenue for the payment processor.

### Key Classes to Add
```java
public class CurrencyService {
    public FXQuote getQuote(Currency from, Currency to, double amount) { ... }
    public double convert(FXQuote quote) { ... }
}

public class FXQuote {
    private Currency sourceCurrency;
    private Currency targetCurrency;
    private double rate;
    private double sourceAmount;
    private double targetAmount;
    private Instant expiresAt;
    
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
}

public class MultiCurrencyTransaction extends Transaction {
    private Money chargeAmount; // what customer paid
    private Money settlementAmount; // what merchant receives
    private FXQuote appliedRate;
    private Money crossBorderFee;
}
```

---

## Variation 3: Split Payments
**Learning Value:** Practices fund splitting, multi-party disbursement, and proportional allocation algorithms.

### Additional Requirements
- Marketplace model: buyer pays, split between seller and platform
- Escrow: hold funds until delivery confirmed
- Commission deduction before seller payout
- Multi-party splits (e.g., seller + shipping partner + platform)
- Configurable split rules per merchant/product

### Design Changes
- Add `SplitRule` defining how to divide payment among parties
- Add `EscrowAccount` for holding funds pending release
- Add `PayoutSchedule` for distributing funds to recipients
- Add `Commission` calculation per transaction
- Modify `Transaction` to track multiple recipients

### Solution Approach
When a buyer makes a payment in a marketplace, the full amount is captured and held. A SplitRule defines how the payment is divided (e.g., 85% to seller, 10% platform fee, 5% shipping). Funds may be held in escrow until a release condition is met (e.g., delivery confirmation). Once released, the system calculates each party's share after deducting applicable commissions and fees, then initiates payouts according to each recipient's schedule (instant, daily, weekly). Refunds reverse the split proportionally. The system handles edge cases like partial refunds and disputes.

### Key Classes to Add
```java
public class SplitRule {
    private List<SplitRecipient> recipients;
    
    public Map<String, Double> calculateSplit(double totalAmount) { ... }
}

public class SplitRecipient {
    private String accountId;
    private SplitType type; // PERCENTAGE, FIXED_AMOUNT
    private double value;
}

public class EscrowAccount {
    private String transactionId;
    private double heldAmount;
    private EscrowStatus status; // HELD, RELEASED, REFUNDED
    private String releaseCondition;
    
    public void release() { ... }
    public void refund() { ... }
}

public class PayoutService {
    public void distributeFunds(Transaction txn, SplitRule rule) { ... }
}
```

---

## Variation 4: Tokenization
**Learning Value:** Explores trade-offs between security and usability in sensitive data tokenization and vault design.

### Additional Requirements
- Replace sensitive card data with non-sensitive tokens
- PCI DSS compliance (minimize card data exposure)
- Token vault with encryption at rest
- Network tokens (scheme-level tokenization via Visa/MC)
- Token lifecycle management (update on card renewal)

### Design Changes
- Add `TokenVault` for secure token-to-card mapping
- Add `Tokenizer` that generates and resolves tokens
- Add `NetworkToken` integration with card networks
- Add `TokenLifecycle` for expiry, update, and revocation
- Modify payment flow to use tokens instead of raw card numbers

### Solution Approach
When a customer enters their card details, the tokenizer immediately replaces the PAN (card number) with a random, non-reversible token. The original card data is stored in an isolated, heavily encrypted vault with strict access controls. Subsequent charges use only the token; the vault resolves it to real card data at charge time within the secure environment. Network tokens are obtained from Visa/Mastercard and provide better authorization rates since they auto-update when cards are reissued. Token lifecycle management handles expiry notifications, automatic updates via account updater services, and revocation on customer request.

### Key Classes to Add
```java
public class TokenVault {
    private Map<String, EncryptedCardData> store;
    
    public String tokenize(CardData card) {
        String token = generateToken();
        store.put(token, encrypt(card));
        return token;
    }
    
    public CardData detokenize(String token) {
        EncryptedCardData encrypted = store.get(token);
        return decrypt(encrypted);
    }
}

public class Tokenizer {
    private TokenVault vault;
    
    public String createToken(CardData card) { ... }
    public PaymentMethod resolveForPayment(String token) { ... }
    public void revokeToken(String token) { ... }
}

public class NetworkTokenService {
    public NetworkToken provision(CardData card) { ... }
    public void onCardUpdated(String token, CardData newCard) { ... }
}
```

---

## Variation 5: 3D Secure / SCA
**Learning Value:** Deepens understanding of multi-factor authentication flows, regulatory compliance, and challenge protocols.

### Additional Requirements
- Strong Customer Authentication (SCA) compliance (EU PSD2)
- Challenge flow: redirect customer to bank for verification
- Frictionless flow: low-risk transactions auto-approved
- Exemption management (low value, trusted beneficiary, TRA)
- Liability shift from merchant to issuer on authenticated transactions

### Design Changes
- Add `ThreeDSecureService` orchestrating the authentication flow
- Add `ChallengeFlow` handling bank redirect and verification
- Add `ExemptionEngine` determining when SCA can be skipped
- Add `RiskAssessment` scoring transactions for frictionless approval
- Modify payment flow to insert authentication step before capture

### Solution Approach
Before processing a payment, the system checks if SCA is required based on regulation, amount, and exemptions. If required, it initiates 3DS authentication: sends transaction details to the card issuer via the 3DS server. The issuer either approves frictionlessly (low risk) or triggers a challenge (OTP, biometric, app approval). The customer completes the challenge, and the authentication result (success/failure/attempt) is returned. On success, the payment proceeds with a liability shift to the issuer for fraud. Exemptions (e.g., transactions under 30 EUR, recurring payments after initial auth, trusted beneficiaries) allow skipping SCA when applicable.

### Key Classes to Add
```java
public class ThreeDSecureService {
    private ExemptionEngine exemptionEngine;
    
    public AuthenticationResult authenticate(Transaction txn) {
        if (exemptionEngine.isExempt(txn)) {
            return AuthenticationResult.exempt();
        }
        // Initiate 3DS flow with issuer
        ThreeDSResponse response = initiate3DS(txn);
        if (response.isFrictionless()) return AuthenticationResult.frictionless();
        return AuthenticationResult.challengeRequired(response.getChallengeUrl());
    }
}

public class ExemptionEngine {
    public boolean isExempt(Transaction txn) {
        // Check: low value (<30 EUR), trusted beneficiary,
        // TRA (Transaction Risk Analysis), recurring
    }
}

public class ChallengeFlow {
    private String challengeUrl;
    private String transactionId;
    
    public void redirectCustomer() { ... }
    public AuthenticationResult onChallengeComplete(String authCode) { ... }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
