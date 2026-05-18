/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BookingSystem.java — Lock-free seat reads, per-show locking for booking
// DESIGN PATTERN: Facade
//
// FACADE: Main.java talks only to this class.
// Uses BookingObserver (defined in BookingObserver.java) to notify on booking events.

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class BookingSystem {
    private List<Movie> movies;
    private List<Screen> screens;
    private ConcurrentHashMap<String, Show> showIndex; // ConcurrentHashMap = O(1) thread-safe show lookup by ID
    private List<Show> shows;
    private CopyOnWriteArrayList<Booking> bookings; // CopyOnWriteArrayList = safe iteration while adding bookings
    private SeatLockManager lockManager;
    private List<BookingObserver> observers; // CopyOnWriteArrayList = safe observer notification

    public BookingSystem() {
        this.movies = new ArrayList<>(); this.screens = new ArrayList<>();
        this.showIndex = new ConcurrentHashMap<>(); this.shows = new ArrayList<>();
        this.bookings = new CopyOnWriteArrayList<>(); this.lockManager = new SeatLockManager(5 * 60 * 1000);
        this.observers = new CopyOnWriteArrayList<>();
    }

    public void addObserver(BookingObserver observer) { observers.add(observer); }
    public void addMovie(Movie movie) { movies.add(movie); }
    public void addScreen(Screen screen) { screens.add(screen); }

    public Show addShow(Movie movie, Screen screen, String timing) {
        Show show = new Show("S" + (shows.size() + 1), movie, screen, timing);
        shows.add(show);
        showIndex.put(show.getShowId(), show);
        return show;
    }

    public List<Show> getShowsForMovie(String movieId) {
        List<Show> result = new ArrayList<>();
        for (Show show : shows) if (show.getMovie().getMovieId().equals(movieId)) result.add(show);
        return result;
    }

    public boolean lockSeats(String showId, List<String> seatIds, String userId) {
        Show show = showIndex.get(showId);
        if (show == null) return false;
        for (String seatId : seatIds) if (show.getSeatStatus(seatId) == SeatStatus.BOOKED) return false;
        return lockManager.lockSeats(showId, seatIds, userId);
    }

    public Booking confirmBooking(String showId, List<String> seatIds, String userId) {
        Show show = showIndex.get(showId);
        if (show == null) return null;
        for (String seatId : seatIds) if (!userId.equals(lockManager.getLockedBy(showId, seatId))) return null;

        List<Seat> seats = new ArrayList<>();
        double total = 0;
        for (String seatId : seatIds) { Seat seat = show.getScreen().getSeat(seatId); if (seat == null) return null; seats.add(seat); total += seat.getPrice(); }

        Payment payment = new Payment(total, userId);
        if (!payment.process()) { lockManager.unlockSeats(showId, seatIds); return null; }

        if (!show.tryBookSeats(seatIds)) { payment.refund(); lockManager.unlockSeats(showId, seatIds); return null; }
        lockManager.unlockSeats(showId, seatIds);

        Booking booking = new Booking(userId, show, seats, payment);
        bookings.add(booking);
        for (BookingObserver obs : observers) obs.onBookingConfirmed(booking);
        return booking;
    }

    public boolean cancelBooking(String bookingId) {
        for (Booking b : bookings) {
            if (b.getBookingId().equals(bookingId)) {
                boolean c = b.cancel();
                if (c) for (BookingObserver obs : observers) obs.onBookingCancelled(b);
                return c;
            }
        }
        return false;
    }

    public List<Movie> getMovies() { return movies; }
    public List<Screen> getScreens() { return screens; }
    public List<Show> getShows() { return shows; }
    public SeatLockManager getLockManager() { return lockManager; }
}
