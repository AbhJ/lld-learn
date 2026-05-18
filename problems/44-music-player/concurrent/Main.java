/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// concurrent/Main.java — Rapid skip + auto-next racing, verify player advances exactly once per action

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Music Player Demo ===\n");
        System.out.println("Race condition: User presses skip while current track's onComplete fires");
        System.out.println("— player jumps ahead twice.\n");

        // Create a playlist of 20 songs
        List<Song> playlist = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            playlist.add(new Song("song-" + i, "Track " + i, 100));
        }

        MusicPlayer player = new MusicPlayer(playlist);
        int racingPairs = 15; // 15 pairs of skip+complete racing

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(racingPairs * 2);
        AtomicInteger skipSuccesses = new AtomicInteger(0);
        AtomicInteger completeSuccesses = new AtomicInteger(0);

        for (int i = 0; i < racingPairs; i++) {
            // Skip thread
            new Thread(() -> {
                try {
                    startLatch.await();
                    Song before = player.getCurrentSong();
                    boolean skipped = player.skip();
                    if (skipped) skipSuccesses.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();

            // OnComplete thread (simulates track finishing)
            final Song songAtCreation = player.getCurrentSong();
            new Thread(() -> {
                try {
                    startLatch.await();
                    boolean completed = player.onComplete(songAtCreation);
                    if (completed) completeSuccesses.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        int totalAdvances = player.getAdvanceCount();
        int finalIndex = player.getCurrentIndex();

        System.out.println("--- Results ---");
        System.out.println("Playlist size: " + playlist.size());
        System.out.println("Racing pairs (skip + complete): " + racingPairs);
        System.out.println("Skip successes: " + skipSuccesses.get());
        System.out.println("OnComplete successes: " + completeSuccesses.get());
        System.out.println("Total advances: " + totalAdvances);
        System.out.println("Final track index: " + finalIndex);
        System.out.println("Current song: " + player.getCurrentSong());

        // Key invariant: advances should equal finalIndex (started at 0)
        boolean advancesMatchIndex = (totalAdvances == finalIndex);
        // No double-advance: total advances == skip successes + complete successes
        boolean noDoubleAdvance = (totalAdvances == skipSuccesses.get() + completeSuccesses.get());
        // Index should never exceed playlist bounds
        boolean inBounds = (finalIndex < playlist.size());

        System.out.println("\nAdvances match final index: " + advancesMatchIndex);
        System.out.println("No double-advance: " + noDoubleAdvance);
        System.out.println("Index in bounds: " + inBounds);

        boolean passed = advancesMatchIndex && noDoubleAdvance && inBounds;
        System.out.println("\nCorrectness check: " + (passed ? "PASSED" : "FAILED"));
    }
}
