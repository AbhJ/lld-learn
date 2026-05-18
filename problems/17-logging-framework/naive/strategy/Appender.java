/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/Appender.java — Interchangeable log output destinations (console, file)

import java.util.ArrayList;
import java.util.List;

public interface Appender {                      // interface = contract for any log output destination
    void append(String formattedMessage);
    String getName();
    void close();
}

class ConsoleAppender implements Appender {      // implements = fulfills Appender contract for console output
    @Override
    public void append(String formattedMessage) {
        System.out.println(formattedMessage);
    }

    @Override
    public String getName() { return "Console"; }

    @Override
    public void close() { }
}

class FileAppender implements Appender {          // implements = fulfills Appender contract for file output
    private String fileName;
    private List<String> buffer; // Simulates file writing

    public FileAppender(String fileName) {
        this.fileName = fileName;
        this.buffer = new ArrayList<>();
    }

    @Override
    public void append(String formattedMessage) {
        buffer.add(formattedMessage);
        // In production, this would write to actual file
    }

    @Override
    public String getName() { return "File(" + fileName + ")"; }

    @Override
    public void close() {
        // In production, flush and close file handle
        buffer.clear();
    }

    public List<String> getBufferedContent() {
        return new ArrayList<>(buffer);
    }

    public int getLineCount() { return buffer.size(); }
}
