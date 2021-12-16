package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.RequestBuilder;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by sunjian on 2021/09/09.
 */
public class THotRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String REQUET_URI = "/recall/thot";
    private static final Logger LOG = LoggerFactory.getLogger(com.td.recommend.requestbuilder.InterestRequestBuilder.class);
    private int num = 100;

    public THotRequestBuilder() {

    }

    public THotRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("type", retrieveKey.getType());
        requestParams.put("interest", retrieveKey.getKey());
        requestParams.put("alias", retrieveKey.getAlias());
        requestParams.put("ihf", retrieveKey.getIhf());
        requestParams.put("num", String.valueOf(num));
        requestParams.put("bucket", retrieveKey.getPlaceholder());
        requestParams.put("cstage", "6,7,8,9,10");
        HttpRequest httpRequest = new HttpRequest(REQUET_URI, requestParams);
        LOG.info("recall requestParams: {}", requestParams);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
