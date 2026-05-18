/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SnakeAndLadderGame.java — Concrete Template Method impl: snake/ladder turn rules

class SnakeAndLadderGame extends AbstractBoardGame {
    private final Board board;
    // Captured between applyMove and onAfterMove so the verbose hook can
    // report the pre-effect intermediate position vs. the final landing.
    private int lastIntermediatePosition;
    private boolean lastOvershot;

    public SnakeAndLadderGame(Board board, Dice dice, boolean verbose) {
        super(dice, verbose);
        this.board = board;
    }

    public SnakeAndLadderGame(Board board, Dice dice) { this(board, dice, true); }

    @Override
    protected int applyMove(Player current, int roll) {
        int oldPosition = current.getPosition();
        int newPosition = oldPosition + roll;
        if (newPosition > board.getSize()) {
            // Overshoot: stay put.
            lastOvershot = true;
            lastIntermediatePosition = oldPosition;
            return oldPosition;
        }
        lastOvershot = false;
        lastIntermediatePosition = newPosition;
        int finalPosition = board.getFinalPosition(newPosition);
        current.moveTo(finalPosition);
        return finalPosition;
    }

    @Override
    protected boolean checkVictory(Player current) {
        return board.isWinningPosition(current.getPosition());
    }

    @Override
    protected void onAfterMove(Player current, int roll, int oldPosition, int landedPosition) {
        if (!verbose) return;
        if (lastOvershot) {
            System.out.println("  Turn " + turnCount + ": " + current.getName() +
                    " rolls " + roll + " - stays at " + oldPosition + " (overshoot)");
            return;
        }
        StringBuilder log = new StringBuilder();
        log.append("  Turn ").append(turnCount).append(": ").append(current.getName())
                .append(" rolls ").append(roll).append(", moves ").append(oldPosition)
                .append("->").append(lastIntermediatePosition);
        if (landedPosition != lastIntermediatePosition) {
            if (landedPosition < lastIntermediatePosition) {
                log.append(", Snake! ").append(lastIntermediatePosition).append("->").append(landedPosition);
            } else {
                log.append(", Ladder! ").append(lastIntermediatePosition).append("->").append(landedPosition);
            }
        }
        System.out.println(log);
    }

    @Override
    protected void onVictory(Player winner) {
        if (verbose) {
            System.out.println("  *** " + winner.getName() + " WINS in " + turnCount + " turns! ***");
        }
    }

    public Board getBoard() { return board; }
}
