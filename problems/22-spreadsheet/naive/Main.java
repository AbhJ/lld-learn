/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Demonstrates the spreadsheet with formulas, undo, and circular detection
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Spreadsheet Demo (Naive) ===\n");

        Spreadsheet sheet = new Spreadsheet();
        sheet.addObserver(new LoggingCellObserver());

        System.out.println("--- Setting Cell Values ---");
        sheet.setCellValue("A1", 10.0);
        sheet.setCellValue("A2", 20.0);
        sheet.setCellValue("A3", 30.0);
        sheet.setCellText("B1", "Hello");
        System.out.println("A1=10, A2=20, A3=30, B1=\"Hello\"");

        System.out.println("\n--- Formula: A4 = A1 + A2 ---");
        sheet.setCellFormula("A4", "A1+A2");
        System.out.println("A4 = " + sheet.getCellDisplayValue("A4"));

        System.out.println("\n--- Update A1 → recalculates all dependents ---");
        sheet.setCellValue("A1", 50.0);
        System.out.println("A1 = " + sheet.getCellDisplayValue("A1"));
        System.out.println("A4 = " + sheet.getCellDisplayValue("A4"));

        System.out.println("\n--- SUM(A1:A3) ---");
        sheet.setCellFormula("A5", "SUM(A1:A3)");
        System.out.println("A5 = " + sheet.getCellDisplayValue("A5"));

        System.out.println("\n--- Undo/Redo ---");
        sheet.undo();
        System.out.println("After undo A5: A5 = " + sheet.getCellDisplayValue("A5"));
        sheet.redo();
        System.out.println("After redo A5: A5 = " + sheet.getCellDisplayValue("A5"));

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
