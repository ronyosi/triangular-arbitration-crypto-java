package com.webhopper;

import com.google.common.base.Stopwatch;
import com.webhopper.business.DepthArbitrageCalculator;
import com.webhopper.business.ExchangeMarketDataService;
import com.webhopper.business.SurfaceArbitrageCalculator;
import com.webhopper.business.StructureTriangles;
import com.webhopper.entities.CryptoExchange;
import com.webhopper.entities.DepthCalcState;
import com.webhopper.entities.TriArbTrade;
import com.webhopper.entities.Triangle;
import com.webhopper.poloniex.PoloniexQuote;
import com.webhopper.poloniex.PoloniexApi;
import com.webhopper.poloniex.PolonixService;
import com.webhopper.poloniex.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.webhopper.business.SurfaceArbitrageCalculator.logSurfaceRateInfo;

public class TriangularArbitrationApp {
    private static final Logger logger = LoggerFactory.getLogger(TriangularArbitrationApp.class);

    private final StructureTriangles structureTriangles;
    private final PolonixService polonixService;

    public TriangularArbitrationApp(
            final StructureTriangles structureTriangles,
            final PolonixService polonixService) {
        this.structureTriangles = structureTriangles;
        this.polonixService = polonixService;
    }

    public List<Triangle> structureTriangles() {
        Stopwatch timer = Stopwatch.createUnstarted();
        timer.start();
        final List<Triangle> triangles = structureTriangles.structure(CryptoExchange.POLONIEX);
        Stopwatch stop = timer.stop();
        logger.info("Structuring triangles took {}", stop);
        return triangles;
    }

    public static void main(String[] args)  {
        PoloniexApi poloniexApi = new PoloniexApi();
        PolonixService polonixService = new PolonixService(poloniexApi);
        ExchangeMarketDataService exchangeMarketDataService = new ExchangeMarketDataService(polonixService, null);
        StructureTriangles structureTriangles = new StructureTriangles(exchangeMarketDataService);
        TriangularArbitrationApp arbitrationApp = new TriangularArbitrationApp(structureTriangles, polonixService);
        List<Triangle> triangles = arbitrationApp.structureTriangles();
        arbitrationApp.findArbitrageFromTriangles(triangles);
    }

    private void findArbitrageFromTriangles(List<Triangle> triangles) {
        final double percentProfitExpected = 0;

        final SurfaceArbitrageCalculator arbitrageCalculator = new SurfaceArbitrageCalculator(polonixService);
        final DepthArbitrageCalculator realArbitrageCalculator = new DepthArbitrageCalculator(polonixService);
        final Map<String, Quote> quotes = polonixService.getPricingInfo();
        List<TriArbTrade> profitableTrianglesByRealRate = new ArrayList<>();
        List<TriArbTrade> profitableTrianglesBySurfaceRate = new ArrayList<>();

        for(Triangle triangle : triangles) {
            final List<TriArbTrade> surfaceRateCalculations = arbitrageCalculator.calculateSurfaceArbitrage(triangle, quotes, new BigDecimal(500));

            final List<TriArbTrade> candidates = surfaceRateCalculations.stream().filter(t -> t.getSurfaceCalcProfitPercent().doubleValue() > percentProfitExpected).collect(Collectors.toList());
            profitableTrianglesBySurfaceRate.addAll(candidates); // For reporting after all the logic runs.

            for(TriArbTrade candidate : candidates) {
                logSurfaceRateInfo(candidate);
                TriArbTrade triArbTrade = realArbitrageCalculator.calculateDepthArbitrage(candidate);
               logger.info(triArbTrade.prettyPrintTradeSummary());

                if(triArbTrade.getDepthCalcState() == DepthCalcState.SUCCESSFULLY_CALCULATED
                        && triArbTrade.getDepthCalcProfit().doubleValue() > 0) {
                    profitableTrianglesByRealRate.add(triArbTrade);
                }
            }
        }

        logger.info("Found {} triangles profitable by surface rate.", profitableTrianglesBySurfaceRate.size());
        logger.info("Found {} triangles profitable by real rate.", profitableTrianglesByRealRate.size());
    }
}
