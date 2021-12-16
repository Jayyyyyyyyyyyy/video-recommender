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
 * Created by liujikun on 2019/6/19.
 */
public class TalentRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String REQUET_URI = "/recall/talent";
    private static final Logger LOG = LoggerFactory.getLogger(com.td.recommend.requestbuilder.InterestRequestBuilder.class);
    private int num = 100;

    public TalentRequestBuilder() {

    }

    public TalentRequestBuilder(Config config) {
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
        requestParams.put("cstage", "6,7,8,10");
        if (retrieveKey.getType().startsWith("sv")) {
            requestParams.put("ctype", "105,106,107");
        } else {
            requestParams.put("ctype", "101,102,103,121");
        }
        HttpRequest httpRequest = new HttpRequest(REQUET_URI, requestParams);
        LOG.info("recall requestParams: {}", requestParams);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
