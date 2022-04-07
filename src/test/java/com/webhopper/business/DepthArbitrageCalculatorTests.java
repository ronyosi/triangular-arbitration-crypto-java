package com.webhopper.business;

import com.webhopper.entities.TriArbTrade;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DepthArbitrageCalculatorTests {
    @Mock
    private PoloniexApi poloniexApi;

    private PolonixService polonixService;

    private StructureTriangles structureTriangles;

    private DepthArbitrageCalculator depthArbitrageCalculator;

    @Before
    public void setup() throws IOException {
        polonixService = new PolonixService(poloniexApi);
        structureTriangles = new StructureTriangles(polonixService);
        depthArbitrageCalculator = new DepthArbitrageCalculator(polonixService);
    }

    @Test
    public void testProfitableSurfaceArbitrageCalculatedCorrectly() throws IOException {
        // 1: Create triangle.
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "ticker_for_1_unprofitable_triangle.json");
        when(poloniexApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        List<Triangle> triangles = structureTriangles.structure();
        final Triangle triangle = triangles.get(0);

        final Map<String, PairQuote> quotes = polonixService.getPricingInfo();

        // 2: Calculate surface rate
        final SurfaceArbitrageCalculator arbitrageCalculator = new SurfaceArbitrageCalculator(polonixService);
        final List<TriArbTrade> candidates = arbitrageCalculator.calculateSurfaceArbitrage(triangles.get(0), quotes, new BigDecimal(500));
        Assert.assertEquals(2, candidates.size());// There should be only one triangle in that file loaded above.
        TriArbTrade fullTriArbTradeForward = candidates.get(0);
        TriArbTrade fullTriArbTradeReverse = candidates.get(1);

        mockOrderBookCallForPair(triangle.getA().getPair());
        mockOrderBookCallForPair(triangle.getB().getPair());
        mockOrderBookCallForPair(triangle.getC().getPair());

        //todo: try to refactor depthArbitrageCalculator to not need the surfaceArbitrage result object.
        Map<String, Object> stringObjectMap = depthArbitrageCalculator.calculateDepthArbitrage(fullTriArbTradeForward);

    }

    private void mockOrderBookCallForPair(String pairName) throws IOException {
        final String fileName = pairName + "_order_book.json";
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), fileName);
        when(poloniexApi.httpGetOrderBookForPair(eq(pairName))).thenReturn(json);
    }
}
