package com.webhopper.integrations;

import com.webhopper.entities.CryptoExchange;
import com.webhopper.integrations.poloniex.OrderBook;
import com.webhopper.integrations.poloniex.PolonixService;
import com.webhopper.integrations.poloniex.Quote;
import com.webhopper.integrations.uniswap.UniswapService;

import java.util.Map;

public class ExchangeMarketDataService {
    private final PolonixService polonixService;
    private final UniswapService uniswapService;

    public ExchangeMarketDataService(PolonixService polonixService, UniswapService uniswapService) {
        this.polonixService = polonixService;
        this.uniswapService = uniswapService;
    }

    public Map<String, Quote> getPricingInfo(CryptoExchange cryptoExchange) {
        if(cryptoExchange == CryptoExchange.POLONIEX) {
            return polonixService.getPricingInfo();
        } else if(cryptoExchange == CryptoExchange.UNISWAP) {
            return uniswapService.getPricingInfo();
        }

        return null;
    }

    public OrderBook getBookForPair(String pair) {
        return polonixService.getBookForPair(pair);
    }

}
