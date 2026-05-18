# Document Editor - Variations

## Variation 1: Real-Time Collaboration (CRDT)
**Learning Value:** Teaches conflict-free replicated data types, concurrent editing, and eventual consistency in documents.

### Additional Requirements
- Multiple users editing simultaneously
- Conflict-free eventual consistency
- Cursor presence showing other users
- Operational transformation or CRDT-based merging

### Design Changes
- Add `CRDT` data structure for text (e.g., RGA or Logoot)
- Add `CollaborationService` managing sessions
- Add `OperationLog` for operation history
- Modify `Document` to use CRDT-backed text storage

### Solution Approach
Replace the plain text buffer with a CRDT-based sequence (e.g., RGA - Replicated Growable Array). Each character gets a unique ID (lamport timestamp + user). Insertions and deletions are expressed as CRDT operations that can be applied in any order and converge. The `CollaborationService` broadcasts operations to all connected clients via WebSocket. Each client applies remote operations to their local CRDT replica. Cursor positions are shared as presence metadata.

### Key Classes to Add
```java
public class CRDTDocument {
    private List<CRDTChar> sequence;
    private String documentId;
    private LamportClock clock;

    public CRDTOperation insert(int position, char c, String userId) { /* Generate insert op */ }
    public CRDTOperation delete(int position, String userId) { /* Generate delete op */ }
    public void applyRemote(CRDTOperation op) { /* Merge remote operation */ }
}

public class CollaborationService {
    private Map<String, Set<String>> activeSessions; // docId -> userIds
    public void broadcastOperation(String docId, CRDTOperation op) { /* Send to peers */ }
    public void updateCursorPosition(String docId, String userId, int position) { /* Presence */ }
}
```

---

## Variation 2: Track Changes / Suggestions
**Learning Value:** Introduces change tracking, suggestion workflows, and accept/reject decision pipelines.

### Additional Requirements
- Suggest mode where edits are proposals
- Accept/reject individual suggestions
- Track author of each change
- Visual diff of suggested changes

### Design Changes
- Add `Suggestion` class with proposed edit
- Add `ChangeTracker` to record all modifications
- Add `SuggestionMode` enum (EDITING, SUGGESTING)
- Modify `EditCommand` to create suggestions when in suggest mode

### Solution Approach
When a user is in "Suggesting" mode, their edits don't directly modify the document. Instead, each edit creates a `Suggestion` object containing the original text, proposed text, author, and position. Suggestions are rendered as inline annotations (strikethrough for deletions, colored for additions). The document owner can accept (apply the edit) or reject (discard) each suggestion. The `ChangeTracker` maintains a log of all accepted changes with author attribution.

### Key Classes to Add
```java
public class Suggestion {
    private String id;
    private String authorId;
    private int startPosition;
    private int endPosition;
    private String originalText;
    private String proposedText;
    private SuggestionStatus status; // PENDING, ACCEPTED, REJECTED
    private LocalDateTime createdAt;
}

public class ChangeTracker {
    private List<Suggestion> suggestions;
    public void addSuggestion(Suggestion s) { /* Record */ }
    public void accept(String suggestionId) { /* Apply edit to document */ }
    public void reject(String suggestionId) { /* Discard */ }
    public List<Suggestion> getPendingSuggestions() { /* Filter */ }
}
```

---

## Variation 3: Template System
**Learning Value:** Practices template instantiation, placeholder resolution, and reusable document blueprints.

### Additional Requirements
- Pre-built document templates
- Variable substitution (e.g., {{name}}, {{date}})
- Locked sections that can't be edited
- Template versioning and categories

### Design Changes
- Add `Template` class with placeholders
- Add `TemplateEngine` for variable substitution
- Add `LockedSection` marker in document
- Add `TemplateRepository` for CRUD

### Solution Approach
A `Template` contains document structure with placeholder variables marked as `{{variableName}}`. The `TemplateEngine` takes a template and a map of variable values, substituting placeholders to create a new document. Templates can have `LockedSection` markers that prevent editing in certain regions (e.g., legal boilerplate). Users can create documents from templates, and template authors can version them. Categories allow browsing templates by type (resume, report, contract).

### Key Classes to Add
```java
public class Template {
    private String id;
    private String name;
    private String category;
    private int version;
    private String content; // with {{placeholders}}
    private List<LockedSection> lockedSections;
    private List<String> requiredVariables;
}

public class TemplateEngine {
    private TemplateRepository repository;

    public Document createFromTemplate(String templateId, Map<String, String> variables) {
        // Fetch template, substitute variables, create document with locked sections
    }

    public List<String> getRequiredVariables(String templateId) { /* Parse placeholders */ }
}
```

---

## Variation 4: Export to Multiple Formats
**Learning Value:** Explores trade-offs between format fidelity and portability in multi-format document conversion.

### Additional Requirements
- Export to PDF, DOCX, HTML, Markdown
- Preserve formatting across formats
- Batch export multiple documents
- Custom export settings (page size, margins)

### Design Changes
- Add `ExportService` with strategy pattern
- Add `Exporter` interface with format-specific implementations
- Add `ExportConfig` for settings
- Add `FormattingMapper` to translate internal format

### Solution Approach
Use the Strategy pattern with an `Exporter` interface. Each format (PDF, DOCX, HTML, Markdown) has its own implementation that traverses the document's `TextSegment` list and converts formatting to the target format. The `ExportService` selects the appropriate exporter based on requested format, applies `ExportConfig` settings (margins, font, page size), and produces the output. A `FormattingMapper` translates internal formatting enums to format-specific markup.

### Key Classes to Add
```java
public interface Exporter {
    byte[] export(Document document, ExportConfig config);
    String getFormat();
}

public class PdfExporter implements Exporter {
    public byte[] export(Document document, ExportConfig config) { /* Render to PDF */ }
    public String getFormat() { return "PDF"; }
}

public class ExportService {
    private Map<String, Exporter> exporters;

    public byte[] exportDocument(String docId, String format, ExportConfig config) {
        Exporter exporter = exporters.get(format);
        return exporter.export(getDocument(docId), config);
    }
}
```

---

## Variation 5: Comments and Mentions
**Learning Value:** Deepens understanding of inline annotation systems, mention resolution, and threaded discussions.

### Additional Requirements
- Inline comments on text selections
- @mention users in comments
- Resolve/unresolve comment threads
- Notifications for mentions

### Design Changes
- Add `Comment` class with text anchor
- Add `CommentThread` for replies
- Add `MentionService` for @mention detection and notification
- Modify `Document` to track comment anchors

### Solution Approach
A `Comment` is anchored to a text range (start position, end position) in the document. Comments form threads (original + replies). When a user types `@username`, the `MentionService` detects it, resolves the user, and sends a notification. Comment threads can be resolved (hidden from default view) or reopened. As the document is edited, comment anchors are adjusted — if the anchored text is deleted, the comment becomes orphaned.

### Key Classes to Add
```java
public class Comment {
    private String id;
    private String authorId;
    private String content;
    private int anchorStart;
    private int anchorEnd;
    private LocalDateTime createdAt;
    private boolean resolved;
    private List<Comment> replies;
}

public class MentionService {
    private NotificationManager notificationManager;

    public List<String> extractMentions(String text) { /* Parse @username */ }
    public void notifyMentionedUsers(Comment comment) { /* Send notifications */ }
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
