# Splitwise - Variations

## Variation 1: Minimum Transactions to Settle
**Learning Value:** Teaches graph-based debt simplification, minimum-edge optimization, and NP-hard approximation.

### Additional Requirements
- Simplify debts to minimize number of transactions
- Compute net amounts (what each person owes/is owed overall)
- Handle cycles in the debt graph
- Produce optimal settlement plan
- Support partial settlements

### Design Changes
- Add `DebtSimplifier` class using graph algorithms
- Add `NetBalance` computation across all group members
- Add `SettlementPlan` as an ordered list of transfers
- Modify `Group.getBalances()` to return simplified debts
- Add `SettlementOptimizer` using greedy or min-cost flow

### Solution Approach
Compute the net balance for each person (total owed to them minus total they owe). People with positive balances are creditors; negative are debtors. The greedy approach matches the largest debtor with the largest creditor repeatedly. A more optimal approach uses subset-sum to find groups whose net balances sum to zero, then settles within each group independently. This is NP-hard in general but practical for small groups. The simplified set of transactions achieves the same final balances with fewer transfers than the naive pairwise approach.

### Key Classes to Add
```java
public class DebtSimplifier {
    public List<Transfer> simplify(Map<String, Double> netBalances) {
        // Separate into creditors (positive) and debtors (negative)
        // Greedily match largest debtor with largest creditor
        // Each match produces one Transfer
    }
    
    public Map<String, Double> computeNetBalances(List<Expense> expenses) { ... }
}

public class Transfer {
    private String fromUserId;
    private String toUserId;
    private double amount;
}

public class SettlementPlan {
    private List<Transfer> transfers;
    public int getTransactionCount() { return transfers.size(); }
}
```

---

## Variation 2: Multi-Currency
**Learning Value:** Introduces currency conversion, exchange rate management, and multi-denomination settlement.

### Additional Requirements
- Expenses in different currencies within same group
- Real-time exchange rate integration
- Per-currency balances (avoid unnecessary conversions)
- Settlement currency preference per user
- Historical rate tracking for past expenses

### Design Changes
- Add `Currency` enum and `Money` value object (amount + currency)
- Add `ExchangeRateService` for rate lookups
- Modify `Balance` to track per-currency amounts
- Add `CurrencyConverter` for settlement calculations
- Store exchange rate at time of expense for auditability

### Solution Approach
Each expense stores its amount with a currency. Balances are tracked per-currency to avoid unnecessary conversions (if Alice owes Bob 10 EUR, she should pay in EUR). When cross-currency settlement is needed, the system uses current exchange rates. Each user can set a preferred settlement currency. The net balance calculation groups by currency first, then converts remaining balances to the preferred currency for final settlement. Historical rates are stored with each expense to ensure the split remains fair regardless of rate fluctuations after the fact.

### Key Classes to Add
```java
public class Money {
    private double amount;
    private Currency currency;
    
    public Money convertTo(Currency target, ExchangeRateService rates) { ... }
}

public class ExchangeRateService {
    private Map<CurrencyPair, Double> rates;
    
    public double getRate(Currency from, Currency to) { ... }
    public double getHistoricalRate(Currency from, Currency to, Instant at) { ... }
}

public class MultiCurrencyBalance {
    private Map<Currency, Double> balances;
    
    public Money getNetInCurrency(Currency target, ExchangeRateService rates) { ... }
}
```

---

## Variation 3: Recurring Expenses
**Learning Value:** Practices recurring event scheduling, automatic splitting, and subscription lifecycle tracking.

### Additional Requirements
- Monthly rent/utility splits that auto-create expenses
- Subscription sharing (streaming services, utilities)
- Configurable frequency (daily, weekly, monthly)
- Auto-split with same participants and ratios
- Pause/resume and end-date support

### Design Changes
- Add `RecurringExpense` with schedule and template
- Add `Scheduler` to trigger expense creation at intervals
- Add `ExpenseTemplate` storing split configuration
- Modify `SplitwiseService` to manage recurring expense lifecycle
- Add notification on auto-created expenses

### Solution Approach
A recurring expense is defined as a template (description, amount, payer, split strategy, participants) plus a schedule (frequency, start date, optional end date). A scheduler checks periodically (or uses cron-like scheduling) and creates actual expense instances from the template when due. Each generated expense is a normal expense that can be modified individually without affecting future occurrences. Users can pause, resume, or cancel recurring expenses. Notifications are sent when a new instance is created so participants can review.

