/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Order.java — Represents a customer's pizza order
import java.util.ArrayList;
import java.util.List;

public class Order {
    public enum OrderStatus { PLACED, PREPARING, BAKING, READY, OUT_FOR_DELIVERY, DELIVERED } // enum = fixed order lifecycle stages

    private String id;              // private = order ID encapsulated
    private List<OrderLine> lines;  // private = pizza lines managed via addPizza()
    private OrderStatus status;     // private = status changes via advanceStatus() only

    public Order(String id) { this.id = id; this.lines = new ArrayList<>(); this.status = OrderStatus.PLACED; }

    public String getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public List<OrderLine> getLines() { return lines; }

    public void addPizza(Pizza pizza, List<PizzaDecorator> extras) { lines.add(new OrderLine(pizza, extras)); }

    public double getTotal() { return lines.stream().mapToDouble(OrderLine::getPrice).sum(); }

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

    public static class OrderLine { // static = no reference to enclosing Order; standalone line item
        private Pizza pizza;               // private = which pizza was ordered
        private List<PizzaDecorator> extras; // private = add-ons for this pizza
        public OrderLine(Pizza pizza, List<PizzaDecorator> extras) { this.pizza = pizza; this.extras = extras; }
        public double getPrice() {
            double price = pizza.getBasePrice();
            for (PizzaDecorator extra : extras) price += extra.getCost();
            return price;
        }
    }
}
