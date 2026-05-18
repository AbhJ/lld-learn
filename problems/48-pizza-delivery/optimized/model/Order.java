/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Order.java — Order with concurrent tracking via ConcurrentHashMap
import java.util.ArrayList;
import java.util.List;

public class Order {
    public enum OrderStatus { PLACED, PREPARING, BAKING, READY, OUT_FOR_DELIVERY, DELIVERED } // enum = fixed lifecycle stages

    private String id;              // private = order ID encapsulated
    private List<OrderLine> lines;  // private = pizza lines managed via addPizza()
    private OrderStatus status;     // private = status changes via advanceStatus() only
    // Pre-computed total updated on addPizza instead of recalculating
    private double runningTotal;    // O(1) getTotal() instead of O(n) stream sum each call

    public Order(String id) {
        this.id = id; this.lines = new ArrayList<>();
        this.status = OrderStatus.PLACED; this.runningTotal = 0;
    }

    public String getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public List<OrderLine> getLines() { return lines; }
    public double getTotal() { return runningTotal; }

    public void addPizza(Pizza pizza, List<PizzaDecorator> extras) {
        OrderLine line = new OrderLine(pizza, extras);
        lines.add(line);
        // WHY: O(1) total update instead of O(n) recalculation
        runningTotal += line.getPrice();
    }

    public void advanceStatus() {
        switch (status) {
            case PLACED: status = OrderStatus.PREPARING; break;
            case PREPARING: status = OrderStatus.BAKING; break;
            case BAKING: status = OrderStatus.READY; break;
            case READY: status = OrderStatus.OUT_FOR_DELIVERY; break;
            case OUT_FOR_DELIVERY: status = OrderStatus.DELIVERED; break;
            default: return;
        }
        System.out.println("  Order " + id + " status: " + status);
    }

    public static class OrderLine {
        private Pizza pizza;
        private List<PizzaDecorator> extras;
        public OrderLine(Pizza pizza, List<PizzaDecorator> extras) { this.pizza = pizza; this.extras = extras; }
        public double getPrice() {
            double price = pizza.getBasePrice();
            for (PizzaDecorator extra : extras) price += extra.getCost();
            return price;
        }
    }
}
