/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Ladder.java — Ladder connecting a lower cell to a higher cell

class Ladder {
    private int start;
    private int end;

    public Ladder(int start, int end) {
        if (start >= end) throw new IllegalArgumentException("Ladder start must be below end: " + start + " -> " + end);
        this.start = start;
        this.end = end;
    }

    public int getStart() { return start; }
    public int getEnd() { return end; }

    @Override
    public String toString() { return "Ladder(" + start + " -> " + end + ")"; }
}
