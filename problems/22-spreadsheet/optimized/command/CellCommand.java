/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// command/CellCommand.java — Undoable command for cell value changes
public class CellCommand {                                // Command pattern = encapsulates cell change for undo/redo
    private Spreadsheet spreadsheet;
    private String cellId;
    private CellMemento previousState;                    // stores snapshot for O(1) undo restore
    private CellValue newValue;

    public CellCommand(Spreadsheet spreadsheet, String cellId, CellValue newValue) {
        this.spreadsheet = spreadsheet;
        this.cellId = cellId;
        this.newValue = newValue;
    }

    public void execute() {
        Cell cell = spreadsheet.getOrCreateCell(cellId);
        this.previousState = cell.saveState();
        spreadsheet.setCellValueInternal(cellId, newValue);
    }

    public void undo() {
        Cell cell = spreadsheet.getOrCreateCell(cellId);
        cell.restoreState(previousState);
        spreadsheet.recalculateDependents(cellId);
    }

    public void redo() {
        spreadsheet.setCellValueInternal(cellId, newValue);
    }

    public String getCellId() { return cellId; }
}
