/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/AlertListener.java — Observer contract for tracking alerts
//
// Subscribers receive notifications when an alert is raised (speeding,
// geofence enter/exit). Examples: a console logger, a dispatcher, a paging
// system. TrackingService fires events; listeners decide how to react.

interface AlertListener {
    /** A new alert was raised for a vehicle. */
    void onAlert(Alert alert);
}
