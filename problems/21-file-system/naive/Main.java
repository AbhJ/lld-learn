/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates the in-memory file system
import java.util.EnumSet;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== In-Memory File System Demo (Naive) ===\n");

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

        System.out.println("\n--- Listing /home/user ---");
        for (FileSystemEntry entry : fs.ls("/home/user")) {
            System.out.println("  " + entry);
        }

        System.out.println("\n--- Size Calculation (recursive traversal) ---");
        long userSize = fs.calculateSize("/home/user");
        System.out.println("Size of /home/user: " + userSize + " bytes");

        System.out.println("\n--- Search (recursive traversal) ---");
        List<String> txtFiles = fs.search("/", "*.txt");
        System.out.println("Search for '*.txt':");
        for (String path : txtFiles) {
            System.out.println("  " + path);
        }

        System.out.println("\n--- Permissions ---");
        config.setPermissions(EnumSet.of(Permission.READ));
        try {
            config.setContent("new value");
        } catch (SecurityException e) {
            System.out.println("Write to read-only /etc/config.txt: DENIED");
        }

        System.out.println("\n--- Delete ---");
        fs.delete("/home/user/pictures/photo.jpg");
        System.out.println("Deleted photo.jpg. Pictures now empty: " + fs.ls("/home/user/pictures").isEmpty());

        System.out.println("\n=== Demo Complete ===");
    }
}
