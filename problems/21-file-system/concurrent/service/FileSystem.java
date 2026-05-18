/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/FileSystem.java — Thread-safe file system using ConcurrentHashMap and AtomicReference

import java.util.Set;

public class FileSystem {
    private final FileNode root;                          // final = safely shared across threads after construction

    public FileSystem() {
        this.root = new FileNode("/", true);
    }

    public boolean createFile(String dirPath, String fileName, String content) {
        FileNode dir = navigate(dirPath);
        if (dir == null || !dir.isDirectory()) return false;
        FileNode file = new FileNode(fileName, false);
        file.setContent(content);
        return dir.addChild(file);
    }

    public boolean deleteFile(String dirPath, String fileName) {
        FileNode dir = navigate(dirPath);
        if (dir == null || !dir.isDirectory()) return false;
        return dir.removeChild(fileName) != null;
    }

    public String readFile(String dirPath, String fileName) {
        FileNode dir = navigate(dirPath);
        if (dir == null || !dir.isDirectory()) return null;
        FileNode file = dir.getChild(fileName);
        if (file == null || file.isDirectory()) return null;
        return file.getContent();
    }

    public void writeFile(String dirPath, String fileName, String content) {
        FileNode dir = navigate(dirPath);
        if (dir == null || !dir.isDirectory()) return;
        FileNode file = dir.getChild(fileName);
        if (file != null && !file.isDirectory()) {
            file.setContent(content);
        }
    }

    public boolean createDirectory(String parentPath, String dirName) {
        FileNode parent = navigate(parentPath);
        if (parent == null || !parent.isDirectory()) return false;
        return parent.addChild(new FileNode(dirName, true));
    }

    public Set<String> listDirectory(String path) {
        FileNode dir = navigate(path);
        if (dir == null || !dir.isDirectory()) return null;
        return dir.listChildren();
    }

    public int getChildCount(String path) {
        FileNode dir = navigate(path);
        if (dir == null || !dir.isDirectory()) return -1;
        return dir.childCount();
    }

    private FileNode navigate(String path) {
        if (path == null || path.equals("/")) return root;
        String[] parts = path.split("/");
        FileNode current = root;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            current = current.getChild(part);
            if (current == null) return null;
        }
        return current;
    }
}
