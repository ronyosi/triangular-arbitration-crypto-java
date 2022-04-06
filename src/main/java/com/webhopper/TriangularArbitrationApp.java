package com.webhopper;

import com.google.common.base.Stopwatch;
import com.webhopper.business.ArbitrageCalculator;
import com.webhopper.business.StructureTriangles;
import com.webhopper.entities.FullTriArbTrade;
import com.webhopper.entities.Triangle;
import com.webhopper.poloniex.PairQuote;
import com.webhopper.poloniex.PoloniexApi;
import com.webhopper.poloniex.PolonixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.webhopper.business.ArbitrageCalculator.logSurfaceRateInfo;

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
        final List<Triangle> triangles = structureTriangles.structure();
        Stopwatch stop = timer.stop();
        logger.info("Structuring triangles took {}", stop);
        return triangles;
    }

    public static void main(String[] args)  {
        PoloniexApi poloniexApi = new PoloniexApi();
        PolonixService polonixService = new PolonixService(poloniexApi);
        StructureTriangles structureTriangles = new StructureTriangles(polonixService);
        TriangularArbitrationApp arbitrationApp = new TriangularArbitrationApp(structureTriangles, polonixService);
        List<Triangle> triangles = arbitrationApp.structureTriangles();
        arbitrationApp.findArbitrageFromTriangles(triangles);
    }

    private void findArbitrageFromTriangles(List<Triangle> triangles) {
        final double percentProfitExpected = 0.5;

        final ArbitrageCalculator arbitrageCalculator = new ArbitrageCalculator(polonixService);
        final Map<String, PairQuote> quotes = polonixService.getPricingInfo();
        for(Triangle triangle : triangles) {
            final List<FullTriArbTrade> surfaceRateCalculations = arbitrageCalculator.calculateSurfaceArbitrage(triangle, quotes, new BigDecimal(500));

            final List<FullTriArbTrade> profitableSurfaceRates = surfaceRateCalculations.stream().filter(t -> t.getProfitPercent().doubleValue() > percentProfitExpected).collect(Collectors.toList());

            for(FullTriArbTrade candidate : profitableSurfaceRates) {
                logSurfaceRateInfo(candidate);
                Map<String, Object> stringObjectMap = arbitrageCalculator.calculateDepthArbitrage(candidate);
                System.out.println(stringObjectMap);

            }
        }
    }
}
