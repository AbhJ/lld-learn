/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Bid.java — A bid placed on an auction
public class Bid {
    private Bidder bidder;
    private double amount;                                // compared in PriorityQueue for heap ordering
    private long timestamp;                               // tiebreaker when amounts are equal
    private boolean isAutoBid;

    public Bid(Bidder bidder, double amount, boolean isAutoBid) {
        this.bidder = bidder; this.amount = amount; this.timestamp = System.nanoTime(); this.isAutoBid = isAutoBid;
    }

    public Bidder getBidder() { return bidder; }
    public double getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }
    public boolean isAutoBid() { return isAutoBid; }

    @Override public String toString() { return String.format("$%.2f by %s%s", amount, bidder.getName(), isAutoBid ? " (auto)" : ""); }
}
