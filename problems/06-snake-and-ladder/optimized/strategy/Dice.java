/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/Dice.java — Dice interface and implementations (normal, crooked, fixed)

import java.util.Random;

interface Dice {                        // interface = strategy pattern; swap dice without changing Game
    int roll();
    String getName();
}

class NormalDice implements Dice {
    private Random random;              // Random = uniform distribution with O(1) nextInt
    private int faces;

    public NormalDice() { this(6); }

    public NormalDice(int faces) {
        this.faces = faces;
        this.random = new Random();
    }

    public NormalDice(int faces, long seed) {
        this.faces = faces;
        this.random = new Random(seed);
    }

    @Override
    public int roll() { return random.nextInt(faces) + 1; }

    @Override
    public String getName() { return "Normal Dice (1-" + faces + ")"; }
}

class CrookedDice implements Dice {
    private Random random;

    public CrookedDice() { this.random = new Random(); }
    public CrookedDice(long seed) { this.random = new Random(seed); }

    @Override
    public int roll() {
        int[] evenValues = {2, 4, 6};
        return evenValues[random.nextInt(3)];
    }

    @Override
    public String getName() { return "Crooked Dice (even only)"; }
}

class FixedDice implements Dice {
    private int[] values;
    private int index;

    public FixedDice(int... values) {
        this.values = values;
        this.index = 0;
    }

    @Override
    public int roll() {
        int value = values[index % values.length];
        index++;
        return value;
    }

    @Override
    public String getName() { return "Fixed Dice"; }
}
