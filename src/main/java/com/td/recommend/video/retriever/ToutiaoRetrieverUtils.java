package com.td.recommend.video.retriever;

import com.td.recommend.commons.app.AppId;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.abtest.BucketConstants;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.List;

/**
 * Created by Frang on 2018/7/25.
 */
public class ToutiaoRetrieverUtils {
    public static final String TOUTIAO_PREFIX = "toutiao@";

    public static void addKeyToutiaoPrefixIfNessary(VideoRecommenderContext recommenderContext, List<RetrieveKey> retrieveKeys) {
        RecommendRequest recommendRequest = recommenderContext.getRecommendRequest();
        String appId = recommendRequest.getAppId();
        boolean toutaio = AppId.isToutiao(appId);

        if (toutaio && recommenderContext.hasBucket(BucketConstants.PUBLISH_FIRSH_EXP)) {
            ToutiaoRetrieverUtils.addKeyTouTiaoPrefix(retrieveKeys);
        }
    }

    private static void addKeyTouTiaoPrefix(List<RetrieveKey> retrieveKeyList) {
        for (RetrieveKey retrieveKey : retrieveKeyList) {
            String key = retrieveKey.getKey();
            retrieveKey.setKey(TOUTIAO_PREFIX + key);
        }
    }
}
