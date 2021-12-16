package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.RequestBuilder;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Liujikun on 2019/09/02.
 */
public class AuthorFreshRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String url = "/recall/author_fresh";
    private int num = 500;

    public AuthorFreshRequestBuilder() {
    }

    public AuthorFreshRequestBuilder(Config config) {
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
        requestParams.put("type", retrieveKey.getType());
        if (retrieveKey.getIhf().equals(String.valueOf(Ihf.VSHOWDANCE_RLVT.id()))){
            requestParams.put("ctype", "103,105,107,503");
        }
        else{
            requestParams.put("ctype", "101,102,103,121");
        }


        HttpRequest httpRequest = new HttpRequest(url, requestParams);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
