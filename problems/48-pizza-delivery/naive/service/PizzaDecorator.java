/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// service/PizzaDecorator.java — Adds extras to a pizza
public abstract class PizzaDecorator { // abstract = can't instantiate directly; subclasses provide specifics
    protected String name;  // protected = subclasses (ExtraCheese etc.) can access directly
    protected double cost;  // protected = subclasses set cost via super() constructor
    public PizzaDecorator(String name, double cost) { this.name = name; this.cost = cost; }
    public String getName() { return name; }
    public double getCost() { return cost; }
    @Override public String toString() { return "+ " + name + ": $" + String.format("%.2f", cost); }
}

class ExtraCheese extends PizzaDecorator { public ExtraCheese() { super("Extra Cheese", 2.50); } } // extends = inherits from PizzaDecorator
class ExtraSauce extends PizzaDecorator { public ExtraSauce() { super("Extra Sauce", 1.50); } } // extends = another concrete decorator add-on
