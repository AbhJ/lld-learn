/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Player.java — Chess player with color assignment

enum Color {                            // enum = fixed set: only WHITE and BLACK exist
    WHITE, BLACK;
    public Color opposite() { return this == WHITE ? BLACK : WHITE; }
}

class Player {
    private String name;                // private = name encapsulated; access via getName()
    private Color color;                // private = color set once at construction

    public Player(String name, Color color) { this.name = name; this.color = color; }
    public String getName() { return name; }
    public Color getColor() { return color; }

    @Override
    public String toString() { return name + " (" + color + ")"; }
}
