/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/VersionHistory.java — Maintains document versions with text snapshots
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionHistory {
    private List<Version> versions;            // ordered history for chronological access
    private Map<String, Version> versionIndex; // HashMap = O(1) version lookup by ID vs O(n) scan

    public VersionHistory() {
        this.versions = new ArrayList<>();
        this.versionIndex = new HashMap<>();
    }

    public void saveVersion(Version version) {
        versions.add(version);
        versionIndex.put(version.getVersionId(), version);
    }

    // WHY: O(1) lookup via index vs O(n) linear search
    public Version getVersion(String versionId) {
        return versionIndex.get(versionId);
    }

    public Version getLatestVersion() {
        if (versions.isEmpty()) return null;
        return versions.get(versions.size() - 1);
    }

    public int size() { return versions.size(); }

    @Override
    public String toString() { return versions.toString(); }
}
