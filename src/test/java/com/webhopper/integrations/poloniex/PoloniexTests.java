package com.webhopper.integrations.poloniex;


import com.webhopper.business.ExchangeMarketDataService;
import com.webhopper.business.StructureTriangles;
import com.webhopper.integrations.uniswap.UniswapApi;
import com.webhopper.integrations.uniswap.UniswapService;
import com.webhopper.utils.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
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

    @Mock
    private UniswapApi uniswapApi;

    private PolonixService polonixService;
    private UniswapService uniswapService;
    private ExchangeMarketDataService exchangeMarketDataService;

    private StructureTriangles structureTriangles;

    @Before
    public void setup() throws IOException {
        polonixService = new PolonixService(poloniexApi);
        uniswapService = new UniswapService(uniswapApi);
        exchangeMarketDataService = new ExchangeMarketDataService(polonixService, uniswapService);
        structureTriangles = new StructureTriangles(exchangeMarketDataService);
    }

    @Test
    public void testPoloniexJsonMappingReturnsCorrectKeys() throws IOException {
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "poloniex__tickers_for_3_triangles.json");
        when(poloniexApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        final Map<String, Quote> pricingInfo = polonixService.getPricingInfo();
        assertThat(pricingInfo.keySet(), containsInAnyOrder("USDT_TUSD", "USDC_USDT", "USDC_TUSD", "BTC_MATIC", "USDT_MATIC", "USDT_BTC", "USDC_LTC", "USDT_LTC"));
    }

    @Test
    public void testUniswapJsonMappingReturnsCorrectKeys() throws IOException {
        final String json = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "uniswap__tickers_for_3_triangles.json");
        when(uniswapApi.getPricesFromFileOrApiCall(anyBoolean())).thenReturn(json);
        final Map<String, Quote> pricingInfo = uniswapService.getPricingInfo();
        assertThat(pricingInfo.keySet(), containsInAnyOrder("UNI_RNG", "WBTC_USDC", "ICHI_CEL", "ICHI_USDC", "RNG_WETH", "USDC_CEL", "WBTC_ICHI", "UNI_WETH"));
    }
}
