/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/OrderState.java — Defines order states and valid transitions (linear check)
// DESIGN PATTERN: State
public interface OrderState {     // interface = contract; all states must answer these questions
    boolean canPay();
    boolean canShip();
    boolean canDeliver();
    boolean canReturn();
    String getStateName();
}

class CreatedState implements OrderState { // implements = fulfills the OrderState contract
    @Override public boolean canPay() { return true; }
    @Override public boolean canShip() { return false; }
    @Override public boolean canDeliver() { return false; }
    @Override public boolean canReturn() { return false; }
    @Override public String getStateName() { return "CREATED"; }
}

class PaidState implements OrderState { // implements = this state answers transition questions
    @Override public boolean canPay() { return false; }
    @Override public boolean canShip() { return true; }
    @Override public boolean canDeliver() { return false; }
    @Override public boolean canReturn() { return false; }
    @Override public String getStateName() { return "PAID"; }
}

class ShippedState implements OrderState { // implements = each state defines its own rules
    @Override public boolean canPay() { return false; }
    @Override public boolean canShip() { return false; }
    @Override public boolean canDeliver() { return true; }
    @Override public boolean canReturn() { return false; }
    @Override public String getStateName() { return "SHIPPED"; }
}

class DeliveredState implements OrderState { // implements = delivered state allows returns
    @Override public boolean canPay() { return false; }
    @Override public boolean canShip() { return false; }
    @Override public boolean canDeliver() { return false; }
    @Override public boolean canReturn() { return true; }
    @Override public String getStateName() { return "DELIVERED"; }
}
