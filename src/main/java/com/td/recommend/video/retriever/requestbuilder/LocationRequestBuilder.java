package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.idgenerator.TraceIdGenerator;
import com.td.recommend.commons.request.Ihf;
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
 * Created by sunjian on 2021/09/16.
 */
public class LocationRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String REQUET_URI = "/recall/location";
    private static final Logger LOG = LoggerFactory.getLogger(LocationRequestBuilder.class);
    private int num = 100;

    public LocationRequestBuilder() {

    }

    public LocationRequestBuilder(Config config) {
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
        requestParams.put("traceId", TraceIdGenerator.generate());
        requestParams.put("type", retrieveKey.getType());
        if (retrieveKey.getIhf().equals(String.valueOf(Ihf.VSHOWDANCE_FEED.id()))) {
            requestParams.put("cstage", "6,7,8,9,10");
            requestParams.put("ctype", "103,105,107,109,501,502,503");
        } else {
            requestParams.put("cstage", "6,7,8,10");
            requestParams.put("ctype", "101,102,103,301,121");
        }
        HttpRequest httpRequest = new HttpRequest(REQUET_URI, requestParams);
        LOG.info("location recall requestParams: {}", requestParams);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
