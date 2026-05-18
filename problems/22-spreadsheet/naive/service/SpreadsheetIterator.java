/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SpreadsheetIterator.java — Iterates over a cell range
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class SpreadsheetIterator implements Iterator<Cell> { // implements Iterator = enables for-each style traversal
    private Spreadsheet spreadsheet;
    private List<String> cellIds;                         // private = expanded range of cell references
    private int index;                                    // private = current position in iteration

    public SpreadsheetIterator(Spreadsheet spreadsheet, String startCell, String endCell) {
        this.spreadsheet = spreadsheet;
        this.cellIds = expandRange(startCell, endCell);
        this.index = 0;
    }

    private List<String> expandRange(String start, String end) {
        List<String> cells = new ArrayList<>();
        String colStart = start.replaceAll("\\d", "");
        int rowStart = Integer.parseInt(start.replaceAll("[A-Z]", ""));
        String colEnd = end.replaceAll("\\d", "");
        int rowEnd = Integer.parseInt(end.replaceAll("[A-Z]", ""));
        for (char c = colStart.charAt(0); c <= colEnd.charAt(0); c++) {
            for (int r = rowStart; r <= rowEnd; r++) {
                cells.add(String.valueOf(c) + r);
            }
        }
        return cells;
    }

    @Override
    public boolean hasNext() { return index < cellIds.size(); }

    @Override
    public Cell next() {
        String id = cellIds.get(index++);
        return spreadsheet.getOrCreateCell(id);
    }
}
