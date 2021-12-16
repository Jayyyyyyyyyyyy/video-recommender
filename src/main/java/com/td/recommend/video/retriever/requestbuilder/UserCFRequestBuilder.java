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
public class UserCFRequestBuilder implements RetrieveKeyHttpRequestBuilder {
    private static final String REQUEST_URI = "/recall/usercf";
    private static final String REQUEST_URIV3 = "/recall/usercfv3";

    private int num = 100;

    public UserCFRequestBuilder() {
    }

    public UserCFRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("key", retrieveKey.getKey());
        requestMap.put("type", retrieveKey.getType());
        requestMap.put("bucket", retrieveKey.getPlaceholder());
        requestMap.put("num", String.valueOf(num));
        Optional<Object> opt = retrieveKey.getAttribute("appid");
        if (opt.isPresent()) {
            String appid = (String)opt.get();
            requestMap.put("appid", appid);
        }

        HttpRequest request;
        if (retrieveKey.getType().equals("vusercfv3")) {
            request = new HttpRequest(REQUEST_URIV3, requestMap);
        } else{
            request = new HttpRequest(REQUEST_URI, requestMap);
        }
        return CompletableFuture.completedFuture(Optional.of(request));
    }
}
