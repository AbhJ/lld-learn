/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// Main.java — Entry point demonstrating the music player
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Music Player Demo (Naive) ===");

        Song s1 = new Song("1", "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354);
        Song s2 = new Song("2", "Hotel California", "Eagles", "Hotel California", 391);
        Song s3 = new Song("3", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", 482);
        Song s4 = new Song("4", "Yesterday", "Beatles", "Help!", 125);
        Song s5 = new Song("5", "Imagine", "John Lennon", "Imagine", 187);

        Playlist playlist = new Playlist("My Favorites");
        playlist.addSong(s1); playlist.addSong(s2); playlist.addSong(s3);
        playlist.addSong(s4); playlist.addSong(s5);

        MusicPlayer player = new MusicPlayer();
        player.addObserver(new ConsolePlayerObserver("UI"));
        player.loadPlaylist(playlist);

        System.out.println("\n--- Playback ---");
        player.play();
        player.next();
        player.next();
        player.previous();

        System.out.println("\n--- Queue ---");
        player.addToQueue(s4);
        player.next();

        System.out.println("\n--- Shuffle ---");
        player.enableShuffle(new RandomShuffle());
        for (int i = 0; i < 3; i++) player.next();

        System.out.println("\n--- Repeat ---");
        player.disableShuffle();
        player.setRepeatMode(RepeatMode.ONE);
        player.loadPlaylist(playlist);
        player.play();
        player.next();

        System.out.println("\n=== Music Player Demo Complete ===");
    }
}
