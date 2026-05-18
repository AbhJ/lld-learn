/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/MusicPlayer.java — Player using doubly-linked list for O(1) next/prev
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private PlayerState state;
    private Playlist currentPlaylist;
    private SongNode currentNode;             // SongNode = O(1) next/prev via linked list pointers
    private Queue playQueue;
    private Song currentSong;
    private RepeatMode repeatMode;
    private ShuffleStrategy shuffleStrategy;
    private boolean shuffleEnabled;
    private List<PlayerObserver> observers;

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
        if (repeatMode == RepeatMode.ALL) { playlist.makeCircular(); }
        else { playlist.makeLinear(); }
        this.currentNode = playlist.getHead();
        this.currentSong = currentNode != null ? currentNode.getSong() : null;
    }

    public void play() { state.play(this); notifyStateChanged(); }
    public void pause() { state.pause(this); notifyStateChanged(); }
    public void stop() { state.stop(this); notifyStateChanged(); }

    public Song next() {
        // WHY: Separate play queue takes priority over playlist traversal
        if (!playQueue.isEmpty()) { currentSong = playQueue.dequeue(); }
        else if (currentNode != null) {
            if (repeatMode == RepeatMode.ONE) { /* stay on same node */ }
            else {
                // WHY: O(1) traversal via linked list pointer
                SongNode nextNode = currentNode.getNext();
                if (nextNode != null) { currentNode = nextNode; currentSong = currentNode.getSong(); }
                else { System.out.println("End of playlist reached"); stop(); return null; }
            }
        }
        if (currentSong != null) {
            currentSong.incrementPlayCount(); state = new PlayingState();
            System.out.println("Now playing: " + currentSong); notifyTrackChanged();
        }
        return currentSong;
    }

    public Song previous() {
        if (currentNode != null) {
            SongNode prevNode = currentNode.getPrev();
            if (prevNode != null) {
                currentNode = prevNode; currentSong = currentNode.getSong();
                state = new PlayingState();
                System.out.println("Now playing: " + currentSong); notifyTrackChanged();
            }
        }
        return currentSong;
    }

    public void addToQueue(Song song) { playQueue.enqueue(song); System.out.println("Added to queue: " + song); }

    public void setRepeatMode(RepeatMode mode) {
        this.repeatMode = mode;
        if (currentPlaylist != null) {
            if (mode == RepeatMode.ALL) currentPlaylist.makeCircular();
            else currentPlaylist.makeLinear();
        }
        System.out.println("Repeat mode: " + mode);
    }

    public void enableShuffle(ShuffleStrategy strategy) {
        this.shuffleEnabled = true; this.shuffleStrategy = strategy;
        System.out.println("Shuffle enabled (" + strategy.getName() + ")");
        if (currentPlaylist != null) loadPlaylist(currentPlaylist);
    }

    public void disableShuffle() {
        this.shuffleEnabled = false;
        System.out.println("Shuffle disabled");
        if (currentPlaylist != null) loadPlaylist(currentPlaylist);
    }

    public Song getCurrentSong() { return currentSong; }
    public PlayerState getState() { return state; }
    public void setState(PlayerState state) { this.state = state; }
}
