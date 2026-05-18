/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/EmergencyOverride.java — Emergency vehicle preemption control

class EmergencyOverride {
    private boolean active;           // private = only activate()/deactivate() modify this
    private String priorityDirection; // private = which direction gets green during emergency

    public EmergencyOverride() {
        this.active = false;
        this.priorityDirection = null;
    }

    public void activate(String direction) {
        this.active = true;
        this.priorityDirection = direction;
    }

    public void deactivate() {
        this.active = false;
        this.priorityDirection = null;
    }

    public boolean isActive() { return active; }
    public String getPriorityDirection() { return priorityDirection; }
}
