/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TrendingBadgeDecorator.java — Prepends a trending badge to the wrapped post's display content.

public class TrendingBadgeDecorator extends PostDecorator {
    public TrendingBadgeDecorator(PostComponent wrapped) {
        super(wrapped);
    }

    @Override
    public String getDisplayContent() {
        return "[TRENDING] " + wrapped.getDisplayContent();
    }
}
