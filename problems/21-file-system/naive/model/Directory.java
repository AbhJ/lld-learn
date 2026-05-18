/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Directory.java — Directory composite node containing children
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Directory extends FileSystemEntry implements Iterable<FileSystemEntry> { // implements = fulfills Iterable contract
    private List<FileSystemEntry> children;               // private = only Directory manages its children

    public Directory(String name, String path) {
        super(name, path);
        this.children = new ArrayList<>();
    }

    public void addEntry(FileSystemEntry entry) {
        children.add(entry);
        this.modifiedTime = System.currentTimeMillis();
    }

    public boolean removeEntry(String name) {
        return children.removeIf(e -> e.getName().equals(name));
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
        long total = 0;
        for (FileSystemEntry entry : children) {
            total += entry.getSize();
        }
        return total;
    }

    @Override
    public void accept(FileSystemVisitor visitor) {
        visitor.visitDirectory(this);
        for (FileSystemEntry child : children) {
            child.accept(visitor);
        }
    }

    @Override                                              // @Override = required by Iterable interface
    public Iterator<FileSystemEntry> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        return "[DIR] " + name;
    }
}
