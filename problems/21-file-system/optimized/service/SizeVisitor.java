/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SizeVisitor.java — Size calculation visitor (kept for pattern demo)
import java.util.HashMap;
import java.util.Map;

public class SizeVisitor implements FileSystemVisitor {    // implements = fulfills visitor interface
    private Map<String, Long> directorySizes;             // HashMap = O(1) dir-size lookup by path
    private long totalSize;

    public SizeVisitor() {
        this.directorySizes = new HashMap<>();
        this.totalSize = 0;
    }

    @Override
    public void visitFile(File file) {
        totalSize += file.getSize();
    }

    @Override
    public void visitDirectory(Directory directory) {
        long dirSize = directory.getSize();
        directorySizes.put(directory.getPath(), dirSize);
    }

    public long getTotalSize() { return totalSize; }

    public long getDirectorySize(String path) {
        return directorySizes.getOrDefault(path, 0L);
    }
}
