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

/**
 * Created by liujikun on 2019/6/15.
 */
public class TeachingRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String techingURL = "/recall/teaching";
    private int num = 500;

    public TeachingRequestBuilder() {
    }

    public TeachingRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        String traceId = TraceIdGenerator.generate();

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("traceid", traceId);
        requestMap.put("num", Integer.toString(num));
        requestMap.put("type", retrieveKey.getType());
        requestMap.put("key", retrieveKey.getKey());

        Optional<HttpRequest> httpRequestOpt = Optional.of(new HttpRequest(techingURL, requestMap));
        return CompletableFuture.completedFuture(httpRequestOpt);
    }
}
