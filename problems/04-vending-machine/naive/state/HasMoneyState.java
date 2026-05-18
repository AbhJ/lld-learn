/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// state/HasMoneyState.java — Coins inserted, allows product selection or more coins

import java.util.Map;

class HasMoneyState implements State { // implements = fulfills State contract for has-money behavior
    @Override
    public void insertCoin(VendingMachine machine, Coin coin) {
        machine.addInsertedAmount(coin.getValue());
        System.out.println("  Inserted: " + coin.name() + " (total inserted: " + Coin.formatCents(machine.getInsertedAmount()) + ")");
    }

    @Override
    public void selectProduct(VendingMachine machine, String productCode) {
        Product product = machine.getInventory().getProduct(productCode);
        if (product == null) {
            System.out.println("  Invalid product code: " + productCode);
            return;
        }
        if (!machine.getInventory().isAvailable(productCode)) {
            System.out.println("  " + product.getName() + " is SOLD OUT.");
            return;
        }
        if (machine.getInsertedAmount() < product.getPriceInCents()) {
            System.out.println("  Insufficient funds for " + product.getName() + " (" +
                    Coin.formatCents(product.getPriceInCents()) + "). Inserted: " +
                    Coin.formatCents(machine.getInsertedAmount()) + ". Need " +
                    Coin.formatCents(product.getPriceInCents() - machine.getInsertedAmount()) + " more.");
            return;
        }
        machine.setSelectedProduct(product);
        machine.setState(new DispensingState());
        machine.getState().dispense(machine);
    }

    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("  Please select a product first.");
    }

    @Override
    public void cancel(VendingMachine machine) {
        int refund = machine.getInsertedAmount();
        Map<Coin, Integer> change = machine.getChangeStrategy().calculateChange(refund);
        System.out.println("  Cancelled. Refund: " + ChangeCalculator.formatChange(change) +
                " (" + Coin.formatCents(refund) + ")");
        machine.resetInsertedAmount();
        machine.setState(new IdleState());
    }

    @Override
    public String getName() { return "HAS_MONEY"; }
}
