# Spreadsheet - Variations

## Variation 1: Collaborative Editing (Real-Time)
**Learning Value:** Teaches conflict resolution in concurrent editing, operational transformation, and real-time sync.

### Additional Requirements
- Multiple users editing the same sheet simultaneously
- Operational Transform (OT) or CRDT for conflict resolution
- Real-time cursor and selection visibility for each user
- Change history and attribution per cell
- Presence indicators showing who is viewing/editing

### Design Changes
- Add `Operation` class representing atomic edits (insert, delete, update)
- Add `OperationalTransform` engine for conflict resolution
- Add `CollaborationSession` managing connected users and their cursors
- Add `ChangeLog` for operation history and undo/redo per user
- Add WebSocket-based `SyncService` for real-time updates

### Solution Approach
Each user edit is captured as an operation (e.g., "set cell A1 to 42"). Operations are sent to a central server which assigns a global sequence number. When concurrent edits conflict, the OT algorithm transforms one operation against the other so both can be applied and reach the same final state. Each client maintains a local copy and optimistically applies edits, reconciling with the server's canonical order. Cursors and selections are broadcast as presence metadata. The change log enables undo per user by inverting their operations.

### Key Classes to Add
```java
public class Operation {
    private String userId;
    private String cellRef;
    private Object oldValue;
    private Object newValue;
    private long sequenceNumber;
    private long timestamp;
}

public class OTEngine {
    private List<Operation> history;
    
    public Operation transform(Operation op1, Operation op2) { ... }
    public void apply(Operation op) { ... }
    public Operation inverse(Operation op) { ... }
}

public class CollaborationSession {
    private Map<String, CursorPosition> userCursors;
    private OTEngine otEngine;
    
    public void onRemoteOperation(Operation op) { ... }
    public void broadcastCursor(String userId, CursorPosition pos) { ... }
}
```

---

## Variation 2: Pivot Tables
**Learning Value:** Introduces data aggregation, grouping algorithms, and dynamic summarization of large datasets.

### Additional Requirements
- Dynamic grouping by row/column fields
- Aggregation functions (SUM, COUNT, AVG, MIN, MAX)
- Filter and sort within pivot groups
- Drill-down into aggregated values
- Auto-refresh when source data changes

### Design Changes
- Add `PivotTable` class with row fields, column fields, and value fields
- Add `Aggregator` strategy interface for different aggregation functions
- Add `PivotCache` for pre-computed aggregations
- Add `DrillDown` to expand an aggregated cell back to source rows
- Register pivot tables as observers of source data changes

### Solution Approach
A pivot table is defined by selecting row fields (grouping), column fields (cross-tabulation), and value fields (what to aggregate). The engine scans source data, groups rows by the row field values, cross-tabulates by column fields, and applies the chosen aggregation function to each cell. Results are cached for performance. When source data changes, the pivot table is marked dirty and recalculated lazily on next access. Drill-down stores the set of source row indices contributing to each aggregated cell, allowing users to inspect underlying data.

### Key Classes to Add
```java
public class PivotTable {
    private CellRange sourceRange;
    private List<String> rowFields;
    private List<String> colFields;
    private List<ValueField> valueFields;
    private Map<PivotKey, Double> cache;
    
    public void refresh() { ... }
    public double getValue(List<String> rowKeys, List<String> colKeys) { ... }
    public List<Row> drillDown(PivotKey key) { ... }
}

public interface Aggregator {
    double aggregate(List<Double> values);
}

public class ValueField {
    private String sourceColumn;
    private Aggregator aggregator;
}
```

---

## Variation 3: Macro Recording
**Learning Value:** Practices action recording, playback systems, and user-defined automation scripting.

### Additional Requirements
- Record user actions as a sequence of commands
- Replay recorded macros on demand
- Simple scripting language for custom macros
- Assign macros to keyboard shortcuts
- Parameterized macros (e.g., apply to selected range)

### Design Changes
- Add `MacroRecorder` using Command pattern to capture actions
- Add `Macro` class as an ordered list of `Command` objects
- Add `ScriptInterpreter` for parsing/executing macro scripts
- Modify all spreadsheet operations to be `Command` objects
- Add `MacroStore` for saving/loading named macros

### Solution Approach
Every user action (set value, format cell, insert row, etc.) is implemented as a Command object. The MacroRecorder intercepts these commands during recording mode and stores them in sequence. Replaying a macro re-executes the stored commands in order. For parameterization, commands use relative references (e.g., "current cell + 1 row") instead of absolute cell references. A simple scripting language (like VBA-lite) allows users to write loops, conditionals, and variable assignments that compile down to command sequences. Macros are stored as serialized command lists and can be shared between users.

