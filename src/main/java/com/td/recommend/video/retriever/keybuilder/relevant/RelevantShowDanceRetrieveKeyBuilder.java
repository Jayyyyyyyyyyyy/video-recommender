package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by admin on 2017/6/21.
 */
public class RelevantShowDanceRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantShowDanceRetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public RelevantShowDanceRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        int ihf = recommendContext.getRecommendRequest().getIhf();
        RetrieveKey retrieveKeyShow = new RetrieveKey();
        retrieveKeyShow.setIhf(String.valueOf(ihf))
                .setType(RetrieverType.vshow_rlvt.name())
                .setAlias(RetrieverType.vshow_rlvt.alias())
                .setKey("");
        retrieveKeyContext.addRetrieveKey(retrieveKeyShow);
    }
}
