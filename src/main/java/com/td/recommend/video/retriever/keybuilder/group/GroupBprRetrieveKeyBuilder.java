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
public class GroupBprRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public GroupBprRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }
    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String userId = userItem.getId().replaceFirst("debug_", "");


        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(RetrieverType.gbpr.name())
                .setAlias(RetrieverType.gbpr.alias())
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
