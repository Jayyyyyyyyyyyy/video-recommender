package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.HttpRequestBuilder;
import com.typesafe.config.Config;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class Item2VecRequestBuilder implements HttpRequestBuilder<RetrieveKey> {
    private static final String REQUEST_URI = "/recall/item2vec";

    private int num = 10;

    public Item2VecRequestBuilder() {
    }

    public Item2VecRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestMap = new TreeMap<>();
        requestMap.put("bucket", retrieveKey.getPlaceholder());
        requestMap.put("num", String.valueOf(this.num));
        requestMap.put("type", retrieveKey.getType());
        requestMap.put("key", retrieveKey.getKey());

        return CompletableFuture.completedFuture(Optional.of(new HttpRequest(REQUEST_URI, requestMap)));
    }
}
