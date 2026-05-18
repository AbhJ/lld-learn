/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CellValue.java — Abstract cell value (numeric, text, or formula)
public abstract class CellValue {                         // abstract = base type for Numeric, Text, Formula
    public abstract double getNumericValue(Spreadsheet sheet); // abstract = each subtype evaluates differently
    public abstract String getDisplayValue(Spreadsheet sheet);
    public abstract boolean isFormula();
    public abstract CellValue copy();                     // abstract = each subtype knows how to clone itself

    public static class NumericValue extends CellValue {   // static inner = doesn't need outer class instance
        private double value;

        public NumericValue(double value) { this.value = value; }
        public double getValue() { return value; }

        @Override
        public double getNumericValue(Spreadsheet sheet) { return value; }
        @Override
        public String getDisplayValue(Spreadsheet sheet) { return String.valueOf(value); }
        @Override
        public boolean isFormula() { return false; }
        @Override
        public CellValue copy() { return new NumericValue(value); }
    }

    public static class TextValue extends CellValue {      // static inner = grouped with parent logically
        private String text;

        public TextValue(String text) { this.text = text; }
        public String getText() { return text; }

        @Override
        public double getNumericValue(Spreadsheet sheet) { return 0; }
        @Override
        public String getDisplayValue(Spreadsheet sheet) { return text; }
        @Override
        public boolean isFormula() { return false; }
        @Override
        public CellValue copy() { return new TextValue(text); }
    }
}
