package com.webhopper.entities;

import java.math.BigDecimal;

public class TriArbTradeLeg {
    private Pair pair;
    private PairTradeDirection pairTradeDirection;

    private String coinIn;
    private BigDecimal amountIn;

    private BigDecimal swapRate;

    private String coinOut;
    private BigDecimal amountOut;

    public BigDecimal getAmountIn() {
        return amountIn;
    }

    public void setAmountIn(BigDecimal amountIn) {
        this.amountIn = amountIn;
    }

    public Pair getPair() {
        return pair;
    }

    public void setPair(Pair pair) {
        this.pair = pair;
    }

    public PairTradeDirection getPairTradeDirection() {
        return pairTradeDirection;
    }

    public void setPairTradeDirection(PairTradeDirection pairTradeDirection) {
        this.pairTradeDirection = pairTradeDirection;
    }

    public BigDecimal getSwapRate() {
        return swapRate;
    }

    public void setSwapRate(BigDecimal calculatedRate) {
        this.swapRate = calculatedRate;
    }

    public BigDecimal getAmountOut() {
        return amountOut;
    }

    public void setAmountOut(BigDecimal amountOut) {
        this.amountOut = amountOut;
    }

    public String getCoinIn() {
        return coinIn;
    }

    public void setCoinIn(String coinIn) {
        this.coinIn = coinIn;
    }

    public String getCoinOut() {
        return coinOut;
    }

    public void setCoinOut(String coinOut) {
        this.coinOut = coinOut;
    }

    @Override
    public String toString() {
        return "TriArbTrade{" +
                "\namountIn=" + amountIn +
                ",\n pair=" + pair +
                ",\n pairTradeDirection=" + pairTradeDirection +
                ",\n calculatedRate=" + swapRate +
                ",\n amountOut=" + amountOut +
                ",\n coinIn='" + coinIn + '\'' +
                ",\n coinOut='" + coinOut + '\'' +
                '}';
    }
}
