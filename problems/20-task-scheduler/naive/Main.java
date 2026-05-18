/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the task scheduler

/*
 * VARIATIONS FREQUENTLY ASKED:
 * 1. Distributed scheduler (like Airflow) - DAG-based, worker nodes, fault tolerance
 * 2. Cron expression parser - Parse cron syntax, next fire time calculation
 * 3. Rate-limited execution - Max concurrent tasks, throttling per category
 * 4. Task retry with backoff - Exponential backoff, max retries, circuit break
 * 5. Priority aging - Low priority tasks get boosted over time to prevent starvation
 *
 * See VARIATIONS.md for full solution approaches.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Task Scheduler Demo ===\n");

        // --- Test 1: Priority-Based Execution (default strategy) + Listener fan-out ---
        System.out.println("--- Test 1: Priority-Based Execution ---");
        Scheduler scheduler = new Scheduler(2); // default = PrioritySchedulingStrategy
        scheduler.addListener(new LoggingTaskListener()); // observer fan-out
        System.out.println("Strategy: " + scheduler.getSchedulingStrategy().name());

        Task low = new SimpleTask("SendReport", TaskPriority.LOW, "Generate and email report");
        Task medium = new SimpleTask("ProcessOrder", TaskPriority.MEDIUM, "Process order #123");
        Task high = new SimpleTask("HandlePayment", TaskPriority.HIGH, "Process payment transaction");
        Task critical = new SimpleTask("SecurityAlert", TaskPriority.CRITICAL, "Handle security incident");

        scheduler.submit(low);
        scheduler.submit(medium);
        scheduler.submit(high);
        scheduler.submit(critical);
        System.out.println("\nExecuting in priority order:");
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

        // --- Test 2: Task Dependencies ---
        System.out.println("--- Test 2: Task Dependencies ---");
        Scheduler depScheduler = new Scheduler(2);

        Task validateOrder = new SimpleTask("ValidateOrder", TaskPriority.HIGH, "Validate order data");
        Task chargePayment = new SimpleTask("ChargePayment", TaskPriority.HIGH, "Charge credit card");
        Task updateInventory = new SimpleTask("UpdateInventory", TaskPriority.MEDIUM, "Reduce stock count");
        Task sendConfirmation = new SimpleTask("SendConfirmation", TaskPriority.LOW, "Send email confirmation");

        depScheduler.submit(validateOrder);
        depScheduler.submit(chargePayment);
        depScheduler.submit(updateInventory);
        depScheduler.submit(sendConfirmation);

        // chargePayment depends on validateOrder
        depScheduler.addDependency(chargePayment, validateOrder);
        // updateInventory depends on chargePayment
        depScheduler.addDependency(updateInventory, chargePayment);
        // sendConfirmation depends on updateInventory
        depScheduler.addDependency(sendConfirmation, updateInventory);

        System.out.println("\nExecuting with dependency chain:");
        System.out.println("ValidateOrder -> ChargePayment -> UpdateInventory -> SendConfirmation");
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

        cycleScheduler.addDependency(b, a); // B depends on A
        cycleScheduler.addDependency(c, b); // C depends on B
        cycleScheduler.addDependency(a, c); // A depends on C -> CYCLE!
        System.out.println();

        // --- Test 4: Recurring Tasks ---
        System.out.println("--- Test 4: Recurring Tasks ---");
        Scheduler recurScheduler = new Scheduler(1);

        RecurringTask healthCheck = new RecurringTask("HealthCheck", TaskPriority.HIGH,
                "Check system health", 4, 1000);
        recurScheduler.submit(healthCheck);

        System.out.println("Running recurring task (4 runs):");
        recurScheduler.runAll();
        System.out.println();

        // --- Test 5: Delayed Tasks ---
        System.out.println("--- Test 5: Delayed Tasks ---");
        Scheduler delayScheduler = new Scheduler(1);

        Task immediate = new SimpleTask("ImmediateTask", TaskPriority.LOW, "Execute now");
        DelayedTask delayed = new DelayedTask("DelayedTask", TaskPriority.HIGH,
                "Execute after delay", 100);

        delayScheduler.submit(immediate);
        delayScheduler.submit(delayed);

        System.out.println("Before delay (delayed task should not run):");
        delayScheduler.runAll();

        System.out.println("\nWaiting 150ms for delayed task...");
        Thread.sleep(150);
        delayScheduler.runAll();
        System.out.println();

        // --- Test 6: Task Cancellation ---
        System.out.println("--- Test 6: Task Cancellation ---");
        Scheduler cancelScheduler = new Scheduler(1);

        Task t1 = new SimpleTask("Task1", TaskPriority.LOW, "Will be cancelled");
        Task t2 = new SimpleTask("Task2", TaskPriority.MEDIUM, "Will execute");

        cancelScheduler.submit(t1);
        cancelScheduler.submit(t2);
        cancelScheduler.cancel(t1);

        System.out.println("After cancelling Task1:");
        cancelScheduler.runAll();
        System.out.println("Task1 state: " + t1.getState());
        System.out.println("Task2 state: " + t2.getState());
        System.out.println();

        // --- Test 7: Worker Statistics ---
        System.out.println("--- Test 7: Worker Statistics ---");
        System.out.println("Scheduler 1 workers:");
        for (Worker w : scheduler.getWorkers()) {
            System.out.println("  " + w);
        }
        System.out.println("Total tasks processed: " + scheduler.getResults().size());
        System.out.println();

        // --- Test 8: Mixed Priority with Dependencies ---
        System.out.println("--- Test 8: Mixed Priority with Dependencies ---");
        Scheduler mixedScheduler = new Scheduler(2);

        Task setup = new SimpleTask("Setup", TaskPriority.CRITICAL, "Initialize environment");
        Task parallel1 = new SimpleTask("BuildFrontend", TaskPriority.HIGH, "Build frontend assets");
        Task parallel2 = new SimpleTask("BuildBackend", TaskPriority.HIGH, "Build backend services");
        Task deploy = new SimpleTask("Deploy", TaskPriority.CRITICAL, "Deploy to production");

        mixedScheduler.submit(setup);
        mixedScheduler.submit(parallel1);
        mixedScheduler.submit(parallel2);
        mixedScheduler.submit(deploy);

        // Both builds depend on setup
        mixedScheduler.addDependency(parallel1, setup);
        mixedScheduler.addDependency(parallel2, setup);
        // Deploy depends on both builds
        mixedScheduler.addDependency(deploy, parallel1);
        mixedScheduler.addDependency(deploy, parallel2);

        System.out.println("\nExecution order (Setup -> [Frontend, Backend] -> Deploy):");
        mixedScheduler.runAll();
        System.out.println();

        System.out.println("=== Task Scheduler Demo Complete ===");
    }
}
