package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.utils.RedisClientSingleton;


public class QualityRetrieveKeyBuilder implements RetrieveKeyBuilder {

    private VideoRecommenderContext recommendContext;

    public QualityRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }
    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String diu = recommendContext.getRecommendRequest().getDiu();
        RedisClientSingleton redis = RedisClientSingleton.search;
        if(recommendContext.hasBucket("quality-yes")) {
            if(redis.exists("fitnessdiu_"+diu)) {
                RetrieveKey retrievekey = new RetrieveKey();
                retrievekey.setType(RetrieverType.vquality.name());
                retrievekey.setAlias(RetrieverType.vquality.alias());
                retrievekey.setIhf(recommendContext.getRecommendRequest().getCid());
                retrievekey.setKey(RetrieverType.vquality.name());
                retrieveKeyContext.addRetrieveKey(retrievekey);
            }
        }
        if(redis.exists("classicaldiu_"+diu)) {
            RetrieveKey retrievekey = new RetrieveKey();
            retrievekey.setType(RetrieverType.vquality_classic.name());
            retrievekey.setAlias(RetrieverType.vquality_classic.alias());
            retrievekey.addAttribute("maxCnt",10);
            retrievekey.setKey(RetrieverType.vquality_classic.name());
            retrievekey.setIhf(recommendContext.getRecommendRequest().getCid());
            retrieveKeyContext.addRetrieveKey(retrievekey);
        }
    }

}
