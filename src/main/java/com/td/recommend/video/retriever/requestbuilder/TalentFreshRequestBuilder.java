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
 * Created by liujikun on 2019/6/19.
 */
public class TalentFreshRequestBuilder implements RequestBuilder<RetrieveKey, HttpRequest> {
    private static final String REQUET_URI = "/recall/talent_fresh";
    private static final Logger LOG = LoggerFactory.getLogger(com.td.recommend.requestbuilder.InterestRequestBuilder.class);
    private int num = 100;

    public TalentFreshRequestBuilder() {

    }

    public TalentFreshRequestBuilder(Config config) {
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

        //社区相关推荐召回ttalentfresh_rlvt 的结果,其他都老逻辑
        if(retrieveKey.getIhf().equals(String.valueOf(Ihf.VSHOWDANCE_RLVT.id())) ){
            requestParams.put("cstage", "6,7,8,9,10");
            requestParams.put("ctype", "103,105,107,503");
        }
        //社区首页推荐召回ttalentfresh的结果,其他都老逻辑
        else if(retrieveKey.getIhf().equals(String.valueOf(Ihf.VSHOWDANCE_FEED.id()))){
            requestParams.put("cstage", "6,7,8,9,10");
            requestParams.put("ctype", "103,105,107,501,502,503");
        }
        else{
            requestParams.put("cstage", "6,7,8,10");
            if (retrieveKey.getType().startsWith("sv")) {
                requestParams.put("ctype", "105,106,107");
            } else {
                requestParams.put("ctype", "101,102,103,121");
            }
        }

        HttpRequest httpRequest = new HttpRequest(REQUET_URI, requestParams);
        LOG.info("recall requestParams: {}", requestParams);
        return CompletableFuture.completedFuture(Optional.of(httpRequest));
    }
}
