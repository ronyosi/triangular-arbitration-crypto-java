package com.webhopper.poloniex;

import com.webhopper.entities.CryptoExchange;

import java.math.BigDecimal;

public class PoloniexQuote extends Quote {
    private BigDecimal bid;
    private BigDecimal ask;

    public PoloniexQuote(String pair, String base, String quote, BigDecimal bid, BigDecimal ask) {
        super(pair, base, quote, CryptoExchange.POLONIEX);
        this.bid = bid;
        this.ask = ask;
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
