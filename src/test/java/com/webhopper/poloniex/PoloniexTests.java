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
public class PoloniexTests {
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
    public void testJsonMappingReturnsCorrectKeys() throws IOException {
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "tickers_for_3_triangles.json");
        when(poloniexApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        final Map<String, PairQuote> pricingInfo = polonixService.getPricingInfo();
        assertThat(pricingInfo.keySet(), containsInAnyOrder("USDT_TUSD", "USDC_USDT", "USDC_TUSD", "BTC_MATIC", "USDT_MATIC", "USDT_BTC", "USDC_LTC", "USDT_LTC"));
    }
}
