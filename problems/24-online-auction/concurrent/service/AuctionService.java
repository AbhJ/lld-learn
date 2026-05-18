/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/AuctionService.java — AtomicReference<Bid> with CAS for highest bid

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionService {
    private final AtomicReference<Bid> currentHighest = new AtomicReference<>(null); // AtomicReference = CAS-based lock-free highest bid
    private final AtomicInteger totalBidsAttempted = new AtomicInteger(0); // AtomicInteger = lock-free counter for stats
    private final AtomicInteger successfulBids = new AtomicInteger(0);    // AtomicInteger = tracks successful CAS updates

    /**
     * Place a bid. Only succeeds if bid amount > current highest.
     * Uses CAS loop to prevent two simultaneous bidders both seeing old value.
     */
    public boolean placeBid(Bid newBid) {
        totalBidsAttempted.incrementAndGet();
        while (true) {
            Bid current = currentHighest.get();
            if (current != null && newBid.getAmountCents() <= current.getAmountCents()) {
                return false; // bid too low
            }
            // CAS: only update if still the same as what we read
            if (currentHighest.compareAndSet(current, newBid)) {
                successfulBids.incrementAndGet();
                return true;
            }
            // CAS failed — someone else updated, re-read and retry
        }
    }

    public Bid getCurrentHighest() {
        return currentHighest.get();
    }

    public int getTotalBidsAttempted() { return totalBidsAttempted.get(); }
    public int getSuccessfulBids() { return successfulBids.get(); }
}
