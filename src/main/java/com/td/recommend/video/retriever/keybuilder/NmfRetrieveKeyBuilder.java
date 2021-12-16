package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.Set;

/**
 * Created by admin on 2017/12/2.
 */
public class NmfRetrieveKeyBuilder implements RetrieveKeyBuilder {

    private VideoRecommenderContext videoRecommenderContext;

    public NmfRetrieveKeyBuilder(VideoRecommenderContext videoRecommenderContext) {
        this.videoRecommenderContext = videoRecommenderContext;

    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String userId = userItem.getId().replaceFirst("debug_", "");

        Set<String> buckets = videoRecommenderContext.getBuckets();

        String retrieveKeyName = RetrieverType.vnmfv3.name();
        String retrieveKeyAlias = RetrieverType.vnmfv3.alias();

        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(retrieveKeyName)
                .setAlias(retrieveKeyAlias)
                .addAttribute("maxCnt", 30)
                .addAttribute("appid", videoRecommenderContext.getRecommendRequest().getAppId())
                .setKey(userId);
        String bucket = videoRecommenderContext.hasBucket("exercise_filter-yes") ? "exercise_filter-yes" : "";
        retrieveKey.setPlaceholder(bucket);

        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
