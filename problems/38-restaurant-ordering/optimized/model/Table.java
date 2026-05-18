/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Table.java — Physical table with occupancy tracking
public class Table {
    private final int number;              // final = table number is permanent
    private boolean occupied;              // private = only this class tracks status
    private String customer;               // private = encapsulates who is seated
    private boolean vip;                   // private = VIP flag for priority ordering

    public Table(int number, boolean vip) { this.number = number; this.vip = vip; }
    public void assign(String c) { this.customer = c; this.occupied = true; }
    public void free() { this.customer = null; this.occupied = false; }
    public int getNumber() { return number; }
    public boolean isOccupied() { return occupied; }
    public boolean isVip() { return vip; }
    @Override public String toString() { return "Table-" + number + (vip ? "(VIP)" : "") + (occupied ? " [" + customer + "]" : " [free]"); }
}
