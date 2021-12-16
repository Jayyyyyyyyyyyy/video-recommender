package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.idgenerator.TraceIdGenerator;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.HttpRequestBuilder;
import com.typesafe.config.Config;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * Created by admin on 2017/12/2.
 */
public class BertRequestBuilder implements HttpRequestBuilder<RetrieveKey> {
    private static final String REQUEST_URI = "/recall/bert";

    private int num = 10;
    private static final String version64 = "64";

    public BertRequestBuilder() {
    }

    public BertRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestMap = new TreeMap<>();
        String traceId = TraceIdGenerator.generate();
        requestMap.put("traceId", traceId);
        requestMap.put("key", retrieveKey.getKey());
        requestMap.put("type", retrieveKey.getType());
        requestMap.put("num", String.valueOf(this.num));
        requestMap.put("bucket", retrieveKey.getPlaceholder());

        return CompletableFuture.completedFuture(Optional.of(new HttpRequest(REQUEST_URI, requestMap)));
    }
}
