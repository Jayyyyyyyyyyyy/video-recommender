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
 * Created by liujikun on 2019/07/20.
 */
public class HorseRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String REQUET_URI = "/recall/horse";
    private static final Logger LOG = LoggerFactory.getLogger(com.td.recommend.requestbuilder.InterestRequestBuilder.class);
    private int num = 200;

    public HorseRequestBuilder() {

    }

    public HorseRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("bucket", retrieveKey.getPlaceholder());
        requestParams.put("type", retrieveKey.getType());

        HttpRequest httpRequest = new HttpRequest(REQUET_URI, requestParams);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
