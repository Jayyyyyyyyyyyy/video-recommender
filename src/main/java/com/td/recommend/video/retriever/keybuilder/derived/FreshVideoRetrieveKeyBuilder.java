package com.td.recommend.video.retriever.keybuilder.derived;

import com.td.recommend.commons.app.AppId;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.retriever.KeyTag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.ToutiaoRetrieverUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Frang on 2018/7/23.
 */
public class FreshVideoRetrieveKeyBuilder implements UserDerivedRetrieveKeyBuilder {
    private static Set<String> interestRetrieveTypes = new HashSet<>();
    private VideoRecommenderContext recommenderContext;

    static {
//        interestRetrieveTypes.add(RetrieverType.cat.name());
        interestRetrieveTypes.add(RetrieverType.vsubcat.name());
        interestRetrieveTypes.add(RetrieverType.vtag.name());
//        interestRetrieveTypes.add(RetrieverType.topic.name());
//        interestRetrieveTypes.add(RetrieverType.topic_64.name());
    }

    public FreshVideoRetrieveKeyBuilder(VideoRecommenderContext recommenderContext) {
        this.recommenderContext = recommenderContext;
    }

    @Override
    public Set<RetrieveKey> build(Set<RetrieveKey> retrieveKeys) {
        if (retrieveKeys.size() > 100) {//控制总共召回key的数量
            return Collections.emptySet();
        }

        Set<RetrieveKey> resultKeys = new HashSet<>();

        RecommendRequest recommendRequest = recommenderContext.getRecommendRequest();
        String appId = recommendRequest.getAppId();

        boolean toutiao = AppId.isToutiao(appId);

        for (RetrieveKey retrieveKey : retrieveKeys) {
            if (retrieveKey.getTags().contains(KeyTag.weak.name())) {
                continue;
            }

            if (interestRetrieveTypes.contains(retrieveKey.getType())) {
                String key = retrieveKey.getKey();
                if (toutiao) {
                    key = ToutiaoRetrieverUtils.TOUTIAO_PREFIX + key;
                }

                RetrieveKey derivedKey = DerivedKeyUtils.createDerivedKey(retrieveKey, retrieveKey.getType(), key);
                derivedKey.setCategory("fresh");

                retrieveKeys.add(derivedKey);
            }

        }

        return resultKeys;
    }
}
