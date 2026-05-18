/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Position.java — Board coordinate (row, col) with chess notation

class Position {
    private int row;                    // private = coordinates encapsulated; access via getRow()
    private int col;                    // private = coordinates encapsulated; access via getCol()

    public Position(int row, int col) { this.row = row; this.col = col; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isValid() { return row >= 0 && row < 8 && col >= 0 && col < 8; }

    @Override                            // tells compiler: I'm replacing Object's equals
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return row == p.row && col == p.col;
    }

    @Override                            // tells compiler: I'm replacing Object's hashCode
    public int hashCode() { return row * 8 + col; }

    @Override                            // tells compiler: I'm replacing Object's toString
    public String toString() { return "" + (char)('a' + col) + (8 - row); }
}
