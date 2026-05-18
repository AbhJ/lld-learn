/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/DisplayBoard.java — Concrete observer that prints spot status changes

class DisplayBoard implements ParkingObserver { // implements = this class fulfills the ParkingObserver contract
    private String boardName;                   // private = only this class manages its own name

    public DisplayBoard(String boardName) {
        this.boardName = boardName;
    }

    @Override                                   // @Override = implementing the interface method
    public void onSpotAvailabilityChanged(ParkingSpot spot, boolean available) {
        String status = available ? "AVAILABLE" : "OCCUPIED";
        System.out.println("  [" + boardName + "] Spot " + spot.getSpotId() + " on Level " + spot.getLevel() + " is now " + status);
    }
}
