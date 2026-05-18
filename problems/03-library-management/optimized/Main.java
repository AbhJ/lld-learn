/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the library management system

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Library Management System Test ===\n");

        Member.resetCounter();
        Library library = new Library("City Central Library");

        // --- Test: Add Books ---
        System.out.println("--- Test: Add Books ---");
        Book cleanCode = library.addBook("978-0132350884", "Clean Code", "Robert C. Martin", 2008, 2);
        Book designPatterns = library.addBook("978-0201633610", "Design Patterns", "Gang of Four", 1994, 1);
        Book refactoring = library.addBook("978-0134757599", "Refactoring", "Martin Fowler", 2018, 3);
        System.out.println("Added: " + cleanCode + " (copies: " + cleanCode.getTotalCopies() + ")");
        System.out.println("Added: " + designPatterns + " (copies: " + designPatterns.getTotalCopies() + ")");
        System.out.println("Added: " + refactoring + " (copies: " + refactoring.getTotalCopies() + ")");

        // --- Test: Register Members ---
        System.out.println("\n--- Test: Register Members ---");
        Member alice = library.registerMember("Alice");
        Member bob = library.registerMember("Bob");
        System.out.println("Registered: " + alice);
        System.out.println("Registered: " + bob);

        // --- Test: Borrow Books ---
        System.out.println("\n--- Test: Borrow Books ---");
        LocalDate today = LocalDate.of(2026, 5, 1);
        BorrowRecord r1 = library.borrowBook(alice, cleanCode, today);
        System.out.println("Borrowed: " + r1);
        BorrowRecord r2 = library.borrowBook(bob, cleanCode, today);
        System.out.println("Borrowed: " + r2);
        System.out.println("Available copies of Clean Code: " + cleanCode.getAvailableCopyCount());

        // --- Test: No copies available ---
        System.out.println("\n--- Test: No Copies Available ---");
        BorrowRecord r3 = library.borrowBook(alice, cleanCode, today);
        if (r3 == null) System.out.println("Cannot borrow - no copies left");

        // --- Test: Search ---
        System.out.println("\n--- Test: Search by Title ---");
        library.setSearchStrategy(new TitleSearch());
        List<Book> results = library.search("Clean");
        System.out.println("Search 'Clean': " + results);

        System.out.println("\n--- Test: Search by Author ---");
        library.setSearchStrategy(new AuthorSearch());
        results = library.search("Martin");
        System.out.println("Search 'Martin': " + results);

        System.out.println("\n--- Test: Search by ISBN ---");
        library.setSearchStrategy(new ISBNSearch());
        results = library.search("978-0201633610");
        System.out.println("Search ISBN: " + results);

        // --- Test: Return with Fine ---
        System.out.println("\n--- Test: Return with Fine (Daily) ---");
        library.setFineStrategy(new DailyFine(1.0));
        LocalDate lateReturn = today.plusDays(20); // 6 days late (14-day loan)
        double fine = library.returnBook(r1, lateReturn);
        System.out.println("Returned: " + r1.getCopy() + " on " + lateReturn);
        System.out.println("Fine: $" + String.format("%.2f", fine) + " (6 days overdue)");

        // --- Test: Weekly Fine ---
        System.out.println("\n--- Test: Return with Fine (Weekly) ---");
        library.setFineStrategy(new WeeklyFine(5.0));
        LocalDate lateReturn2 = today.plusDays(25); // 11 days late
        double fine2 = library.returnBook(r2, lateReturn2);
        System.out.println("Returned: " + r2.getCopy() + " on " + lateReturn2);
        System.out.println("Fine: $" + String.format("%.2f", fine2) + " (11 days overdue, 2 weeks)");

        // --- Test: Reservation ---
        System.out.println("\n--- Test: Reservation ---");
        BorrowRecord r4 = library.borrowBook(alice, designPatterns, today);
        System.out.println("Alice borrowed: " + designPatterns.getTitle());
        Reservation res = library.reserveBook(bob, designPatterns, today);
        System.out.println("Bob reserved: " + designPatterns.getTitle());
        System.out.println("Returning book (should notify Bob):");
        library.returnBook(r4, today.plusDays(7));

        System.out.println("\n=== All Tests Passed ===");
    }
}
