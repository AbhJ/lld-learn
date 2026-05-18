/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/SignalDisplay.java — Concrete observer that prints signal state changes

class SignalDisplay implements SignalObserver { // implements = fulfills the SignalObserver contract
    private String displayName;                // private = only this class manages its name

    public SignalDisplay(String displayName) {
        this.displayName = displayName;
    }

    @Override                                  // @Override = implementing the interface method
    public void onSignalChange(String signalId, SignalState oldState, SignalState newState) {
        System.out.println("  [" + displayName + "] " + signalId + ": " + oldState + " -> " + newState);
    }
}
