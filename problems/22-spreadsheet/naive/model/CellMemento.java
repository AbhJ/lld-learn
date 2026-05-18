/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/CellMemento.java — Snapshot of cell state for undo support
public class CellMemento {                                // Memento pattern = snapshot for undo
    private String cellId;                                // private = immutable snapshot state
    private CellValue value;                              // private = captured value for restore

    public CellMemento(String cellId, CellValue value) {
        this.cellId = cellId;
        this.value = value;
    }

    public String getCellId() { return cellId; }
    public CellValue getValue() { return value; }
}
