package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LookLikeRetrieveKeyBuilder implements RetrieveKeyBuilder{
    private VideoRecommenderContext recommendContext;
    private static final Logger LOG = LoggerFactory.getLogger(LookLikeRetrieveKeyBuilder.class);

    public LookLikeRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }
    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String diu = userItem.getId().replaceFirst("debug_", "");
        if(StringUtils.isNoneBlank(diu)){
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(RetrieverType.valike_ev.name());
            retrieveKey.setAlias(RetrieverType.valike_ev.alias());
            retrieveKey.setKey(diu);
            retrieveKey.addAttribute("maxCnt", 5);
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }
    }
}
