package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.RequestBuilder;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NewUserHotRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String URL = "/recall/newuserhot";

    private int num = 200;

    public NewUserHotRequestBuilder() {
    }

    public NewUserHotRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("interest", retrieveKey.getKey());
        requestParams.put("ihf", retrieveKey.getIhf());
        requestParams.put("alias", retrieveKey.getAlias());
        requestParams.put("num", String.valueOf(num));
        requestParams.put("bucket", retrieveKey.getPlaceholder());
        requestParams.put("type", retrieveKey.getType());
        requestParams.put("ctype", "101,102,103,121");
        requestParams.put("cstage", "6,7,8,10");
        requestParams.put("minStar", "4");

        Optional<HttpRequest> httpRequestOpt = Optional.of(new HttpRequest(URL, requestParams));
        return CompletableFuture.completedFuture(httpRequestOpt);
    }
}
