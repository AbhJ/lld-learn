/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/FileSystemVisitor.java — Visitor interface for traversing file system entries
public interface FileSystemVisitor {                      // interface = decouples traversal logic from tree structure
    void visitFile(File file);
    void visitDirectory(Directory directory);
}
