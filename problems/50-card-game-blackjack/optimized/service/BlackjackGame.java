/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BlackjackGame.java — Game with memoized scores and in-place deck shuffle
public class BlackjackGame {
    private Deck deck;          // private = in-place Fisher-Yates deck; zero GC on reset
    private Player player;      // private = the player in this game session
    private Dealer dealer;      // private = dealer with strategy-table decisions
    private GameState state;    // private = game phase controlled by game logic only
    private Bet currentBet;     // private = current wager for this round

    public BlackjackGame(Player player, DealerStrategy dealerStrategy, long seed) {
        this.deck = new Deck(seed);
        this.player = player; this.dealer = new Dealer(dealerStrategy);
        this.state = GameState.BETTING;
    }

    public GameState getState() { return state; }
    public Player getPlayer() { return player; }

    public boolean placeBet(double amount) {
        if (state != GameState.BETTING || !player.canBet(amount)) return false;
        currentBet = new Bet(amount); player.addToBalance(-amount);
        System.out.println("Bet: $" + String.format("%.0f", amount));
        return true;
    }

    public void deal() {
        if (state != GameState.BETTING || currentBet == null) return;
        state = GameState.DEALING;
        player.resetHand(); dealer.resetHand();
        player.getHand().addCard(deck.deal()); dealer.getHand().addCard(deck.deal());
        player.getHand().addCard(deck.deal()); dealer.getHand().addCard(deck.deal());
        // WHY: getScore() uses memoized value — O(1) after first computation
        System.out.println("Player: " + player.getHand() + " = " + player.getHand().getScore());

        if (player.getHand().isBlackjack()) {
            state = GameState.GAME_OVER;
            double winnings = currentBet.getAmount() + currentBet.getBlackjackWinAmount();
            player.addToBalance(winnings); currentBet.settle();
            System.out.println("BLACKJACK! +$" + String.format("%.0f", currentBet.getBlackjackWinAmount()));
        } else { state = GameState.PLAYING; }
    }

    public boolean hit() {
        if (state != GameState.PLAYING) return false;
        Card card = deck.deal(); player.getHand().addCard(card);
        System.out.println("Hit: " + card + " -> " + player.getHand().getScore());
        if (player.getHand().isBusted()) {
            System.out.println("BUST! -$" + String.format("%.0f", currentBet.getAmount()));
            state = GameState.GAME_OVER; currentBet.settle(); return false;
        }
        return true;
    }

    public void stand() {
        if (state != GameState.PLAYING) return;
        state = GameState.DEALER_TURN;
        System.out.println("Stand with " + player.getHand().getScore());
        System.out.println("Dealer: " + dealer.getHand() + " = " + dealer.getHand().getScore());
        // Pass the player's first card as the "upcard" the dealer's strategy may consult.
        // Chart-based strategies use it; simple threshold strategies ignore it (default).
        Card playerUpcard = player.getHand().getCards().isEmpty() ? null : player.getHand().getCards().get(0);
        while (dealer.shouldHit(playerUpcard)) {
            Card card = deck.deal(); dealer.getHand().addCard(card);
            System.out.println("Dealer hits: " + card + " -> " + dealer.getHand().getScore());
        }
        settleRound();
    }

    private void settleRound() {
        state = GameState.GAME_OVER;
        int ps = player.getHand().getScore(), ds = dealer.getHand().getScore();
        if (dealer.getHand().isBusted()) { player.addToBalance(currentBet.getAmount() + currentBet.getWinAmount()); System.out.println("Dealer busts! +$" + String.format("%.0f", currentBet.getWinAmount())); }
        else if (ps > ds) { player.addToBalance(currentBet.getAmount() + currentBet.getWinAmount()); System.out.println("Player wins! +$" + String.format("%.0f", currentBet.getWinAmount())); }
        else if (ds > ps) { System.out.println("Dealer wins! -$" + String.format("%.0f", currentBet.getAmount())); }
        else { player.addToBalance(currentBet.getAmount()); System.out.println("Push!"); }
        currentBet.settle();
        System.out.println("Balance: $" + String.format("%.0f", player.getBalance()));
    }

    // WHY: In-place shuffle on reset — no object allocation, no GC
    public void newRound() { state = GameState.BETTING; currentBet = null; if (deck.remaining() < 15) deck.reset(); }
}
