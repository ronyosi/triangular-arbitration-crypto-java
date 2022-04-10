package com.webhopper.integrations.poloniex;

import com.webhopper.entities.CryptoExchange;

import java.math.BigDecimal;

public class UniswapQuote extends Quote {
    private BigDecimal basePrice;

    private BigDecimal quotePrice;

    public UniswapQuote(String pair, String base, String quote, BigDecimal basePrice, BigDecimal quotePrice) {
        super(pair, base, quote, CryptoExchange.UNISWAP);
        this.basePrice = basePrice;
        this.quotePrice = quotePrice;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getQuotePrice() {
        return quotePrice;
    }

    public void setQuotePrice(BigDecimal quotePrice) {
        this.quotePrice = quotePrice;
    }
}
