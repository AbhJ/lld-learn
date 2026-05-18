/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating DAG-based scheduling with topological sort

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Task Scheduler (Optimized) ===\n");

        // --- Test 1: Priority-Based Execution (default strategy) + Listener fan-out ---
        System.out.println("--- Test 1: Priority-Based Execution ---");
        Scheduler scheduler = new Scheduler(2); // default = PrioritySchedulingStrategy
        scheduler.addListener(new LoggingTaskListener()); // observer fan-out
        System.out.println("Strategy: " + scheduler.getSchedulingStrategy().name());

        Task low = new SimpleTask("SendReport", TaskPriority.LOW, "Generate report");
        Task medium = new SimpleTask("ProcessOrder", TaskPriority.MEDIUM, "Process order #123");
        Task high = new SimpleTask("HandlePayment", TaskPriority.HIGH, "Process payment");
        Task critical = new SimpleTask("SecurityAlert", TaskPriority.CRITICAL, "Handle security incident");

        scheduler.submit(low);
        scheduler.submit(medium);
        scheduler.submit(high);
        scheduler.submit(critical);
        System.out.println("\nExecuting (priority strategy order):");
        scheduler.runAll();
        System.out.println();

        // --- Test 1b: FIFO Strategy (oldest-first, ignores priority) ---
        System.out.println("--- Test 1b: FIFO Scheduling Strategy ---");
        Scheduler fifoScheduler = new Scheduler(2, new FifoSchedulingStrategy());
        fifoScheduler.addListener(new LoggingTaskListener());
        System.out.println("Strategy: " + fifoScheduler.getSchedulingStrategy().name());

        fifoScheduler.submit(new SimpleTask("FirstSubmitted", TaskPriority.LOW, "submitted earliest"));
        fifoScheduler.submit(new SimpleTask("SecondSubmitted", TaskPriority.CRITICAL, "high priority but later"));
        fifoScheduler.submit(new SimpleTask("ThirdSubmitted", TaskPriority.HIGH, "submitted last"));
        System.out.println("\nExecuting in FIFO order (priority ignored):");
        fifoScheduler.runAll();
        System.out.println();

        // --- Test 2: DAG Dependencies with Topological Sort ---
        System.out.println("--- Test 2: DAG Dependencies ---");
        Scheduler depScheduler = new Scheduler(2);

        Task validate = new SimpleTask("ValidateOrder", TaskPriority.HIGH, "Validate order data");
        Task charge = new SimpleTask("ChargePayment", TaskPriority.HIGH, "Charge credit card");
        Task updateInv = new SimpleTask("UpdateInventory", TaskPriority.MEDIUM, "Reduce stock");
        Task sendConf = new SimpleTask("SendConfirmation", TaskPriority.LOW, "Send email");

        depScheduler.submit(validate);
        depScheduler.submit(charge);
        depScheduler.submit(updateInv);
        depScheduler.submit(sendConf);

        depScheduler.addDependency(charge, validate);
        depScheduler.addDependency(updateInv, charge);
        depScheduler.addDependency(sendConf, updateInv);

        System.out.println("\nTopological order: " + depScheduler.getExecutionOrder());
        System.out.println("Executing with DAG resolution:");
        depScheduler.runAll();
        System.out.println();

        // --- Test 3: Circular Dependency Detection ---
        System.out.println("--- Test 3: Circular Dependency Detection ---");
        Scheduler cycleScheduler = new Scheduler(1);
        Task a = new SimpleTask("TaskA", TaskPriority.MEDIUM, "Do A");
        Task b = new SimpleTask("TaskB", TaskPriority.MEDIUM, "Do B");
        Task c = new SimpleTask("TaskC", TaskPriority.MEDIUM, "Do C");
        cycleScheduler.submit(a);
        cycleScheduler.submit(b);
        cycleScheduler.submit(c);
        cycleScheduler.addDependency(b, a);
        cycleScheduler.addDependency(c, b);
        cycleScheduler.addDependency(a, c); // Cycle detected!
        System.out.println();

        // --- Test 4: Delayed Tasks ---
        System.out.println("--- Test 4: Delayed Tasks ---");
        Scheduler delayScheduler = new Scheduler(1);
        Task immediate = new SimpleTask("ImmediateTask", TaskPriority.LOW, "Execute now");
        Task delayed = new SimpleTask("DelayedTask", TaskPriority.HIGH, "After delay", 100);
        delayScheduler.submit(immediate);
        delayScheduler.submit(delayed);
        System.out.println("Before delay:");
        delayScheduler.runAll();
        System.out.println("\nWaiting 150ms...");
        Thread.sleep(150);
        delayScheduler.runAll();
        System.out.println();

        // --- Test 5: Recurring Tasks ---
        System.out.println("--- Test 5: Recurring Tasks ---");
        Scheduler recurScheduler = new Scheduler(1);
        RecurringTask health = new RecurringTask("HealthCheck", TaskPriority.HIGH, "Check health", 3, 0);
        recurScheduler.submit(health);
        recurScheduler.runAll();
        System.out.println();

        // --- Test 6: Parallel Dependencies (fan-in) ---
        System.out.println("--- Test 6: Parallel Dependencies (fan-in) ---");
        Scheduler fanInScheduler = new Scheduler(2);
        Task setup = new SimpleTask("Setup", TaskPriority.CRITICAL, "Initialize");
        Task buildFE = new SimpleTask("BuildFrontend", TaskPriority.HIGH, "Build frontend");
        Task buildBE = new SimpleTask("BuildBackend", TaskPriority.HIGH, "Build backend");
        Task deploy = new SimpleTask("Deploy", TaskPriority.CRITICAL, "Deploy to prod");

        fanInScheduler.submit(setup);
        fanInScheduler.submit(buildFE);
        fanInScheduler.submit(buildBE);
        fanInScheduler.submit(deploy);

        fanInScheduler.addDependency(buildFE, setup);
        fanInScheduler.addDependency(buildBE, setup);
        fanInScheduler.addDependency(deploy, buildFE);
        fanInScheduler.addDependency(deploy, buildBE);

        System.out.println("\nExecution (Setup -> [FE, BE] -> Deploy):");
        fanInScheduler.runAll();
        System.out.println();

        System.out.println("=== Task Scheduler Demo Complete ===");
    }
}
