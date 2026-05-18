# ATM Machine - Variations

## Variation 1: Multi-currency ATM
**Learning Value:** Teaches multi-denomination handling, currency conversion, and cash inventory balancing across types.

### Additional Requirements
- Support multiple currencies (USD, EUR, GBP, JPY)
- Real-time exchange rate fetching
- Currency conversion with spread/markup
- Denomination management per currency
- Display amounts in user's preferred currency

### Design Changes
- Add `Currency` enum and `ExchangeRateService` interface
- Modify `CashDispenser` to maintain separate denomination slots per currency
- Add `CurrencyConverter` class with Strategy pattern for different rate providers
- Modify `Account` to support multi-currency balances

### Solution Approach
Create an `ExchangeRateProvider` interface with implementations for different rate sources. The `CashDispenser` maintains a map of currency to available denominations. When a user requests withdrawal in a foreign currency, the system calculates the equivalent in the account's base currency using the current exchange rate plus a spread. The `Transaction` records both the source and target currencies with the applied rate. Use the Strategy pattern for selecting the best denomination combination per currency.

### Key Classes to Add
```java
public class CurrencyConverter {
    private ExchangeRateProvider rateProvider;
    private double spreadPercentage;

    public Money convert(Money amount, Currency targetCurrency) {
        double rate = rateProvider.getRate(amount.getCurrency(), targetCurrency);
        double converted = amount.getAmount() * rate * (1 + spreadPercentage);
        return new Money(converted, targetCurrency);
    }
}
```

---

## Variation 2: Cardless Withdrawal (UPI/OTP)
**Learning Value:** Introduces token-based authentication, OTP verification, and session management without physical cards.

### Additional Requirements
- Phone number-based identification
- OTP generation and verification with expiry
- UPI PIN as secondary authentication
- QR code scanning at ATM
- Transaction limits for cardless withdrawals (lower than card-based)

### Design Changes
- Add `AuthenticationStrategy` interface (Card-based vs Cardless)
- Add `OTPService` for generation, storage, and verification
- Add `UPIHandler` for UPI-based authentication flow
- Modify ATM state machine to handle alternative auth flows

### Solution Approach
Implement an `AuthenticationStrategy` interface with `CardAuthentication` and `CardlessAuthentication` implementations. The cardless flow starts when a user initiates a request from their mobile app, generating a reference code and OTP. At the ATM, the user enters their phone number and OTP. The `OTPService` validates the OTP against the stored value, checking expiry (typically 5-10 minutes). On success, the ATM proceeds to the standard withdrawal flow with potentially lower transaction limits.

### Key Classes to Add
```java
public class OTPService {
    private Map<String, OTPEntry> activeOTPs; // phone -> OTP
    private int otpLength = 6;
    private long expiryMillis = 300_000; // 5 minutes

    public String generateOTP(String phoneNumber) {
        String otp = generateSecureRandom(otpLength);
        activeOTPs.put(phoneNumber, new OTPEntry(otp, System.currentTimeMillis()));
        return otp;
    }

    public boolean verifyOTP(String phoneNumber, String enteredOTP) {
        OTPEntry entry = activeOTPs.get(phoneNumber);
        if (entry == null || entry.isExpired(expiryMillis)) return false;
        return entry.getOtp().equals(enteredOTP);
    }
}
```

---

## Variation 3: Check Deposit
**Learning Value:** Practices document processing workflows, image capture, and asynchronous verification pipelines.

### Additional Requirements
- Image scanning and OCR for check details
- MICR line parsing (routing number, account number, check number)
- Hold periods based on check amount and account history
- Partial availability (first $200 immediately, rest after clearing)
- Duplicate check detection
- Endorsement verification

### Design Changes
- Add `CheckDeposit` transaction type
- Add `CheckScanner` interface for image processing
- Add `HoldPolicy` strategy for determining fund availability
- Add `CheckValidator` for duplicate detection and MICR parsing
- Modify `Account` to support pending/available balance distinction

### Solution Approach
When a check is inserted, the `CheckScanner` captures front/back images and extracts MICR data. The `CheckValidator` checks for duplicates using check number + routing number + amount as a composite key. The `HoldPolicy` determines the hold period based on factors like check amount, account age, and deposit history. The account balance is split into "available" and "pending" amounts. A background process simulates check clearing after the hold period expires, moving funds from pending to available.

