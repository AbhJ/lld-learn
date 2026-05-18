/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Auction.java — Manages bidding with linear scan for highest bid
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Auction {
    private String id;
    private Item item;
    private AuctionState state;                           // private = tracks lifecycle; controls valid operations
    private double startingBid;
    private double minIncrement;
    private List<Bid> bids;                               // private = full bid history
    private List<Bidder> watchers;                        // private = Observer pattern: notified on events
    private WinnerStrategy winnerStrategy;                // private = Strategy pattern: pluggable winner logic

    public Auction(String id, Item item, double startingBid, double minIncrement, WinnerStrategy strategy) {
        this.id = id; this.item = item; this.state = AuctionState.UPCOMING;
        this.startingBid = startingBid; this.minIncrement = minIncrement;
        this.bids = new ArrayList<>(); this.watchers = new ArrayList<>();
        this.winnerStrategy = strategy;
    }

    public void start() {
        if (state != AuctionState.UPCOMING) throw new IllegalStateException("Can only start UPCOMING auction");
        state = AuctionState.ACTIVE;
        notifyWatchers("Auction for " + item.getName() + " is now ACTIVE!");
    }

    public boolean placeBid(Bidder bidder, double amount) {
        if (state != AuctionState.ACTIVE) return false;
        // Naive: linear scan to find current highest
        double currentHighest = getCurrentHighestBid();
        double minRequired = currentHighest > 0 ? currentHighest + minIncrement : startingBid;
        if (amount < minRequired) return false;
        bids.add(new Bid(bidder, amount, false));
        addWatcher(bidder);
        return true;
    }

    // Naive: O(n) scan every time we need the highest bid
    public double getCurrentHighestBid() {
        double max = 0;
        for (Bid bid : bids) max = Math.max(max, bid.getAmount());
        return max;
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
