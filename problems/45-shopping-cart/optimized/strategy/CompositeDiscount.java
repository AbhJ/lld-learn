/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/CompositeDiscount.java — Composite of DiscountStrategy children, treated uniformly
// DESIGN PATTERN: Composite (a DiscountStrategy that contains other DiscountStrategy instances)
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class CompositeDiscount implements DiscountStrategy { // composite = a strategy that aggregates child strategies
    private final List<DiscountStrategy> children;

    public CompositeDiscount(DiscountStrategy... strategies) {
        this.children = new ArrayList<>(Arrays.asList(strategies));
    }

    @Override public double calculateDiscount(List<CartItem> items) {
        double total = 0.0;                                          // accumulate child discount amounts uniformly
        for (DiscountStrategy child : children) {
            total += child.calculateDiscount(items);                 // each child treated as a leaf or composite — same call
        }
        return total;
    }

    @Override public String getDescription() {
        if (children.isEmpty()) return "Composite (empty)";
        return "Composite[" + children.stream()
                .map(DiscountStrategy::getDescription)
                .collect(Collectors.joining(" + ")) + "]";
    }
}
