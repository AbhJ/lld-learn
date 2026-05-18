/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/Dice.java — Dice interface and implementations (normal, crooked, fixed)

import java.util.Random;

interface Dice {                        // interface = contract; any dice must provide these methods
    int roll();
    String getName();
}

class NormalDice implements Dice {       // implements = fulfills the Dice interface contract
    private Random random;              // private = RNG hidden; only roll() uses it
    private int faces;                  // private = number of faces set once at construction

    public NormalDice() { this(6); }

    public NormalDice(int faces) {
        this.faces = faces;
        this.random = new Random();
    }

    public NormalDice(int faces, long seed) {
        this.faces = faces;
        this.random = new Random(seed);
    }

    @Override                            // tells compiler: I'm fulfilling Dice.roll()
    public int roll() { return random.nextInt(faces) + 1; }

    @Override                            // tells compiler: I'm fulfilling Dice.getName()
    public String getName() { return "Normal Dice (1-" + faces + ")"; }
}

class CrookedDice implements Dice {     // implements = fulfills Dice; always rolls even numbers
    private Random random;              // private = RNG hidden from outside

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

class FixedDice implements Dice {        // implements = fulfills Dice; returns predetermined values
    private int[] values;               // private = sequence of rolls hidden from outside
    private int index;                  // private = tracks current position in sequence

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
