package com.webhopper.poloniex;

import java.math.BigDecimal;

public class BookEntry {
    private final BigDecimal price;
    private final int quantity;

    public BookEntry(BigDecimal price, int quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
