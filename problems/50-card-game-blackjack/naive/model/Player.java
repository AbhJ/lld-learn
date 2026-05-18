/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Player.java — Represents a player with chips and current hand
public class Player {
    private String name;    // private = player name encapsulated
    private Hand hand;      // private = current hand managed internally
    private double balance; // private = chip balance only changes via addToBalance()

    public Player(String name, double initialBalance) { this.name = name; this.hand = new Hand(); this.balance = initialBalance; }
    public String getName() { return name; }
    public Hand getHand() { return hand; }
    public double getBalance() { return balance; }
    public void addToBalance(double amount) { this.balance += amount; }
    public boolean canBet(double amount) { return balance >= amount; }
    public void resetHand() { hand.clear(); }
}
