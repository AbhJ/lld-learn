/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Playlist.java — Playlist backed by doubly-linked list for O(1) traversal
import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;
    private List<Song> songs;
    private SongNode head;                    // linked list head = O(1) next/prev navigation
    private SongNode tail;                    // tail pointer = O(1) append and circular linking
    private int size;

    public Playlist(String name) {
        this.name = name; this.songs = new ArrayList<>(); this.size = 0;
    }

    public String getName() { return name; }
    public List<Song> getSongs() { return songs; }
    public int size() { return size; }
    public SongNode getHead() { return head; }

    public void addSong(Song song) {
        songs.add(song);
        SongNode node = new SongNode(song);
        if (head == null) { head = node; tail = node; }
        else { tail.setNext(node); node.setPrev(tail); tail = node; }
        size++;
    }

    // WHY: Link tail to head for O(1) wrap-around in repeat-all mode
    public void makeCircular() {
        if (head != null && tail != null) {
            tail.setNext(head);
            head.setPrev(tail);
        }
    }

    public void makeLinear() {
        if (head != null && tail != null) {
            tail.setNext(null);
            head.setPrev(null);
        }
    }

    @Override
    public String toString() { return name + " (" + size + " songs)"; }
}
