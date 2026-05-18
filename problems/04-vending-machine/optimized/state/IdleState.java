/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/IdleState.java — Waiting for coin insertion, rejects product selection

class IdleState implements State {     // implements = fulfills State contract for idle behavior
    @Override
    public void insertCoin(VendingMachine machine, Coin coin) {
        machine.addInsertedAmount(coin.getValue());
        System.out.println("  Inserted: " + coin.name() + " (total inserted: " + Coin.formatCents(machine.getInsertedAmount()) + ")");
        machine.setState(new HasMoneyState());
    }

    @Override
    public void selectProduct(VendingMachine machine, String productCode) {
        System.out.println("  Please insert coins first.");
    }

    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("  Please insert coins and select a product.");
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("  Nothing to cancel.");
    }

    @Override
    public String getName() { return "IDLE"; }
}
