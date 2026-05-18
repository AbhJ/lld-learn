/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/FileSystemEntry.java — Abstract base for file system nodes (Composite component)
import java.util.EnumSet;
import java.util.Set;

public abstract class FileSystemEntry {                   // abstract = can't instantiate directly; must subclass
    protected String name;                                // protected = accessible by subclasses (File, Directory)
    protected String path;                                // protected = subclasses need direct access to path
    protected Set<Permission> permissions;                // Set = no duplicates; each permission appears at most once
    protected long createdTime;
    protected long modifiedTime;

    public FileSystemEntry(String name, String path) {
        this.name = name;
        this.path = path;
        this.permissions = EnumSet.allOf(Permission.class);
        this.createdTime = System.currentTimeMillis();
        this.modifiedTime = this.createdTime;
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

    public abstract boolean isDirectory();                 // abstract = each subclass defines its own answer
    public abstract long getSize();                        // abstract = File/Directory compute size differently
    public abstract void accept(FileSystemVisitor visitor); // abstract = enables Visitor pattern dispatch

    @Override                                              // @Override = replaces Object.toString()
    public String toString() {
        return (isDirectory() ? "[DIR] " : "[FILE] ") + name;
    }
}
