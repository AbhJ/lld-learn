/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Path.java — Utility for path normalization and manipulation
import java.util.ArrayList;
import java.util.List;

public class Path {                                       // utility class; all static methods (stateless)
    public static String normalize(String path) {
        if (path == null || path.isEmpty()) return "/";
        if (!path.startsWith("/")) path = "/" + path;

        String[] parts = path.split("/");
        List<String> stack = new ArrayList<>();
        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (!stack.isEmpty()) stack.remove(stack.size() - 1);
            } else {
                stack.add(part);
            }
        }
        if (stack.isEmpty()) return "/";
        StringBuilder sb = new StringBuilder();
        for (String s : stack) {
            sb.append("/").append(s);
        }
        return sb.toString();
    }

    public static String[] split(String path) {
        String normalized = normalize(path);
        if (normalized.equals("/")) return new String[0];
        return normalized.substring(1).split("/");
    }

    public static String getParent(String path) {
        String normalized = normalize(path);
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash <= 0) return "/";
        return normalized.substring(0, lastSlash);
    }

    public static String getName(String path) {
        String normalized = normalize(path);
        int lastSlash = normalized.lastIndexOf('/');
        return normalized.substring(lastSlash + 1);
    }

    public static String join(String parent, String child) {
        if (parent.equals("/")) return "/" + child;
        return parent + "/" + child;
    }

    public static boolean matches(String name, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        return name.matches(regex);
    }
}
