package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

public class ExtRelevantUserCFRetrieveKeyBuilder implements RetrieveKeyBuilder {

    private VideoRecommenderContext recommendContext;

    public ExtRelevantUserCFRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }
    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String diu = userItem.getId().replaceFirst("debug_", "");

        String retrieveKeyName = RetrieverType.vusercfv3.name();
        String retrieveKeyAlias =  RetrieverType.vusercfv3.alias();
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(retrieveKeyName)
                .setAlias(retrieveKeyAlias)
                .addAttribute("maxCnt", 30)
                .addAttribute("appid", recommendContext.getRecommendRequest().getAppId())
                .setKey(diu);
        String bucket = recommendContext.hasBucket("exercise_filter-yes") ? "exercise_filter-yes" : "";
        retrieveKey.setPlaceholder(bucket);

        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
