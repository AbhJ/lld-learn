/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Movie.java — Movie with title, genre, and duration

class Movie {
    private String movieId;
    private String title;
    private String genre;
    private int durationMinutes;

    public Movie(String movieId, String title, String genre, int durationMinutes) {
        this.movieId = movieId; this.title = title; this.genre = genre; this.durationMinutes = durationMinutes;
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDurationMinutes() { return durationMinutes; }

    @Override
    public String toString() { return title + " (" + genre + ", " + durationMinutes + " min)"; }
}
