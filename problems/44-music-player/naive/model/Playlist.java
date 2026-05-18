/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Playlist.java — An ordered collection of songs
import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;                      // private = playlist identity encapsulated
    private List<Song> songs;                 // private = song list managed through add/remove

    public Playlist(String name) { this.name = name; this.songs = new ArrayList<>(); }

    public String getName() { return name; }
    public List<Song> getSongs() { return songs; }
    public int size() { return songs.size(); }
    public void addSong(Song song) { songs.add(song); }
    public void removeSong(String songId) { songs.removeIf(s -> s.getId().equals(songId)); }

    public PlaylistIterator createIterator(RepeatMode repeatMode) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) order.add(i);
        return new PlaylistIterator(songs, order, repeatMode);
    }

    public PlaylistIterator createShuffledIterator(ShuffleStrategy strategy, RepeatMode repeatMode) {
        List<Integer> shuffledOrder = strategy.shuffle(songs.size());
        return new PlaylistIterator(songs, shuffledOrder, repeatMode);
    }

    @Override
    public String toString() { return name + " (" + songs.size() + " songs)"; }
}
