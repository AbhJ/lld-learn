/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Directory.java — Directory node with cached size computation
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Directory extends FileSystemEntry implements Iterable<FileSystemEntry> {
    private List<FileSystemEntry> children;               // ArrayList = O(1) random access; good for iteration

    public Directory(String name, String path) {
        super(name, path);
        this.children = new ArrayList<>();
    }

    public void addEntry(FileSystemEntry entry) {
        children.add(entry);
        this.modifiedTime = System.currentTimeMillis();
        invalidateSize(); // WHY: new child means cached size is stale
    }

    public boolean removeEntry(String name) {
        boolean removed = children.removeIf(e -> e.getName().equals(name));
        if (removed) invalidateSize();
        return removed;
    }

    public FileSystemEntry getEntry(String name) {
        for (FileSystemEntry entry : children) {
            if (entry.getName().equals(name)) return entry;
        }
        return null;
    }

    public List<FileSystemEntry> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean isEmpty() { return children.isEmpty(); }

    @Override
    public boolean isDirectory() { return true; }

    @Override
    public long getSize() {
        // WHY: only recalculate when dirty, otherwise return cached value
        if (sizeDirty) {
            cachedSize = 0;
            for (FileSystemEntry entry : children) {
                cachedSize += entry.getSize();
            }
            sizeDirty = false;
        }
        return cachedSize;
    }

    @Override
    public void invalidateSize() {
        this.sizeDirty = true;
        // Propagation happens through the indexed FileSystem
    }

    @Override
    public void accept(FileSystemVisitor visitor) {
        visitor.visitDirectory(this);
        for (FileSystemEntry child : children) {
            child.accept(visitor);
        }
    }

    @Override
    public Iterator<FileSystemEntry> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        return "[DIR] " + name;
    }
}
