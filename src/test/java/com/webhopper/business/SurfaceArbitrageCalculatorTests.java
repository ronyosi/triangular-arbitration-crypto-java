package com.webhopper.business;

import com.webhopper.entities.*;
import com.webhopper.integrations.poloniex.*;
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
public class SurfaceArbitrageCalculatorTests {
    @Mock
    private PoloniexApi poloniexApi;

    private PolonixService polonixService;
    private ExchangeMarketDataService exchangeMarketDataService;

    private StructureTriangles structureTriangles;

    @Before
    public void setup() throws IOException {
        polonixService = new PolonixService(poloniexApi);
        exchangeMarketDataService = new ExchangeMarketDataService(polonixService, null);
        structureTriangles = new StructureTriangles(exchangeMarketDataService);
    }

    @Test
    public void testProfitableSurfaceArbitrageCalculatedCorrectly() throws IOException {
        // 1: Create triangles.
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "poloniex__ticker_for_1_profitable_triangle.json");
        when(poloniexApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        List<Triangle> triangles = structureTriangles.structure(CryptoExchange.POLONIEX);

        final Map<String, Quote> quotes = polonixService.getPricingInfo();

        // 2: Calculate surface rate
        final SurfaceArbitrageCalculator arbitrageCalculator = new SurfaceArbitrageCalculator(polonixService);
        final List<TriArbTrade> candidates = arbitrageCalculator.calculateSurfaceArbitrage(triangles.get(0), quotes, new BigDecimal(500));
        Assert.assertEquals(2, candidates.size());// There should be only one triangle in that file loaded above.
        TriArbTrade fullTriArbTradeForward = candidates.get(0);
        TriArbTrade fullTriArbTradeReverse = candidates.get(1);

        verifyCalculations(quotes, fullTriArbTradeForward, BASE_TO_QUOTE, QUOTE_TO_BASE, BASE_TO_QUOTE);
        verifyCalculations(quotes, fullTriArbTradeReverse, QUOTE_TO_BASE, QUOTE_TO_BASE, BASE_TO_QUOTE);
    }

    @Test
    public void testUnprofitableSurfaceArbitrageCalculatedCorrectly() throws IOException {
        // 1: Create triangles.
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "poloniex__ticker_for_1_unprofitable_triangle.json");
        when(poloniexApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        List<Triangle> triangles = structureTriangles.structure(CryptoExchange.POLONIEX);

        final Map<String, Quote> quotes = polonixService.getPricingInfo();

        // 2: Calculate surface rate
        final SurfaceArbitrageCalculator arbitrageCalculator = new SurfaceArbitrageCalculator(polonixService);
        final List<TriArbTrade> candidates = arbitrageCalculator.calculateSurfaceArbitrage(triangles.get(0), quotes, new BigDecimal(500));
        Assert.assertEquals(2, candidates.size());// There should be only one triangle in that file loaded above.
        TriArbTrade fullTriArbTradeForward = candidates.get(0);
        TriArbTrade fullTriArbTradeReverse = candidates.get(1);

        verifyCalculations(quotes, fullTriArbTradeForward, BASE_TO_QUOTE, QUOTE_TO_BASE, BASE_TO_QUOTE);
        verifyCalculations(quotes, fullTriArbTradeReverse, QUOTE_TO_BASE, QUOTE_TO_BASE, BASE_TO_QUOTE);
    }

    private void verifyCoinsInAndOutOfLegsAReCompatible(TriArbTradeLeg leg1, TriArbTradeLeg leg2, TriArbTradeLeg leg3) {
        Assert.assertEquals(leg1.getCoinOut(), leg2.getCoinIn());
        Assert.assertEquals(leg2.getCoinOut(), leg3.getCoinIn());
        Assert.assertEquals(leg3.getCoinOut(), leg1.getCoinIn());
    }

    private void verifyCalculations(Map<String, Quote> quotes, TriArbTrade fullTriArbTrade,
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

        final BigDecimal expectedLeg1CoinOut = calculatedLeg1SwapRate.multiply(leg1.getSurfaceCalcAmountIn());
        Assert.assertEquals(expectedLeg1CoinOut, leg1.getSurfaceCalcAmountOut());

        //Verify Leg2 calculation
        final BigDecimal calculatedLeg2SwapRate = leg2.getSwapRate();
        Assert.assertEquals(leg2Direction, leg2.getPairTradeDirection());
        final BigDecimal expectedLeg2SwapRate = calculateExpectedSwapRate(leg2, quotes);
        Assert.assertEquals(calculatedLeg2SwapRate, expectedLeg2SwapRate);

        final BigDecimal expectedLeg2CoinOut = calculatedLeg2SwapRate.multiply(leg2.getSurfaceCalcAmountIn());
        Assert.assertEquals(expectedLeg2CoinOut, leg2.getSurfaceCalcAmountOut());

        //Verify Leg3 calculation
        final BigDecimal calculatedLeg3SwapRate = leg3.getSwapRate();
        Assert.assertEquals(leg3Direction, leg3.getPairTradeDirection());
        final BigDecimal expectedLeg3SwapRate = calculateExpectedSwapRate(leg3, quotes);
        Assert.assertEquals(calculatedLeg3SwapRate, expectedLeg3SwapRate);

        final BigDecimal expectedLeg3CoinOut = calculatedLeg3SwapRate.multiply(leg3.getSurfaceCalcAmountIn());
        Assert.assertEquals(expectedLeg3CoinOut, leg3.getSurfaceCalcAmountOut());

        // Verify surface rate profit calculation.
        final BigDecimal calculatedProfit = fullTriArbTrade.getSurfaceCalcProfit();
        final BigDecimal expectedProfit = leg3.getSurfaceCalcAmountOut().subtract(leg1.getSurfaceCalcAmountIn());
        Assert.assertEquals(expectedProfit, calculatedProfit);

        final BigDecimal expectedProfitPercent = calculatedProfit.divide(leg1.getSurfaceCalcAmountIn(), 7, RoundingMode.HALF_UP).multiply(new BigDecimal(100.00));
        Assert.assertEquals(expectedProfitPercent, fullTriArbTrade.getSurfaceCalcProfitPercent());
    }

    private BigDecimal calculateExpectedSwapRate(final TriArbTradeLeg leg, final Map<String, Quote> quotes) {
        final Quote quote = quotes.get(leg.getPair().getPair());

        if(leg.getPairTradeDirection() == BASE_TO_QUOTE) {
            return new BigDecimal(1).divide(((PoloniexQuote)quote).getAsk(), 7, RoundingMode.HALF_UP);
        } else if(leg.getPairTradeDirection() == QUOTE_TO_BASE) {
            return ((PoloniexQuote)quote).getBid();
        } else {
            return null;
        }
    }
}
