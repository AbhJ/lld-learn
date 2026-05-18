/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/BoardFactory.java — Factory creating pre-configured board layouts
// DESIGN PATTERN: Factory

class BoardFactory {

    public static Board createStandardBoard() { // static factory = no instance needed; returns configured Board
        Board board = new Board(100);
        board.addSnake(99, 54);
        board.addSnake(70, 55);
        board.addSnake(52, 42);
        board.addSnake(25, 2);
        board.addSnake(95, 72);
        board.addLadder(6, 25);
        board.addLadder(11, 40);
        board.addLadder(20, 77);
        board.addLadder(46, 90);
        board.addLadder(60, 85);
        return board;
    }

    public static Board createSmallBoard() {
        Board board = new Board(30);
        board.addSnake(27, 5);
        board.addSnake(21, 9);
        board.addSnake(17, 3);
        board.addLadder(2, 15);
        board.addLadder(8, 22);
        board.addLadder(14, 26);
        return board;
    }

    public static Board createEmptyBoard(int size) {
        return new Board(size);
    }
}
