/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Clipboard.java — Stores copied text for paste operations
public class Clipboard {
    private String content = "";       // private = encapsulates clipboard data from outside
    public void set(String text) { this.content = text; }
    public String get() { return content; }
    public boolean isEmpty() { return content == null || content.isEmpty(); }
}
