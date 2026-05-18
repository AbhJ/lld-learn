/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates the optimized file system with indexed paths and cached sizes
import java.util.EnumSet;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== In-Memory File System Demo (Optimized) ===\n");
        System.out.println("Optimizations: HashMap path index for O(1) resolve, cached directory sizes\n");

        FileSystem fs = new FileSystem();

        System.out.println("--- Creating Directory Structure ---");
        fs.mkdir("/home");
        fs.mkdir("/home/user");
        fs.mkdir("/home/user/documents");
        fs.mkdir("/home/user/pictures");
        fs.mkdir("/etc");
        System.out.println("Created: /home, /home/user, /home/user/documents, /home/user/pictures, /etc");

        System.out.println("\n--- Creating Files ---");
        File readme = fs.createFile("/home/user/documents/readme.txt", "This is a readme file with some content.");
        File notes = fs.createFile("/home/user/documents/notes.txt", "Important notes for the day.");
        StringBuilder photoData = new StringBuilder();
        for (int i = 0; i < 1024; i++) photoData.append("X");
        File photo = fs.createFile("/home/user/pictures/photo.jpg", photoData.toString());
        File config = fs.createFile("/etc/config.txt", "key=value;debug");
        System.out.println("Created: readme.txt, notes.txt, photo.jpg, config.txt");

        System.out.println("\n--- O(1) Path Resolution (indexed) ---");
        FileSystemEntry resolved = fs.resolve("/home/user/documents/readme.txt");
        System.out.println("resolve('/home/user/documents/readme.txt') = " + resolved);

        System.out.println("\n--- Cached Size Calculation ---");
        long userSize = fs.calculateSize("/home/user");
        System.out.println("Size of /home/user: " + userSize + " bytes (cached after first calc)");
        // Second call is O(1) since cache is warm
        long userSize2 = fs.calculateSize("/home/user");
        System.out.println("Size again (O(1) cache hit): " + userSize2 + " bytes");

        System.out.println("\n--- Indexed Search ---");
        List<String> txtFiles = fs.search("/", "*.txt");
        System.out.println("Search for '*.txt' (flat index scan):");
        for (String path : txtFiles) {
            System.out.println("  " + path);
        }

        System.out.println("\n--- Delete (invalidates size cache up the tree) ---");
        fs.delete("/home/user/pictures/photo.jpg");
        long newSize = fs.calculateSize("/home/user");
        System.out.println("After deleting photo.jpg, /home/user size: " + newSize + " bytes");

        System.out.println("\n=== Demo Complete ===");
    }
}
