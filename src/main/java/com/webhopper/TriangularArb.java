package com.webhopper;

import com.google.common.base.Stopwatch;

import java.util.List;

public class TriangularArb {

    public static void main(String[] args)  {
        StructureTriangles structureTriangles = new StructureTriangles();
        Stopwatch timer = Stopwatch.createUnstarted();

        timer.start();
        List<Triangle> triangles = structureTriangles.structure();
        Stopwatch stop = timer.stop();
        System.out.println(stop);

        ArbitrageCalculator arbitrageCalculator = new ArbitrageCalculator();
        arbitrageCalculator.calculateSurfaceArbitrage(triangles.get(0));

    }
}
