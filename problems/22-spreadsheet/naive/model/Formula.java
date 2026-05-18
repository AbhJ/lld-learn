/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Formula.java — Cell formula with references and evaluation logic
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formula extends CellValue {                  // extends CellValue = a formula IS a cell value
    private String expression;                            // private = raw formula text (e.g. "A1+A2")
    private List<String> referencedCells;                 // private = parsed cell references for dependency tracking

    public Formula(String expression) {
        this.expression = expression;
        this.referencedCells = parseReferences(expression);
    }

    public String getExpression() { return expression; }
    public List<String> getReferencedCells() { return referencedCells; }

    @Override
    public boolean isFormula() { return true; }

    @Override
    public CellValue copy() { return new Formula(expression); }

    @Override
    public double getNumericValue(Spreadsheet sheet) {
        return evaluate(expression, sheet);
    }

    @Override
    public String getDisplayValue(Spreadsheet sheet) {
        return String.valueOf(getNumericValue(sheet));
    }

    private List<String> parseReferences(String expr) {
        List<String> refs = new ArrayList<>();
        Pattern rangePattern = Pattern.compile("([A-Z]+\\d+):([A-Z]+\\d+)");
        Matcher rangeMatcher = rangePattern.matcher(expr);
        while (rangeMatcher.find()) {
            refs.addAll(expandRange(rangeMatcher.group(1), rangeMatcher.group(2)));
        }

        String noRanges = expr.replaceAll("[A-Z]+\\d+:[A-Z]+\\d+", "");
        Pattern cellPattern = Pattern.compile("[A-Z]+\\d+");
        Matcher cellMatcher = cellPattern.matcher(noRanges);
        while (cellMatcher.find()) {
            String ref = cellMatcher.group();
            if (!refs.contains(ref)) refs.add(ref);
        }
        return refs;
    }

    private List<String> expandRange(String start, String end) {
        List<String> cells = new ArrayList<>();
        String colStart = start.replaceAll("\\d", "");
        int rowStart = Integer.parseInt(start.replaceAll("[A-Z]", ""));
        String colEnd = end.replaceAll("\\d", "");
        int rowEnd = Integer.parseInt(end.replaceAll("[A-Z]", ""));

        if (colStart.equals(colEnd)) {
            for (int r = rowStart; r <= rowEnd; r++) {
                cells.add(colStart + r);
            }
        } else if (rowStart == rowEnd) {
            for (char c = colStart.charAt(0); c <= colEnd.charAt(0); c++) {
                cells.add(String.valueOf(c) + rowStart);
            }
        }
        return cells;
    }

    private double evaluate(String expr, Spreadsheet sheet) {
        Pattern sumPattern = Pattern.compile("SUM\\(([A-Z]+\\d+):([A-Z]+\\d+)\\)");
        Matcher sumMatcher = sumPattern.matcher(expr);
        if (sumMatcher.find()) {
            List<String> cells = expandRange(sumMatcher.group(1), sumMatcher.group(2));
            double sum = 0;
            for (String ref : cells) {
                Cell cell = sheet.getCell(ref);
                sum += (cell != null) ? cell.getNumericValue(sheet) : 0;
            }
            return sum;
        }

        Pattern avgPattern = Pattern.compile("AVERAGE\\(([A-Z]+\\d+):([A-Z]+\\d+)\\)");
        Matcher avgMatcher = avgPattern.matcher(expr);
        if (avgMatcher.find()) {
            List<String> cells = expandRange(avgMatcher.group(1), avgMatcher.group(2));
            double sum = 0;
            for (String ref : cells) {
                Cell cell = sheet.getCell(ref);
                sum += (cell != null) ? cell.getNumericValue(sheet) : 0;
            }
            return cells.isEmpty() ? 0 : sum / cells.size();
        }

        String resolved = expr;
        List<String> sortedRefs = new ArrayList<>(referencedCells);
        sortedRefs.sort((a, b) -> b.length() - a.length());
        for (String ref : sortedRefs) {
            Cell cell = sheet.getCell(ref);
            double val = (cell != null) ? cell.getNumericValue(sheet) : 0;
            resolved = resolved.replace(ref, String.valueOf(val));
        }
        return evaluateArithmetic(resolved);
    }

    private double evaluateArithmetic(String expr) {
        expr = expr.trim();
        int parenDepth = 0;
        int lastPlusOrMinus = -1;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') parenDepth++;
            else if (c == '(') parenDepth--;
            else if (parenDepth == 0 && (c == '+' || c == '-') && i > 0) {
                lastPlusOrMinus = i;
                break;
            }
        }
        if (lastPlusOrMinus > 0) {
            double left = evaluateArithmetic(expr.substring(0, lastPlusOrMinus));
            char op = expr.charAt(lastPlusOrMinus);
            double right = evaluateArithmetic(expr.substring(lastPlusOrMinus + 1));
            return op == '+' ? left + right : left - right;
        }

        parenDepth = 0;
        int lastMulOrDiv = -1;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') parenDepth++;
            else if (c == '(') parenDepth--;
            else if (parenDepth == 0 && (c == '*' || c == '/')) {
                lastMulOrDiv = i;
                break;
            }
        }
        if (lastMulOrDiv > 0) {
            double left = evaluateArithmetic(expr.substring(0, lastMulOrDiv));
            char op = expr.charAt(lastMulOrDiv);
            double right = evaluateArithmetic(expr.substring(lastMulOrDiv + 1));
            return op == '*' ? left * right : left / right;
        }

        if (expr.startsWith("(") && expr.endsWith(")")) {
            return evaluateArithmetic(expr.substring(1, expr.length() - 1));
        }
        return Double.parseDouble(expr);
    }
}
