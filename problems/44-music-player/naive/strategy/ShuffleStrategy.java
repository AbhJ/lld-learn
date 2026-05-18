/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/ShuffleStrategy.java — Defines how songs are shuffled in the queue
// DESIGN PATTERN: Strategy
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public interface ShuffleStrategy {            // interface = pluggable shuffle algorithms
    List<Integer> shuffle(int size);
    String getName();
}

class RandomShuffle implements ShuffleStrategy { // implements = random shuffle algorithm
    private Random random = new Random(42);

    @Override
    public List<Integer> shuffle(int size) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < size; i++) indices.add(i);
        // Reindexes entire list each time
        Collections.shuffle(indices, random);
        return indices;
    }

    @Override public String getName() { return "Random"; }
}

class WeightedShuffle implements ShuffleStrategy { // implements = biased shuffle algorithm
    private Random random = new Random(42);

    @Override
    public List<Integer> shuffle(int size) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < size; i++) indices.add(i);
        for (int i = 0; i < size - 1; i++) {
            int range = size - i;
            int j = i + (int)(Math.pow(random.nextDouble(), 2) * range);
            if (j < size) Collections.swap(indices, i, j);
        }
        return indices;
    }

    @Override public String getName() { return "Weighted"; }
}
