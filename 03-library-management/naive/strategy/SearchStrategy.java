/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// strategy/SearchStrategy.java — Linear-scan search algorithms (by title, author, ISBN)
// DESIGN PATTERN: Strategy

import java.util.ArrayList;
import java.util.List;

interface SearchStrategy {            // interface = contract; any search algorithm MUST define these
    List<Book> search(List<Book> books, String query);
    String getName();
}

class TitleSearch implements SearchStrategy { // implements = fulfills the SearchStrategy contract
    @Override
    public List<Book> search(List<Book> books, String query) {
        List<Book> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public String getName() { return "Title Search"; }
}

class AuthorSearch implements SearchStrategy { // implements = fulfills the SearchStrategy contract
    @Override
    public List<Book> search(List<Book> books, String query) {
        List<Book> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Book book : books) {
            if (book.getAuthor().toLowerCase().contains(lowerQuery)) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public String getName() { return "Author Search"; }
}

class ISBNSearch implements SearchStrategy { // implements = fulfills the SearchStrategy contract
    @Override
    public List<Book> search(List<Book> books, String query) {
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getIsbn().equals(query)) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public String getName() { return "ISBN Search"; }
}
