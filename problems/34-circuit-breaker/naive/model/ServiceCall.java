/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/ServiceCall.java — Represents a callable remote service operation
public interface ServiceCall<T> { // interface + generic <T> = contract for any callable returning type T
    T call() throws Exception;
}
