/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/BookingSystem.java — Orchestrates booking with synchronized seat management
// DESIGN PATTERN: Facade
//
// FACADE: Main.java talks only to this class.
// Uses BookingObserver (defined in BookingObserver.java) to notify on booking events.

import java.util.ArrayList;
import java.util.List;

class BookingSystem {
    private List<Movie> movies;         // private = movie catalog managed internally
    private List<Screen> screens;       // private = screen list managed internally
    private List<Show> shows;           // private = shows managed internally
    private List<Booking> bookings;     // private = booking history encapsulated
    private SeatLockManager lockManager; // private = seat lock logic encapsulated
    private List<BookingObserver> observers; // private = observer list for notifications

    public BookingSystem() {
        this.movies = new ArrayList<>(); this.screens = new ArrayList<>(); this.shows = new ArrayList<>();
        this.bookings = new ArrayList<>(); this.lockManager = new SeatLockManager(5 * 60 * 1000);
        this.observers = new ArrayList<>();
    }

    public void addObserver(BookingObserver observer) { observers.add(observer); }
    public void addMovie(Movie movie) { movies.add(movie); }
    public void addScreen(Screen screen) { screens.add(screen); }

    public Show addShow(Movie movie, Screen screen, String timing) {
        Show show = new Show("S" + (shows.size() + 1), movie, screen, timing);
        shows.add(show);
        return show;
    }

    public List<Show> getShowsForMovie(String movieId) {
        List<Show> result = new ArrayList<>();
        for (Show show : shows) if (show.getMovie().getMovieId().equals(movieId)) result.add(show);
        return result;
    }

    public boolean lockSeats(String showId, List<String> seatIds, String userId) {
        Show show = getShow(showId);
        if (show == null) return false;
        for (String seatId : seatIds) if (show.getSeatStatus(seatId) == SeatStatus.BOOKED) return false;
        return lockManager.lockSeats(showId, seatIds, userId);
    }

    public Booking confirmBooking(String showId, List<String> seatIds, String userId) {
        Show show = getShow(showId);
        if (show == null) return null;
        for (String seatId : seatIds) if (!userId.equals(lockManager.getLockedBy(showId, seatId))) return null;

        List<Seat> seats = new ArrayList<>();
        double total = 0;
        for (String seatId : seatIds) { Seat seat = show.getScreen().getSeat(seatId); if (seat == null) return null; seats.add(seat); total += seat.getPrice(); }

        Payment payment = new Payment(total, userId);
        if (!payment.process()) { lockManager.unlockSeats(showId, seatIds); return null; }

        for (String seatId : seatIds) show.setSeatStatus(seatId, SeatStatus.BOOKED);
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

    private Show getShow(String showId) { for (Show s : shows) if (s.getShowId().equals(showId)) return s; return null; }
    public List<Movie> getMovies() { return movies; }
    public List<Screen> getScreens() { return screens; }
    public List<Show> getShows() { return shows; }
    public SeatLockManager getLockManager() { return lockManager; }
}
