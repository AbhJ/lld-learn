/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/File.java — File leaf node with content storage
public class File extends FileSystemEntry {               // extends = inherits cached-size fields from parent
    private String content;                               // private = content length doubles as cached file size

    public File(String name, String path) {
        super(name, path);
        this.content = "";
    }

    public File(String name, String path, String content) {
        super(name, path);
        this.content = content;
        this.cachedSize = content.length();
        this.sizeDirty = false;
    }

    public String getContent() { return content; }

    public void setContent(String content) {
        if (!hasPermission(Permission.WRITE)) {
            throw new SecurityException("No write permission on file: " + path);
        }
        this.content = content;
        this.cachedSize = content.length();
        this.sizeDirty = false;
        this.modifiedTime = System.currentTimeMillis();
    }

    public String readContent() {
        if (!hasPermission(Permission.READ)) {
            throw new SecurityException("No read permission on file: " + path);
        }
        return content;
    }

    @Override
    public boolean isDirectory() { return false; }

    @Override
    public long getSize() { return content.length(); }

    @Override
    public void accept(FileSystemVisitor visitor) {
        visitor.visitFile(this);
    }

    @Override
    public String toString() {
        return "[FILE] " + name + " (" + getSize() + " bytes)";
    }
}
