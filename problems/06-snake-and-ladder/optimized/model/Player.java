/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Player.java — Player with position and name

class Player {
    private String name;
    private int position;
    private boolean hasWon;

    public Player(String name) {
        this.name = name;
        this.position = 0;
        this.hasWon = false;
    }

    public void moveTo(int position) { this.position = position; }
    public void setWon(boolean won) { this.hasWon = won; }
    public String getName() { return name; }
    public int getPosition() { return position; }
    public boolean hasWon() { return hasWon; }

    @Override
    public String toString() { return name + " (pos: " + position + ")"; }
}
