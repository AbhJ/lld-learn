/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/ShuffleStrategy.java — Fisher-Yates lazy shuffle for O(n) one-pass
// DESIGN PATTERN: Strategy
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public interface ShuffleStrategy {            // interface = pluggable shuffle algorithms
    List<Integer> shuffle(int size);
    String getName();
}

class FisherYatesShuffle implements ShuffleStrategy { // Fisher-Yates = O(n) unbiased shuffle
    private Random random = new Random(42);

    @Override
    public List<Integer> shuffle(int size) {
        // WHY: Fisher-Yates is O(n) with exactly n-1 swaps, unbiased
        // vs Collections.shuffle which may reallocate internal arrays
        List<Integer> indices = new ArrayList<>(size);
        for (int i = 0; i < size; i++) indices.add(i);
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = indices.get(i);
            indices.set(i, indices.get(j));
            indices.set(j, temp);
        }
        return indices;
    }

    @Override public String getName() { return "Fisher-Yates"; }
}

class WeightedShuffle implements ShuffleStrategy {
    private Random random = new Random(42);

    @Override
    public List<Integer> shuffle(int size) {
        List<Integer> indices = new ArrayList<>(size);
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