### Key Classes to Add
```java
public class RecurringExpense {
    private String id;
    private ExpenseTemplate template;
    private Schedule schedule;
    private RecurringState state; // ACTIVE, PAUSED, CANCELLED
    
    public Expense generateNext() { ... }
    public void pause() { ... }
    public void resume() { ... }
}

public class Schedule {
    private Frequency frequency; // DAILY, WEEKLY, MONTHLY
    private LocalDate startDate;
    private LocalDate endDate; // nullable
    private LocalDate lastGenerated;
    
    public boolean isDue() { ... }
    public LocalDate getNextOccurrence() { ... }
}

public class ExpenseTemplate {
    private String description;
    private double amount;
    private String payerId;
    private SplitStrategy splitStrategy;
    private List<String> participantIds;
}
```

---

## Variation 4: Receipt Scanning
**Learning Value:** Explores trade-offs between automation accuracy and manual correction in OCR-based data extraction.

### Additional Requirements
- OCR integration to extract text from receipt photos
- Auto-detect merchant, date, total, and line items
- Per-item split (assign different items to different people)
- Confidence scores for extracted data with manual correction
- Receipt image storage and linking to expenses

### Design Changes
- Add `ReceiptScanner` interface with OCR implementation
- Add `ReceiptData` model with extracted fields
- Add `LineItem` class for individual items on a receipt
- Add `ItemSplit` allowing per-item participant assignment
- Modify `Expense` to optionally link to receipt and line items

### Solution Approach
When a user uploads a receipt photo, the OCR service extracts text and uses NLP/regex patterns to identify the merchant name, date, total amount, tax, tip, and individual line items with prices. Each extracted field has a confidence score; low-confidence fields are flagged for manual review. Once confirmed, users can assign line items to specific participants for a per-item split (e.g., Alice's steak goes only to Alice). The system calculates each person's share based on their assigned items plus an equal split of shared items (tax, tip). The receipt image is stored and linked to the expense for reference.

### Key Classes to Add
```java
public interface ReceiptScanner {
    ReceiptData scan(byte[] image);
}

public class ReceiptData {
    private String merchant;
    private LocalDate date;
    private double total;
    private double tax;
    private double tip;
    private List<LineItem> items;
    private Map<String, Double> confidenceScores;
}

public class LineItem {
    private String description;
    private double price;
    private int quantity;
    private List<String> assignedUserIds; // who this item is for
}

public class ItemizedSplit implements SplitStrategy {
    private Map<String, List<LineItem>> userItems;
    
    public Map<String, Double> split(double total, List<String> participants) { ... }
}
```

---

## Variation 5: Settlement via Payment Apps
**Learning Value:** Deepens understanding of payment integration, settlement workflows, and external API coordination.

### Additional Requirements
- Integration with UPI/Venmo/PayPal for direct settlement
- Auto-settle reminders on configurable schedule
- One-click pay to settle outstanding balance
- Payment confirmation and balance auto-update
- Support for partial payments

### Design Changes
- Add `PaymentProvider` interface (UPI, Venmo, PayPal adapters)
- Add `SettlementRequest` with payment link generation
- Add `ReminderService` for scheduled payment reminders
- Add `PaymentCallback` handler for confirmation webhooks
- Modify `Balance` to update on successful payment

### Solution Approach
Each user links their preferred payment app (UPI ID, Venmo handle, PayPal email). When settling, the system generates a payment request/link through the chosen provider's API. The debtor can pay with one click. A webhook or callback confirms the payment, and the system automatically updates balances. Reminders are sent on a configurable schedule (e.g., weekly) to users with outstanding balances above a threshold. Partial payments are supported by creating a settlement record for the partial amount and updating the remaining balance accordingly.

### Key Classes to Add
```java
public interface PaymentProvider {
    PaymentLink createPaymentRequest(String fromId, String toId, double amount);
    PaymentStatus checkStatus(String paymentId);
}

public class SettlementService {
    private Map<String, PaymentProvider> providers;
    private ReminderService reminderService;
    
    public PaymentLink initiateSettlement(String debtorId, String creditorId, double amount) { ... }
    public void onPaymentConfirmed(String paymentId) { ... }
}

public class ReminderService {
    private Map<String, ReminderConfig> userConfigs;
    
    public void scheduleReminder(String userId, double amount, String creditorId) { ... }
    public void sendReminders() { ... }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
