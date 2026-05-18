/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/FileSystem.java — Manages the file system tree with path-based operations
import java.util.List;

public class FileSystem {
    private Directory root;                               // private = only FileSystem manages the root

    public FileSystem() {
        this.root = new Directory("", "/");
    }

    public Directory getRoot() { return root; }

    public void mkdir(String path) {
        String normalized = Path.normalize(path);
        String[] parts = Path.split(normalized);
        Directory current = root;

        StringBuilder currentPath = new StringBuilder();
        for (String part : parts) {
            currentPath.append("/").append(part);
            FileSystemEntry entry = current.getEntry(part);
            if (entry == null) {
                Directory newDir = new Directory(part, currentPath.toString());
                current.addEntry(newDir);
                current = newDir;
            } else if (entry.isDirectory()) {
                current = (Directory) entry;
            } else {
                throw new IllegalArgumentException("Path component is a file: " + part);
            }
        }
    }

    public File createFile(String path, String content) {
        String normalized = Path.normalize(path);
        String parentPath = Path.getParent(normalized);
        String fileName = Path.getName(normalized);

        mkdir(parentPath);
        Directory parent = resolveDirectory(parentPath);

        FileSystemEntry existing = parent.getEntry(fileName);
        if (existing != null && existing.isDirectory()) {
            throw new IllegalArgumentException("A directory exists with that name: " + fileName);
        }

        File file;
        if (existing != null) {
            file = (File) existing;
            file.setContent(content);
        } else {
            file = new File(fileName, normalized, content);
            parent.addEntry(file);
        }
        return file;
    }

    public Directory resolveDirectory(String path) {
        String normalized = Path.normalize(path);
        if (normalized.equals("/")) return root;

        String[] parts = Path.split(normalized);
        Directory current = root;
        for (String part : parts) {
            FileSystemEntry entry = current.getEntry(part);
            if (entry == null || !entry.isDirectory()) {
                throw new IllegalArgumentException("Directory not found: " + normalized);
            }
            current = (Directory) entry;
        }
        return current;
    }

    public FileSystemEntry resolve(String path) {
        String normalized = Path.normalize(path);
        if (normalized.equals("/")) return root;

        String parentPath = Path.getParent(normalized);
        String name = Path.getName(normalized);
        Directory parent = resolveDirectory(parentPath);
        FileSystemEntry entry = parent.getEntry(name);
        if (entry == null) {
            throw new IllegalArgumentException("Entry not found: " + normalized);
        }
        return entry;
    }

    public boolean delete(String path) {
        String normalized = Path.normalize(path);
        if (normalized.equals("/")) {
            throw new IllegalArgumentException("Cannot delete root directory");
        }
        String parentPath = Path.getParent(normalized);
        String name = Path.getName(normalized);
        Directory parent = resolveDirectory(parentPath);
        return parent.removeEntry(name);
    }

    public List<FileSystemEntry> ls(String path) {
        Directory dir = resolveDirectory(path);
        return dir.getChildren();
    }

    // Naive: recursive search through all nodes
    public List<String> search(String startPath, String pattern) {
        FileSystemEntry start = resolve(startPath);
        SearchVisitor visitor = new SearchVisitor(pattern);
        start.accept(visitor);
        return visitor.getResults();
    }

    // Naive: recalculates size by traversing entire subtree
    public long calculateSize(String path) {
        FileSystemEntry entry = resolve(path);
        SizeVisitor visitor = new SizeVisitor();
        entry.accept(visitor);
        if (entry.isDirectory()) {
            return visitor.getDirectorySize(entry.getPath());
        }
        return visitor.getTotalSize();
    }
}
