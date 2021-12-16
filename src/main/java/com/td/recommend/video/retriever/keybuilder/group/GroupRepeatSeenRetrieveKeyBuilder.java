package com.td.recommend.video.retriever.keybuilder.group;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

/**
 * Created by admin on 2017/8/8.
 */
public class GroupRepeatSeenRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public GroupRepeatSeenRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String diu = userItem.getId().replaceFirst("debug_", "");
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.addAttribute("maxCnt", 2);
        retrieveKey.setReason("看了又看");
        retrieveKey.setKey(diu);

        retrieveKey.setType(RetrieverType.grepeat_seen.name());
        retrieveKey.setAlias(RetrieverType.grepeat_seen.alias());
        if (UserProfileUtils.isFitnessUser(userItem, null)) {
            retrieveKey.setPlaceholder("1007");
        } else if (UserProfileUtils.is32StepUser(userItem, null)) {
            retrieveKey.setPlaceholder("268");
        }
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
