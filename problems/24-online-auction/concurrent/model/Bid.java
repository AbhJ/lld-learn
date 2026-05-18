/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Bid.java — Immutable bid with bidder name, amount, and timestamp

public class Bid {
    private final String bidder;                          // final = immutable; safe to publish across threads
    private final long amountCents;                       // final = immutable; no synchronization needed to read
    private final long timestamp;                         // final = capture time at creation; never changes

    public Bid(String bidder, long amountCents, long timestamp) {
        this.bidder = bidder;
        this.amountCents = amountCents;
        this.timestamp = timestamp;
    }

    public String getBidder() { return bidder; }
    public long getAmountCents() { return amountCents; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return bidder + ": $" + (amountCents / 100) + "." + String.format("%02d", amountCents % 100);
    }
}
