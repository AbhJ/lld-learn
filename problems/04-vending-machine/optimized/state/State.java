/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/State.java — State interface for vending machine FSM
// DESIGN PATTERN: State

interface State {                      // interface = contract; each state MUST handle all actions
    void insertCoin(VendingMachine machine, Coin coin);
    void selectProduct(VendingMachine machine, String productCode);
    void dispense(VendingMachine machine);
    void cancel(VendingMachine machine);
    String getName();
}
