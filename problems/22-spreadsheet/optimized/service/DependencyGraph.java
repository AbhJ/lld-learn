/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/DependencyGraph.java — Dependency graph with topological sort for minimal recalculation
import java.util.*;

public class DependencyGraph {
    private Map<String, Set<String>> precedents;          // HashMap = O(1) lookup of what a cell depends on
    // reverse dependency graph lets us find exactly which cells need recalc
    private Map<String, Set<String>> dependents;          // HashSet per entry = O(1) add/remove of dependents

    public DependencyGraph() {
        this.precedents = new HashMap<>();
        this.dependents = new HashMap<>();
    }

    public void setDependencies(String cellId, List<String> deps) {
        clearDependencies(cellId);
        Set<String> depSet = new HashSet<>(deps);
        precedents.put(cellId, depSet);
        for (String dep : deps) {
            dependents.computeIfAbsent(dep, k -> new HashSet<>()).add(cellId);
        }
    }

    public void clearDependencies(String cellId) {
        Set<String> oldDeps = precedents.remove(cellId);
        if (oldDeps != null) {
            for (String dep : oldDeps) {
                Set<String> depSet = dependents.get(dep);
                if (depSet != null) {
                    depSet.remove(cellId);
                    if (depSet.isEmpty()) dependents.remove(dep);
                }
            }
        }
    }

    public Set<String> getDependents(String cellId) {
        return dependents.getOrDefault(cellId, Collections.emptySet());
    }

    // WHY: Topological sort ensures each cell is recalculated only ONCE in correct order
    // Only cells actually affected by the change are included
    public List<String> getTopologicalOrder(String changedCell) {
        // Kahn's algorithm on the affected subgraph
        Map<String, Integer> inDegree = new HashMap<>();
        Set<String> affected = new HashSet<>();
        Queue<String> bfs = new LinkedList<>();

        // Find all transitively affected cells
        bfs.add(changedCell);
        while (!bfs.isEmpty()) {
            String current = bfs.poll();
            for (String dep : getDependents(current)) {
                if (affected.add(dep)) {
                    bfs.add(dep);
                }
            }
        }

        // Calculate in-degrees for affected cells only
        for (String cell : affected) {
            int degree = 0;
            Set<String> precs = precedents.getOrDefault(cell, Collections.emptySet());
            for (String prec : precs) {
                if (affected.contains(prec) || prec.equals(changedCell)) {
                    degree++;
                }
            }
            inDegree.put(cell, degree);
        }

        // Start with cells whose only changed precedent is the changedCell
        Queue<String> ready = new LinkedList<>();
        for (String cell : affected) {
            Set<String> precs = precedents.getOrDefault(cell, Collections.emptySet());
            boolean allPrecsResolved = true;
            for (String prec : precs) {
                if (affected.contains(prec)) {
                    allPrecsResolved = false;
                    break;
                }
            }
            if (allPrecsResolved) ready.add(cell);
        }

        List<String> order = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        while (!ready.isEmpty()) {
            String current = ready.poll();
            if (!processed.add(current)) continue;
            order.add(current);
            for (String dep : getDependents(current)) {
                if (affected.contains(dep) && !processed.contains(dep)) {
                    ready.add(dep);
                }
            }
        }

        // Add any remaining affected cells
        for (String cell : affected) {
            if (!processed.contains(cell)) order.add(cell);
        }

        return order;
    }

    public boolean hasCircularDependency(String cellId, List<String> newDeps) {
        Set<String> visited = new HashSet<>();
        visited.add(cellId);
        Queue<String> queue = new LinkedList<>(newDeps);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(cellId)) return true;
            if (visited.add(current)) {
                queue.addAll(precedents.getOrDefault(current, Collections.emptySet()));
            }
        }
        return false;
    }
}
