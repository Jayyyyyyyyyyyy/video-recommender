package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.idgenerator.TraceIdGenerator;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.RequestBuilder;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QualityRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String URL = "/recall/quality";

    private int num = 200;

    public QualityRequestBuilder() {
    }

    public QualityRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }
    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestMap = new HashMap<>();
        String traceId = TraceIdGenerator.generate();
        requestMap.put("key", retrieveKey.getKey());
        requestMap.put("type", retrieveKey.getType());
        requestMap.put("num", Integer.toString(num));
        requestMap.put("traceid", traceId);
        Optional<HttpRequest> httpRequestOpt = Optional.of(new HttpRequest(URL, requestMap));
        return CompletableFuture.completedFuture(httpRequestOpt);
    }
}