### Key Classes to Add
```java
public interface Command {
    void execute(Spreadsheet sheet);
    void undo(Spreadsheet sheet);
    String serialize();
}

public class MacroRecorder {
    private List<Command> recording;
    private boolean isRecording;
    
    public void startRecording() { ... }
    public void stopRecording() { ... }
    public Macro getMacro() { ... }
}

public class Macro {
    private String name;
    private List<Command> commands;
    
    public void replay(Spreadsheet sheet) { ... }
    public void replayOnRange(Spreadsheet sheet, CellRange range) { ... }
}
```

---

## Variation 4: Import/Export
**Learning Value:** Explores trade-offs between fidelity and compatibility in format conversion and serialization.

### Additional Requirements
- Import from CSV, Excel (.xlsx), and other spreadsheet formats
- Export to CSV, Excel, PDF
- Formula conversion between formats (e.g., Excel syntax to internal)
- Preserve formatting, merged cells, and charts during conversion
- Handle large files efficiently with streaming parsers

### Design Changes
- Add `ImportStrategy` interface with CSV, Excel implementations
- Add `ExportStrategy` interface for each output format
- Add `FormulaConverter` to translate between formula syntaxes
- Add `StreamingParser` for memory-efficient large file handling
- Add `FormatMapper` to translate cell styles between formats

### Solution Approach
Use the Strategy pattern for both import and export, with each format having its own parser/writer. CSV import uses streaming line-by-line parsing with configurable delimiters. Excel import uses a library to read the XML-based .xlsx format, mapping Excel cells to internal Cell objects. Formula conversion maintains a mapping table between function names and syntax differences (e.g., VLOOKUP parameters). Formatting is abstracted into an intermediate representation that maps to/from format-specific styles. Large files use streaming to avoid loading everything into memory at once.

### Key Classes to Add
```java
public interface ImportStrategy {
    Spreadsheet importFrom(InputStream input);
}

public interface ExportStrategy {
    void exportTo(Spreadsheet sheet, OutputStream output);
}

public class FormulaConverter {
    private Map<String, String> functionMapping;
    
    public String convertToInternal(String externalFormula, Format sourceFormat) { ... }
    public String convertToExternal(String internalFormula, Format targetFormat) { ... }
}

public class ExcelImporter implements ImportStrategy {
    public Spreadsheet importFrom(InputStream input) { ... }
}
```

---

## Variation 5: Conditional Formatting
**Learning Value:** Deepens understanding of rule-based formatting engines, condition evaluation, and visual state management.

### Additional Requirements
- Rule-based formatting (if value > X, color red)
- Color scales (gradient based on value range)
- Data bars (in-cell bar charts)
- Icon sets (arrows, flags based on thresholds)
- Rules applied to ranges with priority ordering

### Design Changes
- Add `ConditionalFormatRule` with condition and format action
- Add `RulesEngine` that evaluates rules in priority order
- Add `ColorScale`, `DataBar`, `IconSet` as format types
- Modify cell rendering to check conditional format rules
- Add rule inheritance for new rows inserted in formatted ranges

### Solution Approach
Conditional formatting rules are stored as a priority-ordered list per range. Each rule has a condition (comparison, formula-based, or special like top-N) and a format action (background color, font, border, data bar, etc.). When a cell is rendered, the rules engine evaluates all rules that cover that cell in priority order; the first matching rule's format is applied. Color scales compute a gradient position based on min/max values in the range. Data bars calculate bar width as a percentage of the cell value relative to the range max. Rules auto-expand when rows/columns are inserted within the formatted range.

### Key Classes to Add
```java
public class ConditionalFormatRule {
    private CellRange range;
    private Condition condition;
    private FormatAction action;
    private int priority;
    
    public boolean matches(Cell cell) { ... }
    public CellFormat getFormat(Cell cell) { ... }
}

public class RulesEngine {
    private List<ConditionalFormatRule> rules; // sorted by priority
    
    public CellFormat resolveFormat(Cell cell) { ... }
    public void addRule(ConditionalFormatRule rule) { ... }
}

public class ColorScale implements FormatAction {
    private Color minColor, midColor, maxColor;
    
    public CellFormat computeFormat(double value, double min, double max) { ... }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
