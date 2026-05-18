# Inventory Management System


## Problem Statement
Design an inventory management system that tracks products and their stock across multiple warehouses. It supports adding/removing stock, transferring between warehouses, querying total stock per product, and reordering when stock falls below a threshold.

The naive variant scans every warehouse for totals. The optimized variant maintains an inverted index from product to warehouse stock entries.

## Requirements

### Functional Requirements
- Register products and warehouses
- Add and remove stock at a warehouse
- Transfer stock between warehouses
- Query total stock for a product across warehouses
- Trigger low-stock alerts and reorders
- Pluggable reorder strategies

### Non-functional Requirements
- O(1) lookup of warehouses stocking a product (optimized)
- Extensible alerting and reorder logic

## Design Patterns Used
| Pattern | Where Used | Why |
|---------|-----------|-----|
| Strategy | ReorderStrategy | Swappable reorder policies (fixed quantity, threshold-based) |
| Repository | InventorySystem | Central registry for products and warehouses |
| Index | InventoryIndex (optimized) | Inverted index for fast product->warehouse lookup |
| Observer | LowStockListener / LoggingLowStockListener | Subscribers notified when stock falls to/below reorder point |

## Folder Structure

```
40-inventory-management/
├── README.md
├── VARIATIONS.md
├── naive/
│   ├── model/        ← Product, Warehouse
│   ├── service/      ← InventorySystem (loops all warehouses)
│   ├── strategy/     ← ReorderStrategy
│   └── Main.java
└── optimized/
    ├── model/        ← Product, Warehouse (with distance), StockEntry
    ├── service/      ← InventoryIndex, InventorySystem
    ├── strategy/     ← ReorderStrategy
    └── Main.java
```

## How to Run

```bash
# Naive
cd naive && mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main

# Optimized
cd optimized && mkdir -p out && javac -d out model/*.java strategy/*.java service/*.java Main.java && java -cp out Main
```

## Naive vs Optimized

| Aspect | Naive | Optimized |
|--------|-------|-----------|
| Stock lookup | Loop all warehouses O(n) | `HashMap<SKU, TreeMap<Warehouse, Entry>>` O(1) |
| Nearest fulfiller | Scan all warehouses, filter, sort | `TreeMap` sorted by distance — first match = nearest |
| Total stock | Sum across all warehouses each time | Sum entries in product's TreeMap |
| Data structure | `HashMap<warehouseId, HashMap<sku, qty>>` | Inverted index: product-centric view |
| Proximity | Not modeled | Warehouses sorted by distance in TreeMap |
| Scale | O(warehouses) per query | O(1) product lookup + O(log n) warehouse access |

---

## Concurrency Version

A third folder `concurrent/` demonstrates the **multithreading challenges** of this problem:

**Race condition:** Simultaneous orders depleting stock below zero — overselling.

```bash
cd concurrent
mkdir -p out
find . -name '*.java' | xargs javac -d out
java -cp out Main
```

### Key Concurrency Techniques Used
| Technique | Where | Why |
|-----------|-------|-----|
| AtomicInteger | Product.stock | CAS loop prevents overselling |
| compareAndSet | tryDeduct() | Only deduct if stock >= quantity at CAS moment |
| ConcurrentHashMap | InventoryService.products | Thread-safe product registry |

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
