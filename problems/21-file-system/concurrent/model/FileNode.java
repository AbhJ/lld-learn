/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/FileNode.java — Represents a file or directory with thread-safe content and children

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class FileNode {
    private final String name;                            // final = safe publication; immutable after construction
    private final boolean isDirectory;                    // final = never changes once created
    private final AtomicReference<String> content;        // AtomicReference = lock-free thread-safe reads/writes
    private final ConcurrentHashMap<String, FileNode> children; // ConcurrentHashMap = thread-safe map; no external lock needed

    public FileNode(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.content = isDirectory ? null : new AtomicReference<>("");
        this.children = isDirectory ? new ConcurrentHashMap<>() : null;
    }

    public String getName() { return name; }
    public boolean isDirectory() { return isDirectory; }

    public String getContent() {
        if (!isDirectory) return content.get();
        throw new UnsupportedOperationException("Directories have no content");
    }

    public void setContent(String newContent) {
        if (!isDirectory) content.set(newContent);
        else throw new UnsupportedOperationException("Directories have no content");
    }

    public boolean addChild(FileNode child) {
        if (!isDirectory) throw new UnsupportedOperationException("Files cannot have children");
        return children.putIfAbsent(child.getName(), child) == null;
    }

    public FileNode removeChild(String childName) {
        if (!isDirectory) throw new UnsupportedOperationException("Files cannot have children");
        return children.remove(childName);
    }

    public FileNode getChild(String childName) {
        if (!isDirectory) return null;
        return children.get(childName);
    }

    public Set<String> listChildren() {
        if (!isDirectory) return Collections.emptySet();
        return Collections.unmodifiableSet(children.keySet());
    }

    public int childCount() {
        if (!isDirectory) return 0;
        return children.size();
    }
}
