/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Expense.java — Represents an expense with payer and participants

import java.util.List;

public class Expense {
    private final String payer;                           // final = immutable once created; safe to share across threads
    private final long amountCents;                       // final = use cents (long) to avoid floating-point races
    private final List<String> participants;              // final = participant list won't change after construction

    public Expense(String payer, long amountCents, List<String> participants) {
        this.payer = payer;
        this.amountCents = amountCents;
        this.participants = participants;
    }

    public String getPayer() { return payer; }
    public long getAmountCents() { return amountCents; }
    public List<String> getParticipants() { return participants; }
}
