/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConsoleGameObserver.java — Console logger; demonstrates observing tic-tac-toe events

class ConsoleGameObserver implements GameObserver {
    @Override
    public void onMove(Player player, int row, int col, Symbol symbol) {
        System.out.println("  [event] " + player.getName() + " (" + symbol + ") -> (" + row + "," + col + ")");
    }

    @Override
    public void onWin(Player winner, Symbol symbol) {
        System.out.println("  [event] WIN: " + winner.getName() + " with " + symbol);
    }

    @Override
    public void onDraw() {
        System.out.println("  [event] DRAW: board full, no winner");
    }
}
