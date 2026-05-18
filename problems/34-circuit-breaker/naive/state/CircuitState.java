/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/CircuitState.java — Interface for circuit breaker states (State pattern)
// DESIGN PATTERN: State
public interface CircuitState { // interface = each state (Closed/Open/HalfOpen) provides its own behavior
    String getName();
    <T> T execute(ServiceCall<T> call, CircuitBreaker breaker) throws Exception;
}
