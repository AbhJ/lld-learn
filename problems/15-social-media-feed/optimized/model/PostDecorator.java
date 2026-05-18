/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PostDecorator.java — Abstract decorator wrapping a PostComponent.

public abstract class PostDecorator implements PostComponent {
    protected final PostComponent wrapped;

    protected PostDecorator(PostComponent wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String getDisplayContent() {
        return wrapped.getDisplayContent();
    }
}
