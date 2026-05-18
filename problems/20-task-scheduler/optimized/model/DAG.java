/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/DAG.java — Directed Acyclic Graph with topological sort for dependency resolution

import java.util.*;

/**
 * DAG-based dependency resolution with topological sort.
 * Detects cycles using DFS and provides execution order via Kahn's algorithm.
 */
public class DAG {
    private Map<String, Set<String>> adjacency; // HashMap = O(1) lookup of dependencies per task
    private Map<String, Set<String>> reverse;   // HashMap = O(1) lookup of dependents (reverse edges)
    private Map<String, Integer> inDegree;       // HashMap = O(1) check if node has zero incoming edges

    public DAG() {
        this.adjacency = new HashMap<>();
        this.reverse = new HashMap<>();
        this.inDegree = new HashMap<>();
    }

    public void addNode(String taskId) {
        adjacency.putIfAbsent(taskId, new HashSet<>());
        reverse.putIfAbsent(taskId, new HashSet<>());
        inDegree.putIfAbsent(taskId, 0);
    }

    /**
     * Add edge: taskId depends on dependsOnId.
     * Returns false if this would create a cycle.
     */
    public boolean addDependency(String taskId, String dependsOnId) {
        addNode(taskId);
        addNode(dependsOnId);

        if (wouldCreateCycle(taskId, dependsOnId)) {
            System.out.printf("  [REJECTED] %s -> %s would create a cycle%n", taskId, dependsOnId);
            return false;
        }

        adjacency.get(taskId).add(dependsOnId);
        reverse.get(dependsOnId).add(taskId);
        inDegree.merge(taskId, 1, Integer::sum);
        return true;
    }

    private boolean wouldCreateCycle(String taskId, String dependsOnId) {
        // BFS from dependsOnId checking if we can reach taskId
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(dependsOnId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(taskId)) return true;
            if (visited.contains(current)) continue;
            visited.add(current);
            Set<String> deps = adjacency.get(current);
            if (deps != null) queue.addAll(deps);
        }
        return false;
    }

    /**
     * Topological sort using Kahn's algorithm.
     * Returns execution order respecting all dependencies.
     */
    public List<String> topologicalSort() {
        Map<String, Integer> degrees = new HashMap<>(inDegree);
        Queue<String> queue = new LinkedList<>();

        for (Map.Entry<String, Integer> entry : degrees.entrySet()) {
            if (entry.getValue() == 0) queue.add(entry.getKey());
        }

        List<String> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            order.add(current);

            Set<String> dependents = reverse.get(current);
            if (dependents != null) {
                for (String dep : dependents) {
                    int newDegree = degrees.merge(dep, -1, Integer::sum);
                    if (newDegree == 0) queue.add(dep);
                }
            }
        }

        return order;
    }

    /**
     * Mark a task as completed and return newly unblocked tasks.
     */
    public List<String> markCompleted(String taskId) {
        List<String> unblocked = new ArrayList<>();
        Set<String> dependents = reverse.get(taskId);
        if (dependents != null) {
            for (String dep : dependents) {
                adjacency.get(dep).remove(taskId);
                int newDegree = inDegree.merge(dep, -1, Integer::sum);
                if (newDegree == 0) unblocked.add(dep);
            }
        }
        return unblocked;
    }

    public boolean isReady(String taskId) {
        Integer degree = inDegree.get(taskId);
        return degree != null && degree == 0;
    }

    public Set<String> getDependencies(String taskId) {
        return adjacency.getOrDefault(taskId, Collections.emptySet());
    }
}
