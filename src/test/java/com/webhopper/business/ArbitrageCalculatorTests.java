package com.webhopper.business;

import com.webhopper.entities.FullTriArbTrade;
import com.webhopper.entities.PairTradeDirection;
import com.webhopper.entities.TriArbTradeLeg;
import com.webhopper.entities.Triangle;
import com.webhopper.poloniex.PairQuote;
import com.webhopper.poloniex.PoloniexApi;
import com.webhopper.poloniex.PolonixService;
import com.webhopper.utils.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static com.webhopper.entities.PairTradeDirection.BASE_TO_QUOTE;
import static com.webhopper.entities.PairTradeDirection.QUOTE_TO_BASE;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbitrageCalculatorTests {
    @Mock
    private PoloniexApi poloniexApi;

    private PolonixService polonixService;

    private StructureTriangles structureTriangles;

    @Before
    public void setup() throws IOException {
        polonixService = new PolonixService(poloniexApi);
        structureTriangles = new StructureTriangles(polonixService);
    }

    @Test
    public void testProfitableSurfaceArbitrageCalculatedCorrectly() throws IOException {
        // 1: Create triangles.
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "ticker_for_1_profitable_triangle.json");
        when(poloniexApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        List<Triangle> triangles = structureTriangles.structure();

        final Map<String, PairQuote> quotes = polonixService.getPricingInfo();

        // 2: Calculate surface rate
        final ArbitrageCalculator arbitrageCalculator = new ArbitrageCalculator(polonixService);
        final List<FullTriArbTrade> candidates = arbitrageCalculator.calculateSurfaceArbitrage(triangles.get(0), quotes, new BigDecimal(500));
        Assert.assertEquals(2, candidates.size());// There should be only one triangle in that file loaded above.
        FullTriArbTrade fullTriArbTradeForward = candidates.get(0);
        FullTriArbTrade fullTriArbTradeReverse = candidates.get(1);

        verifyCalculations(quotes, fullTriArbTradeForward, BASE_TO_QUOTE, QUOTE_TO_BASE, BASE_TO_QUOTE);
        verifyCalculations(quotes, fullTriArbTradeReverse, QUOTE_TO_BASE, QUOTE_TO_BASE, BASE_TO_QUOTE);
    }

    @Test
    public void testUnprofitableSurfaceArbitrageCalculatedCorrectly() throws IOException {
        // 1: Create triangles.
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "ticker_for_1_unprofitable_triangle.json");
        when(poloniexApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        List<Triangle> triangles = structureTriangles.structure();

        final Map<String, PairQuote> quotes = polonixService.getPricingInfo();

        // 2: Calculate surface rate
        final ArbitrageCalculator arbitrageCalculator = new ArbitrageCalculator(polonixService);
        final List<FullTriArbTrade> candidates = arbitrageCalculator.calculateSurfaceArbitrage(triangles.get(0), quotes, new BigDecimal(500));
        Assert.assertEquals(2, candidates.size());// There should be only one triangle in that file loaded above.
        FullTriArbTrade fullTriArbTradeForward = candidates.get(0);
        FullTriArbTrade fullTriArbTradeReverse = candidates.get(1);

        verifyCalculations(quotes, fullTriArbTradeForward, BASE_TO_QUOTE, QUOTE_TO_BASE, BASE_TO_QUOTE);
        verifyCalculations(quotes, fullTriArbTradeReverse, QUOTE_TO_BASE, QUOTE_TO_BASE, BASE_TO_QUOTE);
    }

    private void verifyCoinsInAndOutOfLegsAReCompatible(TriArbTradeLeg leg1, TriArbTradeLeg leg2, TriArbTradeLeg leg3) {
        Assert.assertEquals(leg1.getCoinOut(), leg2.getCoinIn());
        Assert.assertEquals(leg2.getCoinOut(), leg3.getCoinIn());
        Assert.assertEquals(leg3.getCoinOut(), leg1.getCoinIn());
    }

    private void verifyCalculations(Map<String, PairQuote> quotes, FullTriArbTrade fullTriArbTrade,
                                    PairTradeDirection leg1Direction, PairTradeDirection leg2Direction, PairTradeDirection leg3Direction) {
        TriArbTradeLeg leg1 = fullTriArbTrade.getLeg1();
        TriArbTradeLeg leg2 = fullTriArbTrade.getLeg2();
        TriArbTradeLeg leg3 = fullTriArbTrade.getLeg3();

        verifyCoinsInAndOutOfLegsAReCompatible(leg1, leg2, leg3);

        // Verify leg1 calculation
        final BigDecimal calculatedLeg1SwapRate = leg1.getSwapRate();
        Assert.assertEquals(leg1Direction, leg1.getPairTradeDirection());
        final BigDecimal expectedLeg1SwapRate = calculateExpectedSwapRate(leg1, quotes);
        Assert.assertEquals(calculatedLeg1SwapRate, expectedLeg1SwapRate);

        final BigDecimal expectedLeg1CoinOut = calculatedLeg1SwapRate.multiply(leg1.getAmountIn());
        Assert.assertEquals(expectedLeg1CoinOut, leg1.getAmountOut());

        //Verify Leg2 calculation
        final BigDecimal calculatedLeg2SwapRate = leg2.getSwapRate();
        Assert.assertEquals(leg2Direction, leg2.getPairTradeDirection());
        final BigDecimal expectedLeg2SwapRate = calculateExpectedSwapRate(leg2, quotes);
        Assert.assertEquals(calculatedLeg2SwapRate, expectedLeg2SwapRate);

        final BigDecimal expectedLeg2CoinOut = calculatedLeg2SwapRate.multiply(leg2.getAmountIn());
        Assert.assertEquals(expectedLeg2CoinOut, leg2.getAmountOut());

        //Verify Leg3 calculation
        final BigDecimal calculatedLeg3SwapRate = leg3.getSwapRate();
        Assert.assertEquals(leg3Direction, leg3.getPairTradeDirection());
        final BigDecimal expectedLeg3SwapRate = calculateExpectedSwapRate(leg3, quotes);
        Assert.assertEquals(calculatedLeg3SwapRate, expectedLeg3SwapRate);

        final BigDecimal expectedLeg3CoinOut = calculatedLeg3SwapRate.multiply(leg3.getAmountIn());
        Assert.assertEquals(expectedLeg3CoinOut, leg3.getAmountOut());

        // Verify surface rate profit calculation.
        final BigDecimal calculatedProfit = fullTriArbTrade.getProfit();
        final BigDecimal expectedProfit = leg3.getAmountOut().subtract(leg1.getAmountIn());
        Assert.assertEquals(expectedProfit, calculatedProfit);

        final BigDecimal expectedProfitPercent = calculatedProfit.divide(leg1.getAmountIn(), 7, RoundingMode.HALF_UP).multiply(new BigDecimal(100.00));
        Assert.assertEquals(expectedProfitPercent, fullTriArbTrade.getProfitPercent());
    }

    private BigDecimal calculateExpectedSwapRate(final TriArbTradeLeg leg, final Map<String, PairQuote> quotes) {
        final PairQuote quote = quotes.get(leg.getPair().getPair());

        if(leg.getPairTradeDirection() == BASE_TO_QUOTE) {
            return new BigDecimal(1).divide(quote.getAsk(), 7, RoundingMode.HALF_UP);
        } else if(leg.getPairTradeDirection() == QUOTE_TO_BASE) {
            return quote.getBid();
        } else {
            return null;
        }
    }
}
