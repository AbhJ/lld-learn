/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/AuctionSystem.java — Facade managing auctions and bidders
import java.util.*;

public class AuctionSystem {
    private Map<String, Auction> auctions;                // HashMap = O(1) auction lookup by ID
    private Map<String, Bidder> bidders;                  // HashMap = O(1) bidder lookup by ID

    public AuctionSystem() { this.auctions = new HashMap<>(); this.bidders = new HashMap<>(); }

    public Bidder registerBidder(String id, String name) {
        Bidder b = new Bidder(id, name); bidders.put(id, b); return b;
    }

    public Auction createAuction(String id, Item item, double startingBid, double minIncrement, WinnerStrategy strategy) {
        Auction a = new Auction(id, item, startingBid, minIncrement, strategy); auctions.put(id, a); return a;
    }

    public Auction getAuction(String id) { return auctions.get(id); }
}
