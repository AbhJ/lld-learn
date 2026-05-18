/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Auction.java — Auction with PriorityQueue for O(1) highest bid access
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Auction {
    private String id;
    private Item item;
    private AuctionState state;
    private double startingBid;
    private double minIncrement;
    private List<Bid> bids;
    private List<Bidder> watchers;
    private WinnerStrategy winnerStrategy;

    // Max-heap gives O(1) access to highest bid instead of O(n) scan
    private PriorityQueue<Bid> bidHeap;                   // PriorityQueue = max-heap; peek() is O(1) for top bid
    // AtomicReference enables lock-free concurrent bid validation
    private AtomicReference<Double> currentHighest;       // AtomicReference = CAS-based lock-free highest tracking

    public Auction(String id, Item item, double startingBid, double minIncrement, WinnerStrategy strategy) {
        this.id = id; this.item = item; this.state = AuctionState.UPCOMING;
        this.startingBid = startingBid; this.minIncrement = minIncrement;
        this.bids = new ArrayList<>(); this.watchers = new ArrayList<>();
        this.winnerStrategy = strategy;
        // Max-heap: highest bid at top
        this.bidHeap = new PriorityQueue<>((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        this.currentHighest = new AtomicReference<>(0.0);
    }

    public void start() {
        if (state != AuctionState.UPCOMING) throw new IllegalStateException("Can only start UPCOMING auction");
        state = AuctionState.ACTIVE;
        notifyWatchers("Auction for " + item.getName() + " is now ACTIVE!");
    }

    public boolean placeBid(Bidder bidder, double amount) {
        if (state != AuctionState.ACTIVE) return false;

        // WHY: CAS loop for thread-safe bid validation without locks
        while (true) {
            double highest = currentHighest.get();
            double minRequired = highest > 0 ? highest + minIncrement : startingBid;
            if (amount < minRequired) return false;

            // Atomically update the highest bid
            if (currentHighest.compareAndSet(highest, amount)) {
                Bid bid = new Bid(bidder, amount, false);
                synchronized (bids) {
                    bids.add(bid);
                    bidHeap.offer(bid);
                }
                addWatcher(bidder);
                return true;
            }
            // CAS failed — another bid came in, retry with new highest
        }
    }

    // O(1): just read the atomic reference
    public double getCurrentHighestBid() {
        return currentHighest.get();
    }

    public WinnerStrategy.WinnerResult close() {
        if (state != AuctionState.ACTIVE) throw new IllegalStateException("Can only close ACTIVE auction");
        state = AuctionState.CLOSED;
        WinnerStrategy.WinnerResult result = winnerStrategy.determineWinner(bids, item.getReservePrice());
        if (result != null) notifyWatchers("Winner: " + result.getWinner().getName());
        else notifyWatchers("Reserve not met. No winner.");
        return result;
    }

    private void notifyWatchers(String message) {
        for (Bidder w : watchers) w.notify(message);
    }

    public void addWatcher(Bidder bidder) { if (!watchers.contains(bidder)) watchers.add(bidder); }
    public String getId() { return id; }
    public Item getItem() { return item; }
    public AuctionState getState() { return state; }
    public List<Bid> getBids() { return Collections.unmodifiableList(bids); }
}
