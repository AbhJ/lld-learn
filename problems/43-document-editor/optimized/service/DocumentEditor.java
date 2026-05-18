/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/DocumentEditor.java — Editor using GapBuffer for O(1) inserts and reverse-diff undo
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentEditor {
    private GapBuffer buffer;                 // GapBuffer = O(1) insert at cursor vs O(n) String concat
    private String title;
    private List<EditCommand> commandHistory;
    private List<EditCommand> redoStack;
    private VersionHistory versionHistory;
    private Map<String, Collaborator> collaborators;
    private int versionCounter;

    public DocumentEditor(String title) {
        this.title = title;
        this.buffer = new GapBuffer();
        this.commandHistory = new ArrayList<>();
        this.redoStack = new ArrayList<>();
        this.versionHistory = new VersionHistory();
        this.collaborators = new HashMap<>();
        this.versionCounter = 0;
    }

    public String getFullText() { return buffer.getText(); }
    public int length() { return buffer.length(); }
    public VersionHistory getVersionHistory() { return versionHistory; }

    public Collaborator addCollaborator(String userId, String name) {
        Collaborator collab = new Collaborator(userId, name);
        collaborators.put(userId, collab);
        return collab;
    }

    public void insertText(int position, String text) {
        EditCommand cmd = new InsertText(buffer, position, text);
        cmd.execute();
        commandHistory.add(cmd);
        redoStack.clear();
        System.out.println(cmd.getDescription());
    }

    public void deleteText(int position, int length) {
        EditCommand cmd = new DeleteText(buffer, position, length);
        cmd.execute();
        commandHistory.add(cmd);
        redoStack.clear();
        System.out.println(cmd.getDescription());
    }

    // WHY: Undo uses reverse-diff (stored deleted text) instead of full snapshot
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
        // WHY: Store only text string, not full object graph
        Version version = new Version(verId, buffer.getText(), authorId, description);
        versionHistory.saveVersion(version);
        return verId;
    }

    public void restoreVersion(String versionId) {
        Version version = versionHistory.getVersion(versionId);
        if (version != null) {
            this.buffer = new GapBuffer(version.getSnapshotText());
            commandHistory.clear();
            redoStack.clear();
        }
    }
}
