/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Request.java — Incoming request to be rate-limited
public class Request {
    private String clientId;    // private = only this class manages the client identity
    private long timestamp;     // private = encapsulates when the request arrived

    public Request(String clientId) { this.clientId = clientId; this.timestamp = System.currentTimeMillis(); }
    public Request(String clientId, long timestamp) { this.clientId = clientId; this.timestamp = timestamp; }
    public String getClientId() { return clientId; }
    public long getTimestamp() { return timestamp; }
}
