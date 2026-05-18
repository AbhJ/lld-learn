/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Task.java — Unit of work to be executed by the pool
public interface Task { // interface = contract any submitted task must fulfill
    String getName();
    void execute();
}
