/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/GapBuffer.java — O(1) amortized insert/delete at cursor via gap buffer
public class GapBuffer {
    // WHY: Gap buffer provides O(1) inserts at cursor position vs O(n) for String concat.
    // The gap sits at the editing point, so insertions just fill the gap without copying.
    private char[] buffer;                    // char array = backing store with embedded gap
    private int gapStart;                     // gap sits at edit point; inserts fill gap in O(1)
    private int gapEnd;                       // gap end tracks where post-gap content starts
    private static final int INITIAL_GAP = 64; // static final = shared constant across all instances

    public GapBuffer() {
        this.buffer = new char[INITIAL_GAP];
        this.gapStart = 0;
        this.gapEnd = INITIAL_GAP;
    }

    public GapBuffer(String initial) {
        int len = initial.length();
        this.buffer = new char[len + INITIAL_GAP];
        initial.getChars(0, len, buffer, 0);
        this.gapStart = len;
        this.gapEnd = len + INITIAL_GAP;
    }

    public int length() {
        return buffer.length - (gapEnd - gapStart);
    }

    // WHY: Moving the gap is O(distance) but inserts at gap are O(1)
    public void moveCursor(int position) {
        if (position < gapStart) {
            int moveCount = gapStart - position;
            System.arraycopy(buffer, position, buffer, gapEnd - moveCount, moveCount);
            gapStart = position;
            gapEnd -= moveCount;
        } else if (position > gapStart) {
            int moveCount = position - gapStart;
            System.arraycopy(buffer, gapEnd, buffer, gapStart, moveCount);
            gapStart += moveCount;
            gapEnd += moveCount;
        }
    }

    // WHY: O(1) insert when gap has space; O(n) resize amortized rarely
    public void insert(int position, String text) {
        moveCursor(position);
        if (text.length() > gapEnd - gapStart) {
            expandGap(text.length());
        }
        text.getChars(0, text.length(), buffer, gapStart);
        gapStart += text.length();
    }

    // WHY: O(k) delete where k is the chars deleted, no data shifting needed
    public String delete(int position, int length) {
        moveCursor(position);
        String deleted = new String(buffer, gapEnd, Math.min(length, buffer.length - gapEnd));
        gapEnd += length;
        return deleted;
    }

    private void expandGap(int needed) {
        int newGapSize = Math.max(INITIAL_GAP, needed * 2);
        char[] newBuffer = new char[buffer.length + newGapSize];
        System.arraycopy(buffer, 0, newBuffer, 0, gapStart);
        int afterGapLen = buffer.length - gapEnd;
        System.arraycopy(buffer, gapEnd, newBuffer, newBuffer.length - afterGapLen, afterGapLen);
        gapEnd = newBuffer.length - afterGapLen;
        buffer = newBuffer;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder(length());
        sb.append(buffer, 0, gapStart);
        sb.append(buffer, gapEnd, buffer.length - gapEnd);
        return sb.toString();
    }

    public String substring(int start, int end) {
        String full = getText();
        return full.substring(start, end);
    }
}
