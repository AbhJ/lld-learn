/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CellValue.java — Abstract cell value (numeric, text, or formula)
public abstract class CellValue {                         // abstract = polymorphic base; Cell stores one of these
    public abstract double getNumericValue(Spreadsheet sheet);
    public abstract String getDisplayValue(Spreadsheet sheet);
    public abstract boolean isFormula();
    public abstract CellValue copy();                     // copy() enables snapshot for undo without aliasing

    public static class NumericValue extends CellValue {
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

    public static class TextValue extends CellValue {
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
