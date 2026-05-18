/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/GameObserver.java — Observer contract for tic-tac-toe game events
//
// Subscribers receive notifications when moves are made and when the game
// reaches a terminal state (win/draw). Examples: a console logger, a UI
// renderer, an analytics sink.

interface GameObserver {
    /** A player just made a valid move at (row, col) using their symbol. */
    void onMove(Player player, int row, int col, Symbol symbol);

    /** A player has won the game. */
    void onWin(Player winner, Symbol symbol);

    /** The game ended in a draw (board full, no winner). */
    void onDraw();
}
