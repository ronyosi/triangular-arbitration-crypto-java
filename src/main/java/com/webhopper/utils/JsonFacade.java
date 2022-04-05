package com.webhopper.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.webhopper.poloniex.OrderBook;
import com.webhopper.poloniex.OrderBookDeserializer;

public class JsonFacade {
    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {

        if(objectMapper != null) {
            return objectMapper;
        }

        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OrderBook.class, new OrderBookDeserializer());
//        module.addSerializer(AutomationTaskSignal.class, new AutomationTaskSignalJsonSerializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }

}
