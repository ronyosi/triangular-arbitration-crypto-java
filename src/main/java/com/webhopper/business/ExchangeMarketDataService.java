package com.webhopper.business;

import com.webhopper.entities.CryptoExchange;
import com.webhopper.poloniex.OrderBook;
import com.webhopper.poloniex.PolonixService;
import com.webhopper.poloniex.Quote;
import com.webhopper.uniswap.UniswapService;

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
