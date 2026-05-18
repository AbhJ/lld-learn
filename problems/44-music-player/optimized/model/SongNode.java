/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/SongNode.java — Doubly-linked list node for O(1) next/prev navigation
public class SongNode {
    private Song song;                        // payload of the doubly-linked list node
    private SongNode next;                    // pointer to next = O(1) forward traversal
    private SongNode prev;                    // pointer to prev = O(1) backward traversal

    public SongNode(Song song) { this.song = song; }

    public Song getSong() { return song; }
    public SongNode getNext() { return next; }
    public SongNode getPrev() { return prev; }
    public void setNext(SongNode next) { this.next = next; }
    public void setPrev(SongNode prev) { this.prev = prev; }
}
