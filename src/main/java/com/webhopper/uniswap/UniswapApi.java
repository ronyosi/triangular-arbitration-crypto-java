package com.webhopper.uniswap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webhopper.utils.FileUtils;
import com.webhopper.utils.JsonFacade;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UniswapApi {
    public String getPricesFromFileOrApiCall(boolean disableCache) {
        String tickers = null;

        if(!disableCache) {
            try {
                tickers = FileUtils.openFile("uniswap_tickers.json");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (tickers != null) {
                return tickers;
            }
        }

        final String jsonString = httpGetPrices();
        FileUtils.writeFile(jsonString, "uniswap_tickers.json");
        return jsonString;
    }


    protected String httpGetPrices() {
        String tickers = null;

            var client = HttpClient.newHttpClient();

            HttpResponse<String> response = null;
            try {
                final String graphqlRequest = FileUtils.fileInResourceFolderToString(this.getClass().getClassLoader(), "uniswap-get-pairs.graphql");

//                json={'query': query}

                Map<String,String> jsonRequest = new HashMap<>();
                jsonRequest.put("query", graphqlRequest);

                final ObjectMapper objectMapper = JsonFacade.getObjectMapper();



                var request = HttpRequest.newBuilder(
                        URI.create("https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v3"))
                        .header("accept", "application/graphql")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(jsonRequest)))
//                        .POST(HttpRequest.BodyPublishers.ofFile(Path.of(ClassLoader.getSystemResource("uniswap-get-pairs.graphql").toURI())))
                        .build();


                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return response.body();
        }
    }
