package com.webhopper.poloniex;

import java.math.BigDecimal;

public class BookEntry {
    private final BigDecimal price;
    private final BigDecimal quantity;

    public BookEntry(BigDecimal price, BigDecimal quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}
