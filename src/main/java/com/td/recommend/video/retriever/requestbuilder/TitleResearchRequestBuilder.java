package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.requestbuilder.RetrieveKeyHttpRequestBuilder;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by admin on 2017/8/2.
 */
public class TitleResearchRequestBuilder implements RetrieveKeyHttpRequestBuilder {
    private static final String REQUEST_URI = "/recall/titleresearch";

    private int num = 20;

    public TitleResearchRequestBuilder() {
    }

    public TitleResearchRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("bucket", retrieveKey.getPlaceholder());
        requestMap.put("num", String.valueOf(num));
        requestMap.put("type", retrieveKey.getType());
        requestMap.put("key", retrieveKey.getKey());

        HttpRequest request = new HttpRequest(REQUEST_URI, requestMap);

        return CompletableFuture.completedFuture(Optional.of(request));


    }
}
