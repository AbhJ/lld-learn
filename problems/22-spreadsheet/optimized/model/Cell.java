/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Cell.java — Cell with cached computed value for incremental updates
public class Cell {
    private String id;
    private CellValue value;
    // cache the computed numeric value to avoid re-evaluation when nothing changed
    private double cachedNumeric;                         // avoids recomputing formula if deps unchanged
    private boolean dirty;                                // dirty flag = O(1) invalidation, lazy recompute on read

    public Cell(String id) {
        this.id = id;
        this.value = null;
        this.dirty = true;
    }

    public String getId() { return id; }
    public CellValue getValue() { return value; }

    public void setValue(CellValue value) {
        this.value = value;
        this.dirty = true;
    }

    public void markDirty() { this.dirty = true; }
    public boolean isDirty() { return dirty; }

    public double getNumericValue(Spreadsheet sheet) {
        if (value == null) return 0;
        if (dirty) {
            cachedNumeric = value.getNumericValue(sheet);
            dirty = false;
        }
        return cachedNumeric;
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
        this.dirty = true;
    }
}
