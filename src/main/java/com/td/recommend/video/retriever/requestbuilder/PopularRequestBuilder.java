package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.RequestBuilder;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * create by pansm at 2019/08/30
 */
public class PopularRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String URL = "/recall/popular";

    private int num = 50;

    public PopularRequestBuilder() {
    }

    public PopularRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("bucket", retrieveKey.getPlaceholder());
        requestMap.put("key", retrieveKey.getKey());
        requestMap.put("type", retrieveKey.getKey());
        requestMap.put("num", Integer.toString(num));

        Optional<HttpRequest> httpRequestOpt = Optional.of(new HttpRequest(URL, requestMap));
        return CompletableFuture.completedFuture(httpRequestOpt);
    }
}
