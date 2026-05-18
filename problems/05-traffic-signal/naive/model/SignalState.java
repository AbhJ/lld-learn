/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/SignalState.java — Traffic signal states with transition logic

enum SignalState {                    // enum = fixed set of signal states; type-safe transitions
    RED("STOP"),
    YELLOW("CAUTION"),
    GREEN("GO");

    private String instruction;       // private = each enum constant has its own instruction text

    SignalState(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() { return instruction; }

    public SignalState next() {
        switch (this) {
            case GREEN: return YELLOW;
            case YELLOW: return RED;
            case RED: return GREEN;
            default: return RED;
        }
    }
}
