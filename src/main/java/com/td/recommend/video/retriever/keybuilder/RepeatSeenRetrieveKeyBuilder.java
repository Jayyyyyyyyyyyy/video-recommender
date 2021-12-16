package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by admin on 2017/8/8.
 */
public class RepeatSeenRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public RepeatSeenRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String diu = userItem.getId().replaceFirst("debug_", "");
        RetrieveKey retrievekey = new RetrieveKey();
        retrievekey.addAttribute("maxCnt", 2);
        retrievekey.setReason("看了又看");
        retrievekey.setKey(diu);
        if (recommendContext.getBuckets().contains("repeat_seen-v1")) {
            retrievekey.setType(RetrieverType.vrepeatv1_seen.name());
            retrievekey.setAlias(RetrieverType.vrepeatv1_seen.alias());
        } else if (recommendContext.getBuckets().contains("repeat_seen-v2")) {
            retrievekey.setType(RetrieverType.vrepeatv2_seen.name());
            retrievekey.setAlias(RetrieverType.vrepeatv2_seen.alias());
        } else {
            retrievekey.setType(RetrieverType.vrepeat_seen.name());
            retrievekey.setAlias(RetrieverType.vrepeat_seen.alias());
        }
        retrieveKeyContext.addRetrieveKey(retrievekey);
    }
}
