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
}
