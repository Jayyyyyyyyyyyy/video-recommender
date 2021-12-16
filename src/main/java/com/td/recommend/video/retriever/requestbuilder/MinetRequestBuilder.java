package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.HttpRequestBuilder;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by admin on 2017/12/2.
 */
public class MinetRequestBuilder implements HttpRequestBuilder<RetrieveKey> {
    private static final String REQUEST_URI = "/recall/minet";

    private int num = 50;

    public MinetRequestBuilder() {
    }

    public MinetRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }


    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("type", retrieveKey.getType());
        requestMap.put("key", retrieveKey.getKey());
        requestMap.put("bucket", retrieveKey.getPlaceholder());
        requestMap.put("num", String.valueOf(num));
        HttpRequest request = new HttpRequest(REQUEST_URI, requestMap);

        return CompletableFuture.completedFuture(Optional.of(request));
    }
}
