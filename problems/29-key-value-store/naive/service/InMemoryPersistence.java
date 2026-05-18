/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/InMemoryPersistence.java — Strategy impl: keeps the command log in a heap list only

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class InMemoryPersistence implements PersistenceStrategy {
    private final List<Command> log = new ArrayList<>();

    @Override
    public void append(Command command) {
        log.add(command);
    }

    @Override
    public List<Command> getLog() {
        return Collections.unmodifiableList(log);
    }

    @Override
    public void replaceLog(List<Command> compacted) {
        log.clear();
        log.addAll(compacted);
    }
}
