/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MusicPlayer.java — Controls playback and manages the play queue
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private PlayerState state;                // private = state pattern; current player state
    private Playlist currentPlaylist;         // private = currently loaded playlist
    private PlaylistIterator iterator;        // private = traversal logic encapsulated
    private Queue playQueue;                  // private = user-queued songs take priority
    private Song currentSong;                 // private = what's playing now
    private RepeatMode repeatMode;            // private = repeat behavior setting
    private ShuffleStrategy shuffleStrategy;  // private = pluggable shuffle algorithm
    private boolean shuffleEnabled;           // private = shuffle on/off flag
    private List<PlayerObserver> observers;   // private = registered event listeners

    public MusicPlayer() {
        this.state = new StoppedState(); this.playQueue = new Queue();
        this.repeatMode = RepeatMode.NONE; this.shuffleEnabled = false;
        this.observers = new ArrayList<>();
    }

    public void addObserver(PlayerObserver observer) { observers.add(observer); }
    private void notifyTrackChanged() { for (PlayerObserver obs : observers) obs.onTrackChanged(currentSong); }
    private void notifyStateChanged() { for (PlayerObserver obs : observers) obs.onStateChanged(state.getStateName()); }

    public void loadPlaylist(Playlist playlist) {
        this.currentPlaylist = playlist;
        if (shuffleEnabled && shuffleStrategy != null) {
            this.iterator = playlist.createShuffledIterator(shuffleStrategy, repeatMode);
        } else { this.iterator = playlist.createIterator(repeatMode); }
        this.currentSong = iterator.current();
    }

    public void play() { state.play(this); notifyStateChanged(); }
    public void pause() { state.pause(this); notifyStateChanged(); }
    public void stop() { state.stop(this); notifyStateChanged(); }

    public Song next() {
        if (!playQueue.isEmpty()) { currentSong = playQueue.dequeue(); }
        else if (iterator != null) {
            Song nextSong = iterator.next();
            if (nextSong != null) { currentSong = nextSong; }
            else { System.out.println("End of playlist reached"); stop(); return null; }
        }
        if (currentSong != null) {
            currentSong.incrementPlayCount(); state = new PlayingState();
            System.out.println("Now playing: " + currentSong); notifyTrackChanged();
        }
        return currentSong;
    }

    public Song previous() {
        if (iterator != null) {
            Song prevSong = iterator.previous();
            if (prevSong != null) {
                currentSong = prevSong; state = new PlayingState();
                System.out.println("Now playing: " + currentSong); notifyTrackChanged();
            }
        }
        return currentSong;
    }

    public void addToQueue(Song song) { playQueue.enqueue(song); System.out.println("Added to queue: " + song); }

    public void setRepeatMode(RepeatMode mode) {
        this.repeatMode = mode;
        if (iterator != null) iterator.setRepeatMode(mode);
        System.out.println("Repeat mode: " + mode);
    }

    public void enableShuffle(ShuffleStrategy strategy) {
        this.shuffleEnabled = true; this.shuffleStrategy = strategy;
        if (currentPlaylist != null) {
            this.iterator = currentPlaylist.createShuffledIterator(strategy, repeatMode);
            this.currentSong = iterator.current();
        }
        System.out.println("Shuffle enabled (" + strategy.getName() + ")");
    }

    public void disableShuffle() {
        this.shuffleEnabled = false;
        if (currentPlaylist != null) {
            this.iterator = currentPlaylist.createIterator(repeatMode);
            this.currentSong = iterator.current();
        }
        System.out.println("Shuffle disabled");
    }

    public Song getCurrentSong() { return currentSong; }
    public PlayerState getState() { return state; }
    public void setState(PlayerState state) { this.state = state; }
}
