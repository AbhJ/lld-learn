/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/Spreadsheet.java — Thread-safe spreadsheet with per-cell locking and version checks

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Spreadsheet {
    private final ConcurrentHashMap<String, Cell> cells = new ConcurrentHashMap<>(); // ConcurrentHashMap = thread-safe map without global lock
    private final AtomicLong globalVersion = new AtomicLong(0); // AtomicLong = lock-free global change counter

    public Cell getOrCreateCell(String id) {
        return cells.computeIfAbsent(id, Cell::new);
    }

    public void setCellValue(String id, double value) {
        Cell cell = getOrCreateCell(id);
        cell.setRawValue(value);
        globalVersion.incrementAndGet();
    }

    public void setCellFormula(String id, String formula, String... dependencyIds) {
        Cell cell = getOrCreateCell(id);
        double computed = evaluateFormula(formula, dependencyIds);
        cell.setFormula(formula, computed);
        globalVersion.incrementAndGet();
    }

    public double getCellValue(String id) {
        Cell cell = cells.get(id);
        return cell == null ? 0.0 : cell.getValue();
    }

    public long getCellVersion(String id) {
        Cell cell = cells.get(id);
        return cell == null ? 0 : cell.getVersion();
    }

    /**
     * Recalculates a cell's formula using current dependency values.
     * Uses version checking to detect stale reads.
     */
    public boolean recalculate(String id, String... dependencyIds) {
        Cell cell = cells.get(id);
        if (cell == null || cell.getFormula() == null) return false;

        // Capture dependency versions before read
        long[] preVersions = new long[dependencyIds.length];
        double[] values = new double[dependencyIds.length];
        for (int i = 0; i < dependencyIds.length; i++) {
            Cell dep = cells.get(dependencyIds[i]);
            if (dep != null) {
                preVersions[i] = dep.getVersion();
                values[i] = dep.getValue();
            }
        }

        // Verify versions haven't changed (no stale reads)
        for (int i = 0; i < dependencyIds.length; i++) {
            Cell dep = cells.get(dependencyIds[i]);
            if (dep != null && dep.getVersion() != preVersions[i]) {
                return false; // Stale read detected, caller should retry
            }
        }

        // Compute sum of dependencies
        double sum = 0;
        for (double v : values) sum += v;

        cell.updateComputedValue(sum);
        return true;
    }

    private double evaluateFormula(String formula, String[] dependencyIds) {
        // Simple SUM formula
        double sum = 0;
        for (String depId : dependencyIds) {
            Cell dep = cells.get(depId);
            if (dep != null) sum += dep.getValue();
        }
        return sum;
    }

    public long getGlobalVersion() {
        return globalVersion.get();
    }

    public int getCellCount() {
        return cells.size();
    }
}
