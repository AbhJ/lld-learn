/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Timer.java — Countdown timing for signal phases

class Timer {
    private int greenDuration;        // private = timing config hidden from outside
    private int yellowDuration;       // private = timing config hidden from outside
    private int currentTick;          // private = internal counter; managed via tick()/reset()

    public Timer(int greenDuration, int yellowDuration) {
        this.greenDuration = greenDuration;
        this.yellowDuration = yellowDuration;
        this.currentTick = 0;
    }

    public void tick() { currentTick++; }
    public void reset() { currentTick = 0; }

    public int getCycleDuration() { return greenDuration + yellowDuration; }
    public boolean isGreenExpired() { return currentTick >= greenDuration; }
    public boolean isYellowExpired() { return currentTick >= greenDuration + yellowDuration; }

    public int getCurrentTick() { return currentTick; }
    public int getGreenDuration() { return greenDuration; }
    public int getYellowDuration() { return yellowDuration; }
}
