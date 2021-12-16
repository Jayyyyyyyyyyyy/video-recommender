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

public class InterestEnRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String REQUET_URI = "/recall/rlvt_ien";
    private static final Logger LOG = LoggerFactory.getLogger(com.td.recommend.requestbuilder.InterestRequestBuilder.class);
    private int num = 200;

    public InterestEnRequestBuilder() {

    }

    public InterestEnRequestBuilder(Config config) {
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
        requestParams.put("cstage", "6,7,8,10");
        requestParams.put("ctype", "101,102,103,301,121");
        if (retrieveKey.getType().equals("vneckvertebra_ien_rlvt")) {
            requestParams.put("secondcat","1009,1053");
        }
        requestParams.put("bucket", retrieveKey.getPlaceholder());

        HttpRequest httpRequest = new HttpRequest(REQUET_URI, requestParams);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
