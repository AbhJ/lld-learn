/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Cell.java — A spreadsheet cell with ReentrantReadWriteLock and version tracking

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Cell {
    private final String id;                              // final = immutable identity; safe to share across threads
    private volatile double value;                        // volatile = writes visible to all threads immediately
    private volatile String formula;                      // volatile = other threads see latest formula assignment
    private final AtomicLong version;                     // AtomicLong = lock-free monotonic version counter
    private final ReentrantReadWriteLock lock;            // RW lock = many readers OR one writer; maximizes concurrency

    public Cell(String id) {
        this.id = id;
        this.value = 0.0;
        this.formula = null;
        this.version = new AtomicLong(0);
        this.lock = new ReentrantReadWriteLock();
    }

    public String getId() { return id; }

    public double getValue() {
        lock.readLock().lock();
        try {
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }

    public long getVersion() { return version.get(); }

    public String getFormula() {
        lock.readLock().lock();
        try {
            return formula;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setRawValue(double val) {
        lock.writeLock().lock();
        try {
            this.value = val;
            this.formula = null;
            this.version.incrementAndGet();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setFormula(String formula, double computedValue) {
        lock.writeLock().lock();
        try {
            this.formula = formula;
            this.value = computedValue;
            this.version.incrementAndGet();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void updateComputedValue(double computedValue) {
        lock.writeLock().lock();
        try {
            this.value = computedValue;
            this.version.incrementAndGet();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ReentrantReadWriteLock getLock() { return lock; }
}
