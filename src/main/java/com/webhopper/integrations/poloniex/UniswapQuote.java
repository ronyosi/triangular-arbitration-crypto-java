package com.webhopper.integrations.poloniex;

import com.webhopper.entities.CryptoExchange;

import java.math.BigDecimal;

public class UniswapQuote extends Quote {
    private String quoteContract;

    private BigDecimal token0Price;

    private BigDecimal token1Price;

    private String token0Contract;
    private String token1Contract;

    private Integer token0Decimals;
    private Integer token1Decimals;

    public UniswapQuote(
            final String pair,
            final String base,
            final String quote,
            final BigDecimal token0Price,
            final BigDecimal token1Price
            ) {
        super(pair, base, quote, CryptoExchange.UNISWAP);
        this.token0Price = token0Price;
        this.token1Price = token1Price;
    }

    public BigDecimal getToken0Price() {
        return token0Price;
    }

    public void setToken0Price(BigDecimal token0Price) {
        this.token0Price = token0Price;
    }

    public BigDecimal getToken1Price() {
        return token1Price;
    }

    public void setToken1Price(BigDecimal token1Price) {
        this.token1Price = token1Price;
    }

    public String getToken0Contract() {
        return token0Contract;
    }

    public String getToken1Contract() {
        return token1Contract;
    }

    public Integer getToken0Decimals() {
        return token0Decimals;
    }

    public Integer getToken1Decimals() {
        return token1Decimals;
    }

    public void setToken0Contract(String token0Contract) {
        this.token0Contract = token0Contract;
    }

    public void setToken1Contract(String token1Contract) {
        this.token1Contract = token1Contract;
    }

    public void setToken0Decimals(Integer token0Decimals) {
        this.token0Decimals = token0Decimals;
    }

    public void setToken1Decimals(Integer token1Decimals) {
        this.token1Decimals = token1Decimals;
    }

    public String getQuoteContract() {
        return quoteContract;
    }

    public void setQuoteContract(String quoteContract) {
        this.quoteContract = quoteContract;
    }
}
