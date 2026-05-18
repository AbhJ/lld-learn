/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PlayerObserver.java — Observer pattern interface for playback events
// DESIGN PATTERN: Observer
//
// WHO IMPLEMENTS THIS? → ConsolePlayerObserver (in ConsolePlayerObserver.java)
// WHO CALLS IT? → MusicPlayer calls observers on track change and state change
// WHY? → Decouples "playback event happened" from "display/log it".

interface PlayerObserver {            // interface = contract for playback event listeners
    void onTrackChanged(Song newTrack);
    void onStateChanged(String newState);
}
