package com.webhopper.entities;

public class Pair {
    String base;
    String quote;
    String pair;

    public Pair(String base, String quote, String pair) {
        this.base = base;
        this.quote = quote;
        this.pair = pair;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }
}
