/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/Spreadsheet.java — Spreadsheet with incremental recalculation via topological sort
import java.util.*;

public class Spreadsheet {
    private Map<String, Cell> cells;                      // HashMap = O(1) cell access by ID
    private DependencyGraph dependencyGraph;              // tracks which cells to invalidate on change
    private Deque<CellCommand> undoStack;                 // ArrayDeque = faster than Stack for LIFO operations
    private Deque<CellCommand> redoStack;                 // ArrayDeque = no synchronization overhead vs Stack

    // Observer: fan-out for cell change events (value set, dependents recalculated).
    private final List<CellObserver> observers = new ArrayList<>();

    public Spreadsheet() {
        this.cells = new HashMap<>();
        this.dependencyGraph = new DependencyGraph();
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
    }

    // === Observer plumbing ===
    public void addObserver(CellObserver observer) { observers.add(observer); }
    public void removeObserver(CellObserver observer) { observers.remove(observer); }

    private void fireCellChanged(String cellId, String oldValue, String newValue) {
        for (CellObserver o : observers) o.onCellChanged(cellId, oldValue, newValue);
    }

    public Cell getCell(String id) { return cells.get(id); }

    public Cell getOrCreateCell(String id) {
        return cells.computeIfAbsent(id, Cell::new);
    }

    public void setCellValue(String cellId, double value) {
        CellValue cv = new CellValue.NumericValue(value);
        CellCommand cmd = new CellCommand(this, cellId, cv);
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void setCellText(String cellId, String text) {
        CellValue cv = new CellValue.TextValue(text);
        CellCommand cmd = new CellCommand(this, cellId, cv);
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void setCellFormula(String cellId, String expression) {
        Formula formula = new Formula(expression);
        List<String> refs = formula.getReferencedCells();
        if (dependencyGraph.hasCircularDependency(cellId, refs)) {
            throw new IllegalArgumentException("Circular dependency detected for " + cellId);
        }
        CellCommand cmd = new CellCommand(this, cellId, formula);
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    void setCellValueInternal(String cellId, CellValue value) {
        Cell cell = getOrCreateCell(cellId);
        String oldDisplay = cell.getDisplayValue(this);
        // Snapshot dependents' display values BEFORE recalculation so we can report old->new
        List<String> deps = dependencyGraph.getTopologicalOrder(cellId);
        Map<String, String> depOldValues = new HashMap<>();
        for (String dep : deps) {
            Cell c = cells.get(dep);
            if (c != null) depOldValues.put(dep, c.getDisplayValue(this));
        }
        cell.setValue(value);
        if (value instanceof Formula) {
            dependencyGraph.setDependencies(cellId, ((Formula) value).getReferencedCells());
        } else {
            dependencyGraph.clearDependencies(cellId);
        }
        recalculateDependents(cellId);
        String newDisplay = cell.getDisplayValue(this);
        fireCellChanged(cellId, oldDisplay, newDisplay);
        // Fire for dependents whose computed display changed
        for (String dep : deps) {
            Cell c = cells.get(dep);
            if (c == null) continue;
            String oldV = depOldValues.get(dep);
            String newV = c.getDisplayValue(this);
            if (!java.util.Objects.equals(oldV, newV)) {
                fireCellChanged(dep, oldV, newV);
            }
        }
    }

    // WHY: Only mark affected cells as dirty in topological order
    // Cells that aren't affected by this change keep their cached values
    void recalculateDependents(String cellId) {
        List<String> order = dependencyGraph.getTopologicalOrder(cellId);
        for (String dep : order) {
            Cell cell = cells.get(dep);
            if (cell != null) {
                cell.markDirty(); // Will re-evaluate lazily on next read
            }
        }
    }

    public double getCellNumericValue(String cellId) {
        Cell cell = cells.get(cellId);
        if (cell == null) return 0;
        return cell.getNumericValue(this);
    }

    public String getCellDisplayValue(String cellId) {
        Cell cell = cells.get(cellId);
        if (cell == null) return "";
        return cell.getDisplayValue(this);
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            CellCommand cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            CellCommand cmd = redoStack.pop();
            cmd.redo();
            undoStack.push(cmd);
        }
    }

    public SpreadsheetIterator rangeIterator(String start, String end) {
        return new SpreadsheetIterator(this, start, end);
    }
}
