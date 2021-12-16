package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

/**
 * Created by admin on 2017/9/6.
 */
public class RelevantBlastRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public RelevantBlastRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        RetrieveKey retrievekey = new RetrieveKey();
        retrievekey.setType(RetrieverType.vblast_rlvt.name())
                .setAlias(RetrieverType.vblast_rlvt.alias())
                .setKey(recommendContext.getRecommendRequest().getCid())
                .addAttribute("maxCnt", 1);
        retrievekey.setScore(1.0);
        retrieveKeyContext.addRetrieveKey(retrievekey);
    }
}
