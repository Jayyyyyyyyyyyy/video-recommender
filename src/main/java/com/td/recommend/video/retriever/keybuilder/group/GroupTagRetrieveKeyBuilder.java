package com.td.recommend.video.retriever.keybuilder.group;

import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by admin on 2017/6/21.
 */
public class GroupTagRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(GroupTagRetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public GroupTagRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        Map<String, TVariance> group = UserProfileUtils.getVarianceFeatureMap(userItem, "group_tag");
        RetrieverType type = RetrieverType.gtag;
        group.forEach((k, v) -> {
            if (v != null && v.mean > 0.5) {
                RetrieveKey retrievekey = new RetrieveKey();
                retrievekey.setType(type.name());
                retrievekey.setAlias(type.alias());
                retrievekey.addAttribute("maxCnt", 128);
                retrievekey.setKey(k);
                retrieveKeyContext.addRetrieveKey(retrievekey);
            }
        });
    }
}

