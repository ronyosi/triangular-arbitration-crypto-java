package com.webhopper.business;

import com.webhopper.entities.*;
import com.webhopper.poloniex.PoloniexQuote;
import com.webhopper.poloniex.PoloniexApi;
import com.webhopper.poloniex.PolonixService;
import com.webhopper.poloniex.Quote;
import com.webhopper.utils.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DepthArbitrageCalculatorTests {
    @Mock
    private PoloniexApi poloniexApi;

    private PolonixService polonixService;

    private ExchangeMarketDataService exchangeMarketDataService;

    private StructureTriangles structureTriangles;

    private DepthArbitrageCalculator depthArbitrageCalculator;

    @Before
    public void setup() throws IOException {
        polonixService = new PolonixService(poloniexApi);
        exchangeMarketDataService = new ExchangeMarketDataService(polonixService, null);

        structureTriangles = new StructureTriangles(exchangeMarketDataService);
        depthArbitrageCalculator = new DepthArbitrageCalculator(polonixService);
    }

    @Test
    public void testProfitableSurfaceArbitrageCalculatedCorrectly() throws IOException {
        // 1: Create triangle.
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "ticker_for_1_unprofitable_triangle.json");
        when(poloniexApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        List<Triangle> triangles = structureTriangles.structure(CryptoExchange.POLONIEX);
        final Triangle triangle = triangles.get(0);

        final Map<String, Quote> quotes = polonixService.getPricingInfo();

        // 2: Calculate surface rate
        final SurfaceArbitrageCalculator arbitrageCalculator = new SurfaceArbitrageCalculator(polonixService);
        final List<TriArbTrade> candidates = arbitrageCalculator.calculateSurfaceArbitrage(triangles.get(0), quotes, new BigDecimal(500));
        Assert.assertEquals(2, candidates.size());// There should be only one triangle in that file loaded above.
        TriArbTrade fullTriArbTradeForward = candidates.get(0);
        TriArbTrade fullTriArbTradeReverse = candidates.get(1);

        mockOrderBookCallForPair(triangle.getA().getPair());
        mockOrderBookCallForPair(triangle.getB().getPair());
        mockOrderBookCallForPair(triangle.getC().getPair());

        TriArbTrade triArbTradeForward = depthArbitrageCalculator.calculateDepthArbitrage(fullTriArbTradeForward);
        Assert.assertEquals(DepthCalcState.NOT_ENOUGH_BOOK_DEPTH, triArbTradeForward.getDepthCalcState());

        TriArbTrade triArbTradeReverse = depthArbitrageCalculator.calculateDepthArbitrage(fullTriArbTradeReverse);
        Assert.assertEquals(DepthCalcState.SUCCESSFULLY_CALCULATED, triArbTradeReverse.getDepthCalcState());

        TriArbTradeLeg leg1 = triArbTradeReverse.getLeg1();
        TriArbTradeLeg leg2 = triArbTradeReverse.getLeg2();
        TriArbTradeLeg leg3 = triArbTradeReverse.getLeg3();
        Assert.assertEquals(500.0,leg1.getDepthCalcAmountIn().doubleValue(), 0.0001);
        Assert.assertEquals(0.016935,leg1.getDepthCalcAmountOut().doubleValue(), 0.0001);
        Assert.assertEquals(0.016935,leg2.getDepthCalcAmountIn().doubleValue(), 0.0001);
        Assert.assertEquals(732.076578722536,leg2.getDepthCalcAmountOut().doubleValue(), 0.0001);
        Assert.assertEquals(732.076578722536,leg3.getDepthCalcAmountIn().doubleValue(), 0.0001);
        Assert.assertEquals(498.5597522477581,leg3.getDepthCalcAmountOut().doubleValue(), 0.0001);

        Assert.assertEquals(-1.4402, triArbTradeReverse.getDepthCalcProfit().doubleValue(), .0001);
        Assert.assertEquals(-0.2880500, triArbTradeReverse.getDepthCalcProfitPercent().doubleValue(), .0001);
    }

    private void mockOrderBookCallForPair(String pairName) throws IOException {
        final String fileName = pairName + "_order_book.json";
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), fileName);
        when(poloniexApi.httpGetOrderBookForPair(eq(pairName))).thenReturn(json);
    }
}
