package com.td.recommend.video.retriever.requestbuilder;

import com.typesafe.config.Config;
import com.td.recommend.commons.idgenerator.TraceIdGenerator;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.requestbuilder.RetrieveKeyHttpRequestBuilder;
import com.td.recommend.retriever.engine.request.HttpRequest;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class WhiteMediaRequestBuilder implements RetrieveKeyHttpRequestBuilder {
    private static final String REQUEST_URL = "/whiteMediaNews/getNews";
    private int num = 1000;

    public WhiteMediaRequestBuilder() {
    }

    public WhiteMediaRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestMap = new TreeMap<>();
        requestMap.put("traceId", TraceIdGenerator.generate());
        requestMap.put("num", String.valueOf(num));

        HttpRequest httpRequest = new HttpRequest(REQUEST_URL, requestMap);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
