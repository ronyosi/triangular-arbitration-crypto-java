package com.webhopper.poloniex;

public class PairQuote {
    private String pair;
    private String base;
    private String quote;

    public PairQuote(String pair, String base, String quote) {
        this.pair = pair;
        this.base = base;
        this.quote = quote;
    }

    public String getPair() {
        return pair;
    }

    public String getBase() {
        return base;
    }

    public String getQuote() {
        return quote;
    }
}
