package com.td.recommend.video.retriever.keybuilder.group;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

/**
 * Created by admin on 2017/12/2.
 */
public class GroupNmfRetrieveKeyBuilder implements RetrieveKeyBuilder {

    private VideoRecommenderContext videoRecommenderContext;

    public GroupNmfRetrieveKeyBuilder(VideoRecommenderContext videoRecommenderContext) {
        this.videoRecommenderContext = videoRecommenderContext;

    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String userId = userItem.getId().replaceFirst("debug_", "");
        String retrieveKeyName = RetrieverType.gnmf.name();

        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(retrieveKeyName)
                .addAttribute("maxCnt", 500)
                .setKey(userId);
        if (UserProfileUtils.isFitnessUser(userItem, null)) {
            retrieveKey.setPlaceholder("1007");
        } else if (UserProfileUtils.is32StepUser(userItem, null)) {
            retrieveKey.setPlaceholder("268");
        }

        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