### Key Classes to Add
```java
public class CheckDepositService {
    private CheckScanner scanner;
    private CheckValidator validator;
    private HoldPolicy holdPolicy;

    public DepositResult deposit(Check check, Account account) {
        ScanResult scan = scanner.scan(check);
        if (!validator.isValid(scan)) throw new InvalidCheckException();
        if (validator.isDuplicate(scan)) throw new DuplicateCheckException();

        HoldDetails hold = holdPolicy.calculateHold(scan.getAmount(), account);
        account.addPendingDeposit(scan.getAmount(), hold.getAvailableImmediately());
        return new DepositResult(scan.getAmount(), hold);
    }
}
```

---

## Variation 4: ATM Network (Interbank)
**Learning Value:** Explores trade-offs between decentralization and consistency in interbank transaction networks.

### Additional Requirements
- Cross-bank transaction routing
- Network fee calculation (surcharges, foreign ATM fees)
- Real-time authorization with issuing bank
- Settlement and reconciliation between banks
- Network failover and timeout handling
- Daily/monthly fee caps

### Design Changes
- Add `ATMNetwork` class as intermediary between ATM and banks
- Add `BankAdapter` interface for different bank APIs
- Add `FeeCalculator` with rules for domestic/international/network fees
- Add `SettlementService` for end-of-day reconciliation
- Add `AuthorizationRequest`/`AuthorizationResponse` message types

### Solution Approach
The ATM network acts as a message router between the ATM and the card-issuing bank. When a card is inserted, the BIN (first 6 digits) identifies the issuing bank. The `ATMNetwork` routes an `AuthorizationRequest` to the appropriate `BankAdapter`. The `FeeCalculator` applies network fees (ATM owner surcharge + issuing bank fee). Transactions are logged for end-of-day settlement via the `SettlementService`, which nets out inter-bank obligations. Implement timeout handling with automatic reversal if the response is not received within the SLA.

### Key Classes to Add
```java
public class ATMNetwork {
    private Map<String, BankAdapter> bankAdapters; // BIN prefix -> adapter
    private FeeCalculator feeCalculator;
    private SettlementLedger ledger;

    public AuthorizationResponse authorize(AuthorizationRequest request) {
        String bin = request.getCardNumber().substring(0, 6);
        BankAdapter issuer = bankAdapters.get(bin);
        Fee networkFee = feeCalculator.calculate(request, isOnUs(bin));
        AuthorizationResponse response = issuer.authorize(request);
        if (response.isApproved()) {
            ledger.record(request, networkFee);
        }
        return response;
    }
}
```

---

## Variation 5: Anti-fraud Detection
**Learning Value:** Deepens understanding of anomaly detection, rule-based alerting, and real-time fraud prevention patterns.

### Additional Requirements
- Real-time transaction scoring
- Velocity checks (rapid successive withdrawals)
- Geolocation-based anomaly detection
- Amount pattern analysis (unusual amounts)
- Card skimming detection (multiple different cards, same ATM, short window)
- Automatic card blocking on suspicious activity

### Design Changes
- Add `FraudDetectionEngine` with pluggable rules
- Add `RiskRule` interface (Chain of Responsibility pattern)
- Add `TransactionProfile` for building user behavior baselines
- Add `AlertService` for notifying fraud team
- Add `VelocityTracker` for monitoring transaction frequency

### Solution Approach
Implement a `FraudDetectionEngine` that evaluates each transaction against a chain of `RiskRule` implementations. Each rule returns a risk score (0-100). Rules include: `VelocityRule` (too many transactions in time window), `GeolocationRule` (transaction far from usual locations), `AmountRule` (unusual amount patterns), and `TimeRule` (transactions at unusual hours). The scores are aggregated with configurable weights. If the total exceeds a threshold, the transaction is either blocked, or a step-up authentication (extra PIN, security question) is required. The system maintains a `TransactionProfile` per account that adapts over time.

### Key Classes to Add
```java
public class FraudDetectionEngine {
    private List<RiskRule> rules;
    private double blockThreshold = 80.0;
    private double challengeThreshold = 50.0;

    public FraudDecision evaluate(Transaction txn, TransactionProfile profile) {
        double totalScore = 0;
        List<RiskSignal> signals = new ArrayList<>();
        for (RiskRule rule : rules) {
            RiskAssessment assessment = rule.assess(txn, profile);
            totalScore += assessment.getScore() * rule.getWeight();
            if (assessment.getScore() > 0) signals.add(assessment.getSignal());
        }
        if (totalScore >= blockThreshold) return FraudDecision.BLOCK;
        if (totalScore >= challengeThreshold) return FraudDecision.CHALLENGE;
        return FraudDecision.ALLOW;
    }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
