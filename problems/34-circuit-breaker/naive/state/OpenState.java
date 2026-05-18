/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/OpenState.java — Fail-fast state, rejects all calls until timeout elapses
public class OpenState implements CircuitState { // implements = State pattern; rejects all calls
    private final long enteredAt = System.currentTimeMillis(); // final = captured once when state is created

    @Override
    public String getName() { return "OPEN"; }

    @Override
    public <T> T execute(ServiceCall<T> call, CircuitBreaker breaker) throws Exception {
        long elapsed = System.currentTimeMillis() - enteredAt;
        if (elapsed >= breaker.getConfig().getOpenTimeoutMs()) {
            breaker.transitionTo(new HalfOpenState());
            return breaker.execute(call);
        }
        breaker.getMetrics().recordRejected();
        throw new RuntimeException("Circuit OPEN - failing fast");
    }
}
