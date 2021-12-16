package com.td.recommend.video.retriever.requestbuilder;

import com.typesafe.config.Config;
import com.td.recommend.commons.idgenerator.TraceIdGenerator;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.td.recommend.retriever.engine.requestbuilder.HttpRequestBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * http://10.10.109.53:159/s?tar=video_dnn&qw=v_sjI5wWX0VavqBBXsFkRh60sB9Wg.&num=5
 * Created by admin on 2017/12/16.
 */
public class DNNI2IRequestBuilder implements HttpRequestBuilder<RetrieveKey> {
    private final static String REQUEST_URI = "/s";

    private int num = 5;

    public DNNI2IRequestBuilder() {
    }

    public DNNI2IRequestBuilder(Config config) {
        if (config.hasPath("num")) {
            this.num = config.getInt("num");
        }
    }

    @Override
    public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
        Map<String, String> requestMap = new TreeMap<>();
        String traceId = TraceIdGenerator.generate();
        requestMap.put("traceId", traceId);
        String key = retrieveKey.getKey();
        requestMap.put("qw", key);
        requestMap.put("num", Integer.toString(num));

        requestMap.put("tar", "video_dnn");

        HttpRequest httpRequest = new HttpRequest(REQUEST_URI, requestMap);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
