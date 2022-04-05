package com.webhopper.poloniex;

public class BookEntry {
    private final double price;
    private final int quantity;

    public BookEntry(double price, int quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
