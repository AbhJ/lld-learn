/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/FileSystem.java — Indexed file system with O(1) path resolution via HashMap
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystem {
    private Directory root;
    // HashMap index gives O(1) path resolution instead of O(depth) tree traversal
    private Map<String, FileSystemEntry> pathIndex;       // HashMap = O(1) lookup by full path string

    public FileSystem() {
        this.root = new Directory("", "/");
        this.pathIndex = new HashMap<>();
        pathIndex.put("/", root);
    }

    public Directory getRoot() { return root; }

    public void mkdir(String path) {
        String normalized = Path.normalize(path);
        String[] parts = Path.split(normalized);
        Directory current = root;

        StringBuilder currentPath = new StringBuilder();
        for (String part : parts) {
            currentPath.append("/").append(part);
            String pathStr = currentPath.toString();

            // O(1) check if directory already exists in index
            FileSystemEntry existing = pathIndex.get(pathStr);
            if (existing != null) {
                if (existing.isDirectory()) {
                    current = (Directory) existing;
                } else {
                    throw new IllegalArgumentException("Path component is a file: " + part);
                }
            } else {
                Directory newDir = new Directory(part, pathStr);
                current.addEntry(newDir);
                pathIndex.put(pathStr, newDir); // Index the new directory
                current = newDir;
            }
        }
    }

    public File createFile(String path, String content) {
        String normalized = Path.normalize(path);
        String parentPath = Path.getParent(normalized);
        String fileName = Path.getName(normalized);

        mkdir(parentPath);
        Directory parent = (Directory) pathIndex.get(parentPath);

        FileSystemEntry existing = pathIndex.get(normalized);
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
            pathIndex.put(normalized, file); // Index the new file
        }
        // Invalidate parent size cache up the tree
        invalidateSizeChain(parentPath);
        return file;
    }

    // O(1) path resolution using the index
    public FileSystemEntry resolve(String path) {
        String normalized = Path.normalize(path);
        FileSystemEntry entry = pathIndex.get(normalized);
        if (entry == null) {
            throw new IllegalArgumentException("Entry not found: " + normalized);
        }
        return entry;
    }

    public Directory resolveDirectory(String path) {
        FileSystemEntry entry = resolve(path);
        if (!entry.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + path);
        }
        return (Directory) entry;
    }

    public boolean delete(String path) {
        String normalized = Path.normalize(path);
        if (normalized.equals("/")) {
            throw new IllegalArgumentException("Cannot delete root directory");
        }
        String parentPath = Path.getParent(normalized);
        String name = Path.getName(normalized);
        Directory parent = resolveDirectory(parentPath);

        FileSystemEntry entry = pathIndex.get(normalized);
        if (entry == null) return false;

        // Remove from index (recursively for directories)
        removeFromIndex(entry);
        parent.removeEntry(name);
        invalidateSizeChain(parentPath);
        return true;
    }

    private void removeFromIndex(FileSystemEntry entry) {
        pathIndex.remove(entry.getPath());
        if (entry.isDirectory()) {
            for (FileSystemEntry child : ((Directory) entry).getChildren()) {
                removeFromIndex(child);
            }
        }
    }

    // WHY: propagate size invalidation up to root so cached sizes stay consistent
    private void invalidateSizeChain(String path) {
        String current = path;
        while (!current.equals("/")) {
            FileSystemEntry entry = pathIndex.get(current);
            if (entry != null) entry.invalidateSize();
            current = Path.getParent(current);
        }
        root.invalidateSize();
    }

    public List<FileSystemEntry> ls(String path) {
        Directory dir = resolveDirectory(path);
        return dir.getChildren();
    }

    // Search uses index for O(n) scan of all entries (no tree traversal overhead)
    public List<String> search(String startPath, String pattern) {
        List<String> results = new ArrayList<>();
        String normalizedStart = Path.normalize(startPath);

        // WHY: scanning the flat index is cache-friendly and avoids recursive descent
        for (Map.Entry<String, FileSystemEntry> e : pathIndex.entrySet()) {
            String entryPath = e.getKey();
            // Only include entries under the start path
            if (entryPath.startsWith(normalizedStart) || normalizedStart.equals("/")) {
                String name = e.getValue().getName();
                if (!name.isEmpty() && Path.matches(name, pattern)) {
                    results.add(entryPath);
                }
            }
        }
        return results;
    }

    // Size uses cached values — O(1) if cache is warm
    public long calculateSize(String path) {
        FileSystemEntry entry = resolve(path);
        return entry.getSize();
    }
}
