/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/VersionHistory.java — Maintains a chronological list of document versions
import java.util.ArrayList;
import java.util.List;

public class VersionHistory {
    private List<Version> versions;

    public VersionHistory() { this.versions = new ArrayList<>(); }

    public void saveVersion(Version version) { versions.add(version); }

    public Version getVersion(String versionId) {
        for (Version v : versions) { if (v.getVersionId().equals(versionId)) return v; }
        return null;
    }

    public Version getLatestVersion() {
        if (versions.isEmpty()) return null;
        return versions.get(versions.size() - 1);
    }

    public List<Version> getAllVersions() { return new ArrayList<>(versions); }
    public int size() { return versions.size(); }

    @Override
    public String toString() { return versions.toString(); }
}
