/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/HalfOpenState.java — Trial state allowing limited calls to test recovery
public class HalfOpenState implements CircuitState { // implements = another state variant in the State pattern
    private int trialSuccesses = 0;                   // private = tracks probe outcomes internally
    private int trialAttempts = 0;

    @Override
    public String getName() { return "HALF_OPEN"; }

    @Override
    public <T> T execute(ServiceCall<T> call, CircuitBreaker breaker) throws Exception {
        int maxTrials = breaker.getConfig().getHalfOpenTrialCount();
        trialAttempts++;
        try {
            T result = call.call();
            trialSuccesses++;
            breaker.getMetrics().recordSuccess();
            if (trialSuccesses >= maxTrials) {
                breaker.transitionTo(new ClosedState());
            }
            return result;
        } catch (Exception e) {
            breaker.getMetrics().recordFailure();
            breaker.transitionTo(new OpenState());
            throw e;
        }
    }
}
