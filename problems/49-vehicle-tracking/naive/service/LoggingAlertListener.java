/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/LoggingAlertListener.java — Console logger; demonstrates observing alerts

class LoggingAlertListener implements AlertListener {
    @Override
    public void onAlert(Alert alert) {
        System.out.println("  [event] ALERT raised: " + alert);
    }
}
