package com.td.recommend.video.retriever.requestbuilder;

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
 * Created by liujikun on 2019/07/20.
 */
public class ExploreVideoRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String REQUET_URI = "/recall/ev";
    private static final Logger LOG = LoggerFactory.getLogger(com.td.recommend.requestbuilder.InterestRequestBuilder.class);
    private int num = 200;

    public ExploreVideoRequestBuilder() {

    }

    public ExploreVideoRequestBuilder(Config config) {
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

        if(!retrieveKey.getIhf().equals(String.valueOf(Ihf.VSHOWDANCE_FEED.id())) &&
                !retrieveKey.getIhf().equals(String.valueOf(Ihf.VSHOWDANCE_RLVT.id()))){
            if (retrieveKey.getType().startsWith("sv")) {
                requestParams.put("ctype", "105,106,107");
            } else {
                requestParams.put("ctype", "101,102,103,301,121");
            }
            requestParams.put("cstage", "6,7,8,10");
        }

        HttpRequest httpRequest = new HttpRequest(REQUET_URI, requestParams);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
