package com.webhopper.entities;

import java.math.BigDecimal;

public class TriArbTrade {
    private final TriArbTradeLeg leg1;
    private final TriArbTradeLeg leg2;
    private final TriArbTradeLeg leg3;
    private final BigDecimal surfaceCalcProfit;
    private final BigDecimal surfaceCalcProfitPercent;
    private BigDecimal depthCalcProfit;
    private BigDecimal depthCalcProfitPercent;
    private DepthCalcState depthCalcState;

    public TriArbTrade(TriArbTradeLeg leg1, TriArbTradeLeg leg2, TriArbTradeLeg leg3, BigDecimal surfaceCalcprofit, BigDecimal profitPercent) {
        this.leg1 = leg1;
        this.leg2 = leg2;
        this.leg3 = leg3;
        this.surfaceCalcProfit = surfaceCalcprofit;
        this.surfaceCalcProfitPercent = profitPercent;
    }

    public TriArbTradeLeg getLeg1() {
        return leg1;
    }

    public TriArbTradeLeg getLeg2() {
        return leg2;
    }

    public TriArbTradeLeg getLeg3() {
        return leg3;
    }

    public BigDecimal getSurfaceCalcProfit() {
        return surfaceCalcProfit;
    }

    public BigDecimal getSurfaceCalcProfitPercent() {
        return surfaceCalcProfitPercent;
    }

    public BigDecimal getDepthCalcProfit() {
        return depthCalcProfit;
    }

    public void setDepthCalcProfit(BigDecimal depthCalcProfit) {
        this.depthCalcProfit = depthCalcProfit;
    }

    public BigDecimal getDepthCalcProfitPercent() {
        return depthCalcProfitPercent;
    }

    public void setDepthCalcProfitPercent(BigDecimal depthCalcProfitPercent) {
        this.depthCalcProfitPercent = depthCalcProfitPercent;
    }

    public DepthCalcState getDepthCalcState() {
        return depthCalcState;
    }

    public void setDepthCalcState(DepthCalcState depthCalcState) {
        this.depthCalcState = depthCalcState;
    }

    @Override
    public String toString() {
        return "TriArbTrade{" +
                "\nleg1=" + leg1 +
                "\nleg2=" + leg2 +
                "\nleg3=" + leg3 +
                "\nsurfaceCalcProfit=" + surfaceCalcProfit +
                "\nsurfaceCalcProfitPercent=" + surfaceCalcProfitPercent +
                "\ndepthCalcProfit=" + depthCalcProfit +
                "\ndepthCalcProfitPercent=" + depthCalcProfitPercent +
                '}';
    }

    public String prettyPrintTradeSummary() {
        return "============Trade details================:\n"
                + "\n Trade sequence: "+ leg1.getPair().getPair() +"=>" + leg2.getPair().getPair()+"=>" + leg3.getPair().getPair()
                + "\n Amount in: " + leg1.getSurfaceCalcAmountIn() + " " +leg1.getCoinIn()
                + "\n\n Surface Profit Calculations"
                + "\n | surfaceCalcProfit=" + surfaceCalcProfit
                + "\n | surfaceCalcProfitPercent=" + surfaceCalcProfitPercent
                + "\n Depth Profit Calculations"
                + "\n | depthCalcState=" + depthCalcState
                + "\n | depthCalcProfit=" + depthCalcProfit
                + "\n | depthCalcProfitPercent=" + depthCalcProfitPercent;
    }
}
