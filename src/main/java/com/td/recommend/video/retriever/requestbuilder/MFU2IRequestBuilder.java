package com.td.recommend.video.retriever.requestbuilder;

import com.typesafe.config.Config;
import com.td.recommend.commons.idgenerator.TraceIdGenerator;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.requestbuilder.RetrieveKeyHttpRequestBuilder;
import com.td.recommend.retriever.engine.request.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by admin on 2017/8/2.
 */
public class MFU2IRequestBuilder implements RetrieveKeyHttpRequestBuilder {
    private static final String REQUEST_URI = "/recall/user/cb";

    private int num = 50;

    public MFU2IRequestBuilder() {
    }

    public MFU2IRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        String uid = retrieveKey.getKey();
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("uid", uid);
        requestMap.put("traceId", TraceIdGenerator.generate());
        requestMap.put("num", String.valueOf(num));
        requestMap.put("media", "video");

        HttpRequest request = new HttpRequest(REQUEST_URI, requestMap);

        return CompletableFuture.completedFuture(Optional.of(request));
    }
}
