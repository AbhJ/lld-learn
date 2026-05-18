/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ConsolePlayerObserver.java — Concrete observer that prints playback events to console
//
// This class IMPLEMENTS PlayerObserver (defined in PlayerObserver.java).

class ConsolePlayerObserver implements PlayerObserver { // implements = fulfills observer contract
    private String name;              // private = observer's display name

    public ConsolePlayerObserver(String name) { this.name = name; }

    @Override
    public void onTrackChanged(Song newTrack) {
        System.out.println("[" + name + "] Track: " + newTrack);
    }

    @Override
    public void onStateChanged(String newState) {
        System.out.println("[" + name + "] State: " + newState);
    }
}
