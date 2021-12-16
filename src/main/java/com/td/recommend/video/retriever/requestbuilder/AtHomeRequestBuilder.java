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
public class AtHomeRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String URL = "/recall/athome";

    private int num = 100;

    public AtHomeRequestBuilder() {
    }

    public AtHomeRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        String traceId = TraceIdGenerator.generate();

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("traceid", traceId);
        requestMap.put("ihf", retrieveKey.getIhf());
        requestMap.put("key", retrieveKey.getKey());
        requestMap.put("type", retrieveKey.getType());
        requestMap.put("num", Integer.toString(num));

        Optional<HttpRequest> httpRequestOpt = Optional.of(new HttpRequest(URL, requestMap));
        return CompletableFuture.completedFuture(httpRequestOpt);
    }
}
