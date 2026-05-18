/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/PlayerState.java — Defines player states (playing, paused, stopped)
// DESIGN PATTERN: State
public interface PlayerState {                // interface = state pattern; each state handles transitions
    void play(MusicPlayer player);
    void pause(MusicPlayer player);
    void stop(MusicPlayer player);
    String getStateName();
}

class PlayingState implements PlayerState {   // implements = defines behavior when player is playing
    @Override public void play(MusicPlayer player) { System.out.println("Already playing"); }
    @Override public void pause(MusicPlayer player) { player.setState(new PausedState()); System.out.println("Paused: " + player.getCurrentSong().getTitle()); }
    @Override public void stop(MusicPlayer player) { player.setState(new StoppedState()); System.out.println("Stopped playback"); }
    @Override public String getStateName() { return "PLAYING"; }
}

class PausedState implements PlayerState {   // implements = defines behavior when player is paused
    @Override public void play(MusicPlayer player) { player.setState(new PlayingState()); System.out.println("Resumed: " + player.getCurrentSong()); }
    @Override public void pause(MusicPlayer player) { System.out.println("Already paused"); }
    @Override public void stop(MusicPlayer player) { player.setState(new StoppedState()); System.out.println("Stopped playback"); }
    @Override public String getStateName() { return "PAUSED"; }
}

class StoppedState implements PlayerState {  // implements = defines behavior when player is stopped
    @Override public void play(MusicPlayer player) {
        if (player.getCurrentSong() != null) { player.setState(new PlayingState()); System.out.println("Now playing: " + player.getCurrentSong()); }
        else { System.out.println("No song to play"); }
    }
    @Override public void pause(MusicPlayer player) { System.out.println("Cannot pause - stopped"); }
    @Override public void stop(MusicPlayer player) { System.out.println("Already stopped"); }
    @Override public String getStateName() { return "STOPPED"; }
}
