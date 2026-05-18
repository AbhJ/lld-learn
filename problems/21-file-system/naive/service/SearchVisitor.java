/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SearchVisitor.java — Searches for entries matching a name pattern via traversal
import java.util.ArrayList;
import java.util.List;

public class SearchVisitor implements FileSystemVisitor {  // implements = fulfills the visitor contract
    private String pattern;                               // private = encapsulated search criteria
    private List<String> results;                         // private = accumulates matches internally

    public SearchVisitor(String pattern) {
        this.pattern = pattern;
        this.results = new ArrayList<>();
    }

    @Override
    public void visitFile(File file) {
        if (Path.matches(file.getName(), pattern)) {
            results.add(file.getPath());
        }
    }

    @Override
    public void visitDirectory(Directory directory) {
        if (Path.matches(directory.getName(), pattern)) {
            results.add(directory.getPath());
        }
    }

    public List<String> getResults() { return results; }
}
