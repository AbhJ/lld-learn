/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Alert.java — Generates alerts for geo-fence violations and speed breaches
import java.time.LocalDateTime;

public class Alert {
    public enum AlertType { GEOFENCE_ENTER, GEOFENCE_EXIT, SPEEDING } // enum = fixed alert categories; type-safe

    private String id;                  // private = alert ID encapsulated
    private String vehicleId;           // private = which vehicle triggered this alert
    private AlertType type;             // private = what kind of alert
    private String message;             // private = human-readable description
    private LocalDateTime timestamp;    // private = when alert was generated

    public Alert(String id, String vehicleId, AlertType type, String message) {
        this.id = id; this.vehicleId = vehicleId; this.type = type;
        this.message = message; this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public AlertType getType() { return type; }
    public String getMessage() { return message; }

    @Override public String toString() { return "ALERT [" + type + "]: " + message; }
}
