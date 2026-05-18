/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Rating.java — Customer rating for restaurant or delivery agent

public class Rating {
    private String ratingId;            // unique rating identifier
    private String orderId;             // which order this rating is for
    private double restaurantRating;    // restaurant score (1-5)
    private double agentRating;         // delivery agent score (1-5)
    private String comment;             // customer text feedback
    private static int counter = 0;     // shared ID generator

    public Rating(String orderId, double restaurantRating, double agentRating, String comment) {
        this.ratingId = "RAT-" + (++counter);
        this.orderId = orderId;
        this.restaurantRating = restaurantRating;
        this.agentRating = agentRating;
        this.comment = comment;
    }

    public String getRatingId() { return ratingId; }
    public String getOrderId() { return orderId; }
    public double getRestaurantRating() { return restaurantRating; }
    public double getAgentRating() { return agentRating; }
    public String getComment() { return comment; }

    @Override
    public String toString() {
        return String.format("Rating[%s] Restaurant: %.1f, Agent: %.1f - %s",
                ratingId, restaurantRating, agentRating, comment);
    }
}
