package com.webhopper.poloniex;


import com.webhopper.business.StructureTriangles;
import com.webhopper.entities.Triangle;
import com.webhopper.utils.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StructureTrianglesTests {
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
    public void testCorrectTrianglesFound() throws IOException {
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "tickers_for_3_triangles.json");
        when(poloniexApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        List<Triangle> triangles = structureTriangles.structure();

        assertEquals(3, triangles.size());

        checkThatNoDuplicateTrianglesCreated(triangles);

        for(Triangle triangle : triangles) {
            checkThatTriangleIsCorrectlyFormed(triangle);
        }

    }

    /**
     * Arbitrage triangles must follow some rules
     * 1) Only 3 coin symbols.
     * 2) Only 3 pairs
     * 3) Each coin only appears 2 times in the triangle (a coin appears in 2 of the pairs).
     * @param triangle
     */
    private void checkThatTriangleIsCorrectlyFormed(Triangle triangle) {
        final Map<String, Integer> countPerCoin = new HashMap<>();

        List<String> coins = new ArrayList<>();
        coins.add(triangle.getBaseA());
        coins.add(triangle.getBaseB());
        coins.add(triangle.getBaseC());
        coins.add(triangle.getQuoteA());
        coins.add(triangle.getQuoteB());
        coins.add(triangle.getQuoteC());

        for(String coin : coins) {
            Integer countForSingleCoin = countPerCoin.get(coin);
            if(countForSingleCoin == null) {
                countForSingleCoin = 1;
            } else {
                countForSingleCoin++;
            }
            countPerCoin.put(coin, countForSingleCoin);
        }

        // There should only be 3 coins in the triangle (bcs each trading pair has 2 coins in it)
        assertEquals("There should only be 3 coins in the triangle (bcs each trading pair has 2 coins in it)",
                3, countPerCoin.size());

        for(String coin : countPerCoin.keySet()) {
            int count = countPerCoin.get(coin);
            assertEquals("There should only be 2 of each of the coins.", 2, count);
        }

        //todo: check that a triangle is correct in that it can be used to create the forward and reverse leg1,leg2,leg3
    }

    private void checkThatNoDuplicateTrianglesCreated(final List<Triangle> triangles) {
        List<String> pairsInTriangle1 = new ArrayList<>();
        Triangle triangle1 = triangles.get(0);
        pairsInTriangle1.add(triangle1.getA().getPair());
        pairsInTriangle1.add(triangle1.getB().getPair());
        pairsInTriangle1.add(triangle1.getC().getPair());

        List<String> pairsInTriangle2 = new ArrayList<>();
        Triangle triangle2 = triangles.get(1);
        pairsInTriangle2.add(triangle2.getA().getPair());
        pairsInTriangle2.add(triangle2.getB().getPair());
        pairsInTriangle2.add(triangle2.getC().getPair());

        List<String> pairsInTriangle3 = new ArrayList<>();
        Triangle triangle3 = triangles.get(2);
        pairsInTriangle3.add(triangle3.getA().getPair());
        pairsInTriangle3.add(triangle3.getB().getPair());
        pairsInTriangle3.add(triangle3.getC().getPair());

        assertThat(pairsInTriangle1, not(containsInAnyOrder(pairsInTriangle2.toArray())));
        assertThat(pairsInTriangle1, not(containsInAnyOrder(pairsInTriangle2.toArray())));
        assertThat(pairsInTriangle1, not(containsInAnyOrder(pairsInTriangle3.toArray())));
        assertThat(pairsInTriangle2, not(containsInAnyOrder(pairsInTriangle3.toArray())));
    }
}
