/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/RejectionPolicy.java — Configurable behavior when task queue is full
public interface RejectionPolicy { // interface = contract; each policy provides its own reject logic
    void reject(Task task, ThreadPool pool);
}

class AbortPolicy implements RejectionPolicy { // implements = fulfills the RejectionPolicy contract
    @Override // @Override = guarantees this method matches the interface signature
    public void reject(Task task, ThreadPool pool) {
        throw new RuntimeException("[AbortPolicy] Task rejected: queue is full - " + task.getName());
    }
}

class CallerRunsPolicy implements RejectionPolicy { // implements = this class fulfills the policy contract
    @Override
    public void reject(Task task, ThreadPool pool) {
        if (!pool.isShutdown()) {
            System.out.println("[CallerRunsPolicy] Running task '" + task.getName() + "' in caller thread");
            task.execute();
        }
    }
}

class DiscardPolicy implements RejectionPolicy { // implements = another variant of the same contract
    @Override
    public void reject(Task task, ThreadPool pool) {
        System.out.println("[DiscardPolicy] Task silently discarded: " + task.getName());
    }
}
