/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Document.java — Represents the document content as a collection of segments
import java.util.ArrayList;
import java.util.List;

public class Document {
    private String title;                     // private = document identity encapsulated
    private List<TextSegment> segments;       // private = content manipulated through class methods

    public Document(String title) {
        this.title = title;
        this.segments = new ArrayList<>();
    }

    public String getTitle() { return title; }
    public List<TextSegment> getSegments() { return segments; }

    public String getFullText() {
        StringBuilder sb = new StringBuilder();
        for (TextSegment seg : segments) { sb.append(seg.getText()); }
        return sb.toString();
    }

    public int length() { return getFullText().length(); }

    public void insertText(int position, String text) {
        if (segments.isEmpty()) { segments.add(new TextSegment(text)); return; }
        int currentPos = 0;
        for (int i = 0; i < segments.size(); i++) {
            TextSegment seg = segments.get(i);
            if (position >= currentPos && position <= currentPos + seg.length()) {
                int offset = position - currentPos;
                if (offset == 0) { segments.add(i, new TextSegment(text)); }
                else if (offset == seg.length()) { segments.add(i + 1, new TextSegment(text)); }
                else {
                    String before = seg.getText().substring(0, offset);
                    String after = seg.getText().substring(offset);
                    seg.setText(before);
                    segments.add(i + 1, new TextSegment(text));
                    segments.add(i + 2, new TextSegment(after, seg.getFormatting()));
                }
                return;
            }
            currentPos += seg.length();
        }
        segments.add(new TextSegment(text));
    }

    public String deleteText(int position, int length) {
        String fullText = getFullText();
        if (position < 0 || position + length > fullText.length()) {
            throw new IndexOutOfBoundsException("Invalid delete range");
        }
        String deleted = fullText.substring(position, position + length);
        String newText = fullText.substring(0, position) + fullText.substring(position + length);
        segments.clear();
        if (!newText.isEmpty()) { segments.add(new TextSegment(newText)); }
        return deleted;
    }

    public void applyFormatting(int start, int end, Formatting.Style style) {
        String fullText = getFullText();
        if (start < 0 || end > fullText.length() || start >= end) return;
        List<TextSegment> newSegments = new ArrayList<>();
        int currentPos = 0;
        for (TextSegment seg : segments) {
            int segStart = currentPos;
            int segEnd = currentPos + seg.length();
            if (segEnd <= start || segStart >= end) { newSegments.add(seg); }
            else {
                if (segStart < start) {
                    newSegments.add(new TextSegment(seg.getText().substring(0, start - segStart), seg.getFormatting()));
                }
                int overlapStart = Math.max(segStart, start);
                int overlapEnd = Math.min(segEnd, end);
                TextSegment formatted = new TextSegment(seg.getText().substring(overlapStart - segStart, overlapEnd - segStart), seg.getFormatting());
                formatted.getFormatting().addStyle(style);
                newSegments.add(formatted);
                if (segEnd > end) {
                    newSegments.add(new TextSegment(seg.getText().substring(end - segStart), seg.getFormatting()));
                }
            }
            currentPos += seg.length();
        }
        segments = newSegments;
    }

    public Document snapshot() {
        Document copy = new Document(title);
        for (TextSegment seg : segments) { copy.segments.add(seg.copy()); }
        return copy;
    }

    public void restoreFrom(Document other) {
        this.segments.clear();
        for (TextSegment seg : other.segments) { this.segments.add(seg.copy()); }
    }
}
