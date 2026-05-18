/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Cell.java — Spreadsheet cell with value and state save/restore
public class Cell {
    private String id;                                    // private = only Cell manages its own identity
    private CellValue value;                              // private = encapsulates polymorphic value type

    public Cell(String id) {
        this.id = id;
        this.value = null;
    }

    public Cell(String id, CellValue value) {
        this.id = id;
        this.value = value;
    }

    public String getId() { return id; }
    public CellValue getValue() { return value; }

    public void setValue(CellValue value) {
        this.value = value;
    }

    public double getNumericValue(Spreadsheet sheet) {
        if (value == null) return 0;
        return value.getNumericValue(sheet);
    }

    public String getDisplayValue(Spreadsheet sheet) {
        if (value == null) return "";
        return value.getDisplayValue(sheet);
    }

    public CellMemento saveState() {
        return new CellMemento(id, value != null ? value.copy() : null);
    }

    public void restoreState(CellMemento memento) {
        this.value = memento.getValue();
    }
}
