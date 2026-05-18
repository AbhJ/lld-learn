/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/FileSystemEntry.java — Abstract base for file system nodes with cached metadata
import java.util.EnumSet;
import java.util.Set;

public abstract class FileSystemEntry {
    protected String name;
    protected String path;
    protected Set<Permission> permissions;                // EnumSet = bit-field storage for enums; O(1) contains
    protected long createdTime;
    protected long modifiedTime;
    // Optimization: cached size avoids re-traversal
    protected long cachedSize;                            // cache stores last-computed value; avoids O(n) walk
    protected boolean sizeDirty;                          // dirty flag = cheap invalidation instead of recompute

    public FileSystemEntry(String name, String path) {
        this.name = name;
        this.path = path;
        this.permissions = EnumSet.allOf(Permission.class);
        this.createdTime = System.currentTimeMillis();
        this.modifiedTime = this.createdTime;
        this.cachedSize = 0;
        this.sizeDirty = true;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public Set<Permission> getPermissions() { return permissions; }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = EnumSet.copyOf(permissions);
    }

    public boolean hasPermission(Permission p) {
        return permissions.contains(p);
    }

    public abstract boolean isDirectory();
    public abstract long getSize();
    public abstract void accept(FileSystemVisitor visitor);

    // Mark size as needing recalculation
    public void invalidateSize() { this.sizeDirty = true; }

    @Override
    public String toString() {
        return (isDirectory() ? "[DIR] " : "[FILE] ") + name;
    }
}
