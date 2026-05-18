/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/ParkingObserver.java — Observer pattern interface for parking events
// DESIGN PATTERN: Observer
//
// WHO IMPLEMENTS THIS? → DisplayBoard (in DisplayBoard.java)
// WHO CALLS IT? → ParkingLot calls notifyObservers() on park/unpark
// WHY? → Decouples "spot changed" from "show something on screen/update dashboard".
//         New listeners (e.g., SMS alerts) can be added without changing ParkingLot code.

interface ParkingObserver {            // interface = contract; observers MUST define this method
    void onSpotAvailabilityChanged(ParkingSpot spot, boolean available);
}
