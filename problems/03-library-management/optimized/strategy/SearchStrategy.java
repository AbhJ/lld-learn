/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/SearchStrategy.java — O(1) HashMap-indexed search by title, author, ISBN
// DESIGN PATTERN: Strategy

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface SearchStrategy {            // interface = contract; any search algorithm MUST define these
    List<Book> search(List<Book> books, String query);
    String getName();
    // Optimized strategies can pre-build indexes
    default void buildIndex(List<Book> books) {} // default = optional method; implementors can override or skip
}

/**
 * Optimized: Maintains a HashMap<lowercaseWord, List<Book>> index.
 * Searching is O(1) lookup instead of O(n) scan.
 */
class TitleSearch implements SearchStrategy { // implements = fulfills SearchStrategy contract
    private Map<String, List<Book>> index = new HashMap<>(); // HashMap = O(1) word-to-books lookup
    private boolean indexed = false;  // flag to lazily build index on first search

    @Override
    public void buildIndex(List<Book> books) {
        index.clear();
        for (Book book : books) {
            String[] words = book.getTitle().toLowerCase().split("\\s+");
            for (String word : words) {
                index.computeIfAbsent(word, k -> new ArrayList<>()).add(book);
            }
            // Also index the full title
            index.computeIfAbsent(book.getTitle().toLowerCase(), k -> new ArrayList<>()).add(book);
        }
        indexed = true;
    }

    @Override
    public List<Book> search(List<Book> books, String query) {
        if (!indexed) buildIndex(books);
        String lowerQuery = query.toLowerCase();
        // Try exact word match first (O(1))
        List<Book> result = index.get(lowerQuery);
        if (result != null) return new ArrayList<>(result);
        // Fallback: check partial matches from index keys
        List<Book> partial = new ArrayList<>();
        for (Map.Entry<String, List<Book>> entry : index.entrySet()) {
            if (entry.getKey().contains(lowerQuery)) {
                for (Book b : entry.getValue()) {
                    if (!partial.contains(b)) partial.add(b);
                }
            }
        }
        return partial;
    }

    @Override
    public String getName() { return "Title Search (Indexed)"; }
}

/**
 * Optimized: HashMap<authorNameLower, List<Book>> for O(1) author lookup.
 */
class AuthorSearch implements SearchStrategy { // implements = fulfills SearchStrategy contract
    private Map<String, List<Book>> index = new HashMap<>(); // HashMap = O(1) author-to-books lookup
    private boolean indexed = false;  // flag to lazily build index on first search

    @Override
    public void buildIndex(List<Book> books) {
        index.clear();
        for (Book book : books) {
            String key = book.getAuthor().toLowerCase();
            index.computeIfAbsent(key, k -> new ArrayList<>()).add(book);
            // Also index individual name parts
            String[] parts = key.split("\\s+");
            for (String part : parts) {
                index.computeIfAbsent(part, k -> new ArrayList<>()).add(book);
            }
        }
        indexed = true;
    }

    @Override
    public List<Book> search(List<Book> books, String query) {
        if (!indexed) buildIndex(books);
        String lowerQuery = query.toLowerCase();
        List<Book> result = index.get(lowerQuery);
        if (result != null) return new ArrayList<>(result);
        // Partial match fallback
        List<Book> partial = new ArrayList<>();
        for (Map.Entry<String, List<Book>> entry : index.entrySet()) {
            if (entry.getKey().contains(lowerQuery)) {
                for (Book b : entry.getValue()) {
                    if (!partial.contains(b)) partial.add(b);
                }
            }
        }
        return partial;
    }

    @Override
    public String getName() { return "Author Search (Indexed)"; }
}

/**
 * Optimized: HashMap<ISBN, Book> for O(1) exact lookup.
 */
class ISBNSearch implements SearchStrategy { // implements = fulfills SearchStrategy contract
    private Map<String, Book> index = new HashMap<>(); // HashMap = O(1) ISBN-to-book lookup
    private boolean indexed = false;  // flag to lazily build index on first search

    @Override
    public void buildIndex(List<Book> books) {
        index.clear();
        for (Book book : books) {
            index.put(book.getIsbn(), book);
        }
        indexed = true;
    }

    @Override
    public List<Book> search(List<Book> books, String query) {
        if (!indexed) buildIndex(books);
        List<Book> results = new ArrayList<>();
        Book found = index.get(query);
        if (found != null) results.add(found);
        return results;
    }

    @Override
    public String getName() { return "ISBN Search (Indexed)"; }
}
