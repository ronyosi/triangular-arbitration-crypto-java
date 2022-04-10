package com.webhopper.integrations.poloniex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class OrderBookDeserializer extends JsonDeserializer<OrderBook> {
    @Override
    public OrderBook deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);

        final OrderBook orderBook = new OrderBook();
        JsonNode asks = node.get("asks");
        final Iterator<JsonNode> asksIterator = asks.iterator();

        List<BookEntry> hydratedAsks = new ArrayList<>();
        while(asksIterator.hasNext()) {
            final JsonNode nextTuple = asksIterator.next();
            double price = nextTuple.get(0).asDouble();
            double quantity = nextTuple.get(1).asDouble();
            hydratedAsks.add(new BookEntry(new BigDecimal(price), new BigDecimal(quantity)));
        }

        orderBook.setAsks(hydratedAsks);


        JsonNode bids = node.get("bids");
        final Iterator<JsonNode> bidsIterator = bids.iterator();

        List<BookEntry> hydratedBids = new ArrayList<>();
        while(bidsIterator.hasNext()) {
            final JsonNode nextTuple = bidsIterator.next();
            double price = nextTuple.get(0).asDouble();
            double quantity = nextTuple.get(1).asDouble();
            hydratedBids.add(new BookEntry(new BigDecimal(price), new BigDecimal(quantity)));
        }

        orderBook.setBids(hydratedBids);

        return orderBook;
    }
}