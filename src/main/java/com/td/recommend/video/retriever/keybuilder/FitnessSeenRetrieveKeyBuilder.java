package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by admin on 2017/8/8.
 */
public class FitnessSeenRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public FitnessSeenRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        if (recommendContext.hasBucket("vfitness_seen-no")) {
            return;
        }

        String diu = userItem.getId().replaceFirst("debug_", "");
        RetrieveKey retrievekey = new RetrieveKey();
        retrievekey.setType(RetrieverType.vfitness_seen.name());
        retrievekey.setAlias(RetrieverType.vfitness_seen.alias());
        retrievekey.addAttribute("maxCnt", 3);
        retrievekey.setReason("看了又看");
        retrievekey.setKey(diu);
        retrievekey.setPlaceholder("exp");
        retrieveKeyContext.addRetrieveKey(retrievekey);


    }
}
