/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/PedestrianSignal.java — Pedestrian crossing signal coordinated with traffic

enum PedestrianState {                // enum = fixed set of pedestrian signal states
    WALK, DONT_WALK, FLASHING
}

class PedestrianSignal implements SignalObserver { // implements = reacts to traffic signal changes
    private String id;                // private = signal identity
    private String crossingDirection; // private = which crossing this controls
    private PedestrianState state;    // private = updated via onSignalChange callback
    private String associatedSignalId; // private = links to the traffic signal it follows

    public PedestrianSignal(String id, String crossingDirection, String associatedSignalId) {
        this.id = id;
        this.crossingDirection = crossingDirection;
        this.state = PedestrianState.DONT_WALK;
        this.associatedSignalId = associatedSignalId;
    }

    @Override
    public void onSignalChange(String signalId, SignalState oldState, SignalState newState) {
        if (!signalId.equals(associatedSignalId)) return;
        switch (newState) {
            case GREEN:
                setState(PedestrianState.WALK);
                break;
            case YELLOW:
                setState(PedestrianState.FLASHING);
                break;
            case RED:
                setState(PedestrianState.DONT_WALK);
                break;
        }
    }

    private void setState(PedestrianState newState) {
        this.state = newState;
    }

    public PedestrianState getState() { return state; }
    public String getId() { return id; }

    @Override
    public String toString() {
        return id + " (" + crossingDirection + "): " + state;
    }
}
