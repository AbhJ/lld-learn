/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/SlowDataSource.java — Simulated slow backend (e.g., remote DB) used to demo Proxy benefit

public class SlowDataSource implements DataSource<String, String> {
    @Override
    public String load(String key) {
        System.out.println("[ds] loading " + key);    // log every real backend hit
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return "value-for-" + key;
    }
}
