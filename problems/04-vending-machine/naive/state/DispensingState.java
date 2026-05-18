/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/DispensingState.java — Dispensing product and returning change

import java.util.Map;

class DispensingState implements State {
    @Override
    public void insertCoin(VendingMachine machine, Coin coin) {
        System.out.println("  Please wait, dispensing in progress.");
    }

    @Override
    public void selectProduct(VendingMachine machine, String productCode) {
        System.out.println("  Please wait, dispensing in progress.");
    }

    @Override
    public void dispense(VendingMachine machine) {
        Product product = machine.getSelectedProduct();
        machine.getInventory().dispense(product.getCode());
        machine.deductInsertedAmount(product.getPriceInCents());

        System.out.println("  Dispensing " + product.getName() + "...");

        int changeAmount = machine.getInsertedAmount();
        if (changeAmount > 0) {
            Map<Coin, Integer> change = machine.getChangeStrategy().calculateChange(changeAmount);
            System.out.println("  Change returned: " + ChangeCalculator.formatChange(change) +
                    " (" + Coin.formatCents(changeAmount) + ")");
        } else {
            System.out.println("  Change: none (exact amount)");
        }

        machine.resetInsertedAmount();
        machine.setSelectedProduct(null);
        machine.setState(new IdleState());

        // Fire observer events AFTER state has settled, so listeners see consistent post-sale state.
        machine.fireDispensed(product, changeAmount);
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("  Cannot cancel during dispensing.");
    }

    @Override
    public String getName() { return "DISPENSING"; }
}
