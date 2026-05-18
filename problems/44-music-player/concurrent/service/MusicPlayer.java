/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/service/MusicPlayer.java — AtomicReference + ReentrantLock for thread-safe state transitions

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MusicPlayer {
    private final List<Song> playlist;        // final = playlist reference safe for all threads
    private final AtomicReference<Song> currentSong; // AtomicReference = atomic swap prevents stale reads
    private final AtomicInteger currentIndex; // AtomicInteger = lock-free index access
    private final ReentrantLock transitionLock; // ReentrantLock = mutual exclusion for skip vs onComplete
    private final AtomicInteger advanceCount; // AtomicInteger = thread-safe advance counter
    private final AtomicInteger skipCount;    // AtomicInteger = tracks skip events atomically
    private final AtomicInteger completeCount; // AtomicInteger = tracks completion events atomically

    public MusicPlayer(List<Song> playlist) {
        this.playlist = playlist;
        this.currentIndex = new AtomicInteger(0);
        this.currentSong = new AtomicReference<>(playlist.get(0));
        this.transitionLock = new ReentrantLock();
        this.advanceCount = new AtomicInteger(0);
        this.skipCount = new AtomicInteger(0);
        this.completeCount = new AtomicInteger(0);
    }

    /**
     * Skip to next track. Mutually exclusive with onComplete.
     * Returns true if skip actually advanced the track.
     */
    public boolean skip() {
        transitionLock.lock();
        try {
            int idx = currentIndex.get();
            if (idx >= playlist.size() - 1) return false;
            int nextIdx = idx + 1;
            currentIndex.set(nextIdx);
            currentSong.set(playlist.get(nextIdx));
            advanceCount.incrementAndGet();
            skipCount.incrementAndGet();
            return true;
        } finally {
            transitionLock.unlock();
        }
    }

    /**
     * Called when current track completes naturally. Mutually exclusive with skip.
     * Only advances if the expected song is still the current one.
     */
    public boolean onComplete(Song expectedSong) {
        transitionLock.lock();
        try {
            // Only advance if the song that completed is still current
            if (currentSong.get() != expectedSong) {
                return false; // Song was already skipped — don't double-advance
            }
            int idx = currentIndex.get();
            if (idx >= playlist.size() - 1) return false;
            int nextIdx = idx + 1;
            currentIndex.set(nextIdx);
            currentSong.set(playlist.get(nextIdx));
            advanceCount.incrementAndGet();
            completeCount.incrementAndGet();
            return true;
        } finally {
            transitionLock.unlock();
        }
    }

    public Song getCurrentSong() { return currentSong.get(); }
    public int getCurrentIndex() { return currentIndex.get(); }
    public int getAdvanceCount() { return advanceCount.get(); }
    public int getSkipCount() { return skipCount.get(); }
    public int getCompleteCount() { return completeCount.get(); }
    public int getPlaylistSize() { return playlist.size(); }
}
