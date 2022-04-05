package com.webhopper;

import com.google.common.base.Stopwatch;
import com.webhopper.business.ArbitrageCalculator;
import com.webhopper.business.StructureTriangles;
import com.webhopper.entities.FullTriArbTrade;
import com.webhopper.entities.Triangle;
import com.webhopper.poloniex.PairQuote;
import com.webhopper.poloniex.PolonixApiFacade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.webhopper.business.ArbitrageCalculator.logSurfaceRateInfo;

public class TriangularArb {

    public static void main(String[] args)  {
        StructureTriangles structureTriangles = new StructureTriangles();
        Stopwatch timer = Stopwatch.createUnstarted();

        timer.start();
        List<Triangle> triangles = structureTriangles.structure();
        Stopwatch stop = timer.stop();
        System.out.println(stop);

        ArbitrageCalculator arbitrageCalculator = new ArbitrageCalculator();
        final Map<String, PairQuote> quotes = PolonixApiFacade.getPrices(true);
        for(Triangle triangle : triangles) {
            final List<FullTriArbTrade> candidates = arbitrageCalculator.calculateSurfaceArbitrage(triangle, quotes, new BigDecimal(500), new BigDecimal(0));
            for(FullTriArbTrade candate : candidates) {
                logSurfaceRateInfo(candate);
                Map<String, Object> stringObjectMap = arbitrageCalculator.calculateDepthArbitrage(candate);
                System.out.println(stringObjectMap);

            }
        }
    }
}
