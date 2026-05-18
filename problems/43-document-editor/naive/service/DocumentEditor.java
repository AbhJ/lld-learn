/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/DocumentEditor.java — Orchestrates editing, versioning, and collaboration
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentEditor {
    private Document document;                // private = the document being edited
    private List<EditCommand> commandHistory; // private = undo stack of executed commands
    private List<EditCommand> redoStack;      // private = redo stack of undone commands
    private VersionHistory versionHistory;    // private = saved snapshots for version control
    private Map<String, Collaborator> collaborators; // private = active collaborator registry
    private int versionCounter;               // private = auto-incrementing version ID

    public DocumentEditor(String title) {
        this.document = new Document(title);
        this.commandHistory = new ArrayList<>();
        this.redoStack = new ArrayList<>();
        this.versionHistory = new VersionHistory();
        this.collaborators = new HashMap<>();
        this.versionCounter = 0;
    }

    public Document getDocument() { return document; }
    public VersionHistory getVersionHistory() { return versionHistory; }

    public Collaborator addCollaborator(String userId, String name) {
        Collaborator collab = new Collaborator(userId, name);
        collaborators.put(userId, collab);
        return collab;
    }

    public void executeCommand(EditCommand command) {
        command.execute();
        commandHistory.add(command);
        redoStack.clear();
        System.out.println(command.getDescription());
    }

    public void insertText(int position, String text) { executeCommand(new InsertText(document, position, text)); }
    public void deleteText(int position, int length) { executeCommand(new DeleteText(document, position, length)); }
    public void formatText(int start, int end, Formatting.Style style) { executeCommand(new FormatText(document, start, end, style)); }

    public boolean undo() {
        if (commandHistory.isEmpty()) return false;
        EditCommand cmd = commandHistory.remove(commandHistory.size() - 1);
        cmd.undo();
        redoStack.add(cmd);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        EditCommand cmd = redoStack.remove(redoStack.size() - 1);
        cmd.execute();
        commandHistory.add(cmd);
        return true;
    }

    public String saveVersion(String authorId, String description) {
        String verId = "v" + (++versionCounter);
        Version version = new Version(verId, document.snapshot(), authorId, description);
        versionHistory.saveVersion(version);
        return verId;
    }

    public void restoreVersion(String versionId) {
        Version version = versionHistory.getVersion(versionId);
        if (version != null) {
            document.restoreFrom(version.getSnapshot());
            commandHistory.clear();
            redoStack.clear();
        }
    }
}
