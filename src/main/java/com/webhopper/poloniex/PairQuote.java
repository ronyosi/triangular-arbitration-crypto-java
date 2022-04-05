package com.webhopper.poloniex;

public class PairQuote {
    private String pair;
    private String base;
    private String quote;
    private Double bid;
    private Double ask;

    public PairQuote(String pair, String base, String quote, Double bid, Double ask) {
        this.pair = pair;
        this.base = base;
        this.quote = quote;
        this.bid = bid;
        this.ask = ask;
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

    public void setPair(String pair) {
        this.pair = pair;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
    }

    public Double getAsk() {
        return ask;
    }

    public void setAsk(Double ask) {
        this.ask = ask;
    }
}
