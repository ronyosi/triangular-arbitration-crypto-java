package com.webhopper;

import com.google.common.base.Stopwatch;
import com.webhopper.poloniex.PairQuote;
import com.webhopper.poloniex.PolonixApiFacade;

import java.util.List;
import java.util.Map;

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
            arbitrageCalculator.calculateSurfaceArbitrage(triangle, quotes);
        }
    }
}
