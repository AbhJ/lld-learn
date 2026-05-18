/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PlaylistIterator.java — Iterates through playlist respecting repeat and shuffle
import java.util.List;

public class PlaylistIterator {
    private List<Song> songs;                 // private = song list reference
    private List<Integer> order;              // private = index order (sequential or shuffled)
    private int currentIndex;                 // private = current position in traversal
    private RepeatMode repeatMode;            // private = controls wrap-around behavior

    public PlaylistIterator(List<Song> songs, List<Integer> order, RepeatMode repeatMode) {
        this.songs = songs; this.order = order; this.currentIndex = 0; this.repeatMode = repeatMode;
    }

    public Song current() {
        if (songs.isEmpty()) return null;
        return songs.get(order.get(currentIndex));
    }

    public Song next() {
        if (songs.isEmpty()) return null;
        if (repeatMode == RepeatMode.ONE) return current();
        currentIndex++;
        if (currentIndex >= order.size()) {
            if (repeatMode == RepeatMode.ALL) { currentIndex = 0; }
            else { currentIndex = order.size() - 1; return null; }
        }
        return current();
    }

    public Song previous() {
        if (songs.isEmpty()) return null;
        currentIndex--;
        if (currentIndex < 0) {
            if (repeatMode == RepeatMode.ALL) { currentIndex = order.size() - 1; }
            else { currentIndex = 0; }
        }
        return current();
    }

    public boolean hasNext() {
        if (repeatMode == RepeatMode.ALL || repeatMode == RepeatMode.ONE) return true;
        return currentIndex < order.size() - 1;
    }

    public void setRepeatMode(RepeatMode mode) { this.repeatMode = mode; }
    public void reset() { this.currentIndex = 0; }
    public void setOrder(List<Integer> newOrder) { this.order = newOrder; this.currentIndex = 0; }
}
