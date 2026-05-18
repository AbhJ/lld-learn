/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/model/Song.java — Immutable song representation

public class Song {
    private final String songId;              // final = immutable after construction; safe to share across threads
    private final String title;               // final = guarantees safe publication to all threads
    private final int durationMs;             // final = no synchronization needed to read

    public Song(String songId, String title, int durationMs) {
        this.songId = songId;
        this.title = title;
        this.durationMs = durationMs;
    }

    public String getSongId() { return songId; }
    public String getTitle() { return title; }
    public int getDurationMs() { return durationMs; }

    @Override
    public String toString() { return title + " (" + songId + ")"; }
}
