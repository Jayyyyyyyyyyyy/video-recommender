package com.td.recommend.video.retriever.requestbuilder;

import com.typesafe.config.Config;
import com.td.recommend.commons.idgenerator.TraceIdGenerator;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.RequestBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by fuliangliang on 2017/6/15.
 */
public class HeadPoolRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String URL = "/recall/headpool";

    private int num = 1000;

    public HeadPoolRequestBuilder() {
    }

    public HeadPoolRequestBuilder(Config config) {
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
        requestMap.put("key", retrieveKey.getKey());
        requestMap.put("type", retrieveKey.getType());

        Optional<HttpRequest> httpRequestOpt = Optional.of(new HttpRequest(URL, requestMap));
        return CompletableFuture.completedFuture(httpRequestOpt);
    }
}
