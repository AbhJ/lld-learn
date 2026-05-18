/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TaskDependency.java — Dependency graph with cycle detection for execution ordering

import java.util.*;

public class TaskDependency {
    private Map<String, Set<String>> dependsOn;    // private = internal graph edges; taskId -> dependencies
    private Map<String, Set<String>> dependedBy;   // private = reverse edges for notification on completion

    public TaskDependency() {
        this.dependsOn = new HashMap<>();
        this.dependedBy = new HashMap<>();
    }

    public boolean addDependency(String taskId, String dependsOnTaskId) {
        // Check for circular dependency
        if (wouldCreateCycle(taskId, dependsOnTaskId)) {
            System.out.printf("  [REJECTED] Adding dependency %s -> %s would create a cycle%n",
                    taskId, dependsOnTaskId);
            return false;
        }

        dependsOn.computeIfAbsent(taskId, k -> new HashSet<>()).add(dependsOnTaskId);
        dependedBy.computeIfAbsent(dependsOnTaskId, k -> new HashSet<>()).add(taskId);
        return true;
    }

    private boolean wouldCreateCycle(String taskId, String dependsOnTaskId) {
        // If dependsOnTaskId transitively depends on taskId, we have a cycle
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(dependsOnTaskId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(taskId)) return true;
            if (visited.contains(current)) continue;
            visited.add(current);

            Set<String> deps = dependsOn.get(current);
            if (deps != null) queue.addAll(deps);
        }
        return false;
    }

    public Set<String> getDependencies(String taskId) {
        return dependsOn.getOrDefault(taskId, Collections.emptySet());
    }

    public Set<String> getDependents(String taskId) {
        return dependedBy.getOrDefault(taskId, Collections.emptySet());
    }

    public void markCompleted(String taskId) {
        // Remove this task from all tasks that depend on it
        Set<String> dependents = dependedBy.get(taskId);
        if (dependents != null) {
            for (String dependent : dependents) {
                Set<String> deps = dependsOn.get(dependent);
                if (deps != null) deps.remove(taskId);
            }
        }
    }

    public boolean isReady(String taskId) {
        Set<String> deps = dependsOn.get(taskId);
        return deps == null || deps.isEmpty();
    }
}
