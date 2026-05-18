/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/ClosedState.java — Normal state, tracks consecutive failures with simple counter
public class ClosedState implements CircuitState { // implements = fulfills the CircuitState contract
    private int consecutiveFailures = 0;           // private = internal counter; reset on success

    @Override
    public String getName() { return "CLOSED"; }

    @Override
    public <T> T execute(ServiceCall<T> call, CircuitBreaker breaker) throws Exception {
        try {
            T result = call.call();
            consecutiveFailures = 0;
            breaker.getMetrics().recordSuccess();
            return result;
        } catch (Exception e) {
            consecutiveFailures++;
            breaker.getMetrics().recordFailure();
            if (consecutiveFailures >= breaker.getConfig().getFailureThreshold()) {
                breaker.transitionTo(new OpenState());
            }
            throw e;
        }
    }
}
