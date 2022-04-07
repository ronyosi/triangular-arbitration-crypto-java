package com.webhopper.entities;

import java.math.BigDecimal;

public class TriArbTradeLeg {
    private Pair pair;
    private PairTradeDirection pairTradeDirection;

    private String coinIn;
    private String coinOut;
    private BigDecimal surfaceCalcAmountIn;
    private BigDecimal surfaceCalcAmountOut;
    private BigDecimal depthCalcAmountIn;
    private BigDecimal depthCalcAmountOut;

    private BigDecimal swapRate;

    public BigDecimal getSurfaceCalcAmountIn() {
        return surfaceCalcAmountIn;
    }

    public void setSurfaceCalcAmountIn(BigDecimal surfaceCalculationAmountIn) {
        this.surfaceCalcAmountIn = surfaceCalculationAmountIn;
    }

    public BigDecimal getDepthCalcAmountIn() {
        return depthCalcAmountIn;
    }

    public void setDepthCalcAmountIn(BigDecimal depthCalcAmountIn) {
        this.depthCalcAmountIn = depthCalcAmountIn;
    }

    public BigDecimal getDepthCalcAmountOut() {
        return depthCalcAmountOut;
    }

    public void setDepthCalcAmountOut(BigDecimal depthCalcAmountOut) {
        this.depthCalcAmountOut = depthCalcAmountOut;
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

    public void setSurfaceCalcSwapRate(BigDecimal calculatedRate) {
        this.swapRate = calculatedRate;
    }

    public BigDecimal getSurfaceCalcAmountOut() {
        return surfaceCalcAmountOut;
    }

    public void setSurfaceCalcAmountOut(BigDecimal surfaceCalcAmountOut) {
        this.surfaceCalcAmountOut = surfaceCalcAmountOut;
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
        return "TriArbTradeLeg{" +
                "\npair=" + pair +
                "\n, pairTradeDirection=" + pairTradeDirection +
                "\n, coinIn='" + coinIn + '\'' +
                "\n, coinOut='" + coinOut + '\'' +
                "\n, surfaceCalcAmountIn=" + surfaceCalcAmountIn +
                "\n, surfaceCalcAmountOut=" + surfaceCalcAmountOut +
                "\n, depthCalcAmountIn=" + depthCalcAmountIn +
                "\n, depthCalcAmountOut=" + depthCalcAmountOut +
                "\n, swapRate=" + swapRate +
                '}';
    }
}
