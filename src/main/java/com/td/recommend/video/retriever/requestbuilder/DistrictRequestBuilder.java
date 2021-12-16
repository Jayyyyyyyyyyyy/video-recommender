package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.idgenerator.TraceIdGenerator;
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
 * Created by liujikun on 2019/6/19.
 */
public class DistrictRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String REQUET_URI = "/recall/district";
    private static final Logger LOG = LoggerFactory.getLogger(DistrictRequestBuilder.class);
    private int num = 100;

    public DistrictRequestBuilder() {

    }

    public DistrictRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("key", retrieveKey.getKey());
        requestParams.put("type", retrieveKey.getType());
        requestParams.put("ihf", retrieveKey.getIhf());
        requestParams.put("num", String.valueOf(num));
        requestParams.put("traceId", TraceIdGenerator.generate());
        HttpRequest httpRequest = new HttpRequest(REQUET_URI, requestParams);
        LOG.info("district recall requestParams: {}", requestParams);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
