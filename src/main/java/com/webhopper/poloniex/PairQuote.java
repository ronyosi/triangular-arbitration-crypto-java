package com.webhopper.poloniex;

import java.math.BigDecimal;

public class PairQuote {
    private String pair;
    private String base;
    private String quote;
    private BigDecimal bid;
    private BigDecimal ask;

    public PairQuote(String pair, String base, String quote, BigDecimal bid, BigDecimal ask) {
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

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }
}
