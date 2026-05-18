/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Song.java — Represents a music track with title, artist, and duration
public class Song {
    private String id;
    private String title;
    private String artist;
    private String album;
    private int durationSeconds;
    private int playCount;

    public Song(String id, String title, String artist, String album, int durationSeconds) {
        this.id = id; this.title = title; this.artist = artist;
        this.album = album; this.durationSeconds = durationSeconds; this.playCount = 0;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getPlayCount() { return playCount; }
    public void incrementPlayCount() { playCount++; }

    @Override
    public String toString() { return title + " - " + artist; }
}
