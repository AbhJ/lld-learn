/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/DependencyGraph.java — Tracks cell dependencies and detects circular references
import java.util.*;

public class DependencyGraph {
    private Map<String, Set<String>> precedents;          // private = cells THIS cell depends on
    private Map<String, Set<String>> dependents;          // private = cells that depend ON this cell

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

    // Naive: BFS to find all transitive dependents (recalculates ALL of them)
    public List<String> getAllDependentsTopological(String cellId) {
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        for (String dep : getDependents(cellId)) {
            if (visited.add(dep)) queue.add(dep);
        }
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            for (String dep : getDependents(current)) {
                if (visited.add(dep)) queue.add(dep);
            }
        }
        return result;
    }

    public boolean hasCircularDependency(String cellId, List<String> newDeps) {
        Set<String> visited = new HashSet<>();
        visited.add(cellId);
        Queue<String> queue = new LinkedList<>(newDeps);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(cellId)) return true;
            if (visited.add(current)) {
                Set<String> deps = precedents.getOrDefault(current, Collections.emptySet());
                queue.addAll(deps);
            }
        }
        return false;
    }
}
