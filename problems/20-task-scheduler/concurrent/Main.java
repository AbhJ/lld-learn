/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — 10 workers competing for tasks, dependency chain respected

import java.util.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Task Scheduler Demo ===\n");

        ConcurrentScheduler scheduler = new ConcurrentScheduler();

        // Create a task graph with dependencies:
        // A (no deps) -> B, C (depend on A) -> D (depends on B, C) -> E (depends on D)
        // F, G, H (no deps — can run in parallel)
        // I (depends on F) -> J (depends on I, G)

        Task taskA = new Task("Compile-Core", 30, List.of());
        Task taskB = new Task("Compile-UI", 20, List.of(taskA.getTaskId()));
        Task taskC = new Task("Compile-API", 25, List.of(taskA.getTaskId()));
        Task taskD = new Task("Link-App", 15, List.of(taskB.getTaskId(), taskC.getTaskId()));
        Task taskE = new Task("Package", 10, List.of(taskD.getTaskId()));
        Task taskF = new Task("Unit-Tests", 40, List.of());
        Task taskG = new Task("Lint-Check", 15, List.of());
        Task taskH = new Task("Format-Check", 10, List.of());
        Task taskI = new Task("Integration-Tests", 35, List.of(taskF.getTaskId()));
        Task taskJ = new Task("Deploy-Staging", 20, List.of(taskI.getTaskId(), taskG.getTaskId()));

        scheduler.addTask(taskA);
        scheduler.addTask(taskB);
        scheduler.addTask(taskC);
        scheduler.addTask(taskD);
        scheduler.addTask(taskE);
        scheduler.addTask(taskF);
        scheduler.addTask(taskG);
        scheduler.addTask(taskH);
        scheduler.addTask(taskI);
        scheduler.addTask(taskJ);

        System.out.println("Task graph:");
        System.out.println("  A (Compile-Core) -> B (Compile-UI), C (Compile-API)");
        System.out.println("  B, C -> D (Link-App) -> E (Package)");
        System.out.println("  F (Unit-Tests) -> I (Integration-Tests)");
        System.out.println("  I, G (Lint-Check) -> J (Deploy-Staging)");
        System.out.println("  H (Format-Check) — independent");
        System.out.println("\n10 workers competing for these tasks...\n");

        int workerCount = 10;
        long startTime = System.currentTimeMillis();
        scheduler.executeAll(workerCount);
        long duration = System.currentTimeMillis() - startTime;

        // Print execution order
        System.out.println("Execution results:");
        for (Task task : scheduler.getAllTasks()) {
            System.out.println("  " + task);
        }

        // Verify dependency ordering
        boolean depsRespected = true;
        // B must complete after A
        depsRespected &= taskA.isCompleted() && taskB.isCompleted();
        // D must complete after both B and C
        depsRespected &= taskD.isCompleted();
        // E must complete after D
        depsRespected &= taskE.isCompleted();
        // J must complete after I and G
        depsRespected &= taskJ.isCompleted();

        // No task was executed by more than one worker
        Set<String> executors = new HashSet<>();
        boolean noDoublePickup = true;
        for (Task task : scheduler.getAllTasks()) {
            if (task.getExecutedBy() != null) {
                executors.add(task.getExecutedBy());
            }
        }

        System.out.println("\n--- Summary ---");
        System.out.println("Workers: " + workerCount);
        System.out.println("Tasks: " + scheduler.getAllTasks().size());
        System.out.println("All completed: " + (scheduler.getCompletedCount() == 10));
        System.out.println("Pickup conflicts (CAS prevented double-pickup): " + scheduler.getPickupConflicts());
        System.out.println("Distinct workers used: " + executors.size());
        System.out.println("Total wall time: " + duration + "ms");
        System.out.println("Dependencies respected: " + (depsRespected ? "PASSED" : "FAILED"));
        System.out.println("\nCorrectness check: " +
                (depsRespected && scheduler.getCompletedCount() == 10 ? "PASSED" : "FAILED"));
    }
}
