package com.webhopper.integrations.poloniex;

import java.util.List;

public class OrderBook {
    private List<BookEntry> asks;
    private List<BookEntry> bids;

    public OrderBook() {
    }

    public void setAsks(List<BookEntry> asks) {
        this.asks = asks;
    }

    public void setBids(List<BookEntry> bids) {
        this.bids = bids;
    }

    public List<BookEntry> getAsks() {
        return asks;
    }

    public List<BookEntry> getBids() {
        return bids;
    }
}
