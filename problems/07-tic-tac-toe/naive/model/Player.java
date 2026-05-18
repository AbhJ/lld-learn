/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Player.java — Abstract player with Human and AI implementations

import java.util.Random;

abstract class Player {                 // abstract = can't create Player directly; must be Human or AI
    protected String name;              // protected = subclasses (HumanPlayer, AIPlayer) can access
    protected Symbol symbol;            // protected = subclasses can read symbol directly

    public Player(String name, Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public abstract int[] getMove(Board board); // abstract = each subclass decides how to pick moves
    public String getName() { return name; }
    public Symbol getSymbol() { return symbol; }

    @Override                            // tells compiler: I'm replacing Object's toString
    public String toString() { return name + " (" + symbol + ")"; }
}

class HumanPlayer extends Player {      // extends = inherits from Player; provides human move logic
    private int[][] moves;              // private = predetermined move sequence hidden
    private int moveIndex;              // private = internal index into move sequence

    public HumanPlayer(String name, Symbol symbol, int[][] moves) {
        super(name, symbol);            // super = call parent Player's constructor
        this.moves = moves;
        this.moveIndex = 0;
    }

    @Override                            // tells compiler: I'm providing Player's abstract getMove
    public int[] getMove(Board board) {
        if (moveIndex < moves.length) return moves[moveIndex++];
        return null;
    }
}

class AIPlayer extends Player {          // extends = inherits from Player; provides random move logic
    private Random random;              // private = RNG encapsulated; only getMove uses it

    public AIPlayer(String name, Symbol symbol) {
        super(name, symbol);
        this.random = new Random();
    }

    public AIPlayer(String name, Symbol symbol, long seed) {
        super(name, symbol);
        this.random = new Random(seed);
    }

    @Override                            // tells compiler: I'm providing Player's abstract getMove
    public int[] getMove(Board board) {
        int size = board.getSize();
        int emptyCount = 0;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (board.isEmpty(r, c)) emptyCount++;
        if (emptyCount == 0) return null;

        int target = random.nextInt(emptyCount);
        int count = 0;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (board.isEmpty(r, c)) {
                    if (count == target) return new int[]{r, c};
                    count++;
                }
        return null;
    }
}
