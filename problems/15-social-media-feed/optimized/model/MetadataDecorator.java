/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/MetadataDecorator.java — Appends a metadata tag (e.g. view count) to the wrapped post.

public class MetadataDecorator extends PostDecorator {
    private final int viewCount;

    public MetadataDecorator(PostComponent wrapped, int viewCount) {
        super(wrapped);
        this.viewCount = viewCount;
    }

    @Override
    public String getDisplayContent() {
        return wrapped.getDisplayContent() + " [viewed " + viewCount + " times]";
    }
}
