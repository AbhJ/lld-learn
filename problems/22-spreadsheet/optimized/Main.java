/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates optimized spreadsheet with incremental recalculation
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Spreadsheet Demo (Optimized) ===");
        System.out.println("Optimization: Topological sort-based incremental update\n");

        Spreadsheet sheet = new Spreadsheet();
        sheet.addObserver(new LoggingCellObserver());

        sheet.setCellValue("A1", 10.0);
        sheet.setCellValue("A2", 20.0);
        sheet.setCellValue("A3", 30.0);
        System.out.println("Set A1=10, A2=20, A3=30");

        sheet.setCellFormula("A4", "A1+A2");
        sheet.setCellFormula("A5", "SUM(A1:A3)");
        sheet.setCellFormula("A6", "A4+A5");
        System.out.println("A4=A1+A2, A5=SUM(A1:A3), A6=A4+A5");
        System.out.println("A4=" + sheet.getCellDisplayValue("A4"));
        System.out.println("A5=" + sheet.getCellDisplayValue("A5"));
        System.out.println("A6=" + sheet.getCellDisplayValue("A6"));

        System.out.println("\n--- Update A1=50 (only A4, A5, A6 recalc — not B1, C1, etc.) ---");
        sheet.setCellValue("A1", 50.0);
        System.out.println("A4=" + sheet.getCellDisplayValue("A4") + " (was 30)");
        System.out.println("A5=" + sheet.getCellDisplayValue("A5") + " (was 60)");
        System.out.println("A6=" + sheet.getCellDisplayValue("A6") + " (was 90)");

        System.out.println("\n--- Circular Dependency Detection ---");
        sheet.setCellFormula("C1", "C2+1");
        try {
            sheet.setCellFormula("C2", "C1+1");
        } catch (IllegalArgumentException e) {
            System.out.println("C2=C1+1 → " + e.getMessage());
        }

        System.out.println("\n=== Demo Complete ===");
    }
}
