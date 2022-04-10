package com.webhopper.integrations.poloniex;

import com.webhopper.entities.CryptoExchange;

public class Quote {
    private String pair;
    private String base;
    private String quote;
    private CryptoExchange cryptoExchange;

    public Quote(String pair, String base, String quote, CryptoExchange cryptoExchange) {
        this.pair = pair;
        this.base = base;
        this.quote = quote;
        this.cryptoExchange = cryptoExchange;
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

    public CryptoExchange getCryptoExchange() {
        return cryptoExchange;
    }

    public void setCryptoExchange(CryptoExchange cryptoExchange) {
        this.cryptoExchange = cryptoExchange;
    }
}
