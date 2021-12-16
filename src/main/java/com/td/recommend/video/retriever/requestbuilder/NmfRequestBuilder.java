package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.HttpRequestBuilder;
import com.typesafe.config.Config;

import javax.swing.text.html.Option;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by admin on 2017/12/2.
 */
public class NmfRequestBuilder implements HttpRequestBuilder<RetrieveKey> {
    private static final String REQUEST_URI = "/recall/nmfv2";
    private static final String REQUEST_URIV3 = "/recall/nmfv3";

    private int num = 50;

    public NmfRequestBuilder() {
    }

    public NmfRequestBuilder(Config config) {
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
        if (retrieveKey.getType().equals("vnmfv3")) {
            request = new HttpRequest(REQUEST_URIV3, requestMap);
        } else {
            request = new HttpRequest(REQUEST_URI, requestMap);
        }
        return CompletableFuture.completedFuture(Optional.of(request));
    }
}
