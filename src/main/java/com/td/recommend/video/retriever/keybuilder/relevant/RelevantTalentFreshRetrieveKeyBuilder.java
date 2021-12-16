package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

public class RelevantTalentFreshRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final int MAX_COUNT = 50;
    private static final int MAX_ELAPSED_SECONDS = 60 * 60 * 6;

    public RelevantTalentFreshRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        int ihf = recommendContext.getRecommendRequest().getIhf();

        RetrieverType retrieverType;
        if (ihf == Ihf.VBIG_RLVT.id()) {
            retrieverType = RetrieverType.vtalentfresh_rlvt;
        } else if(ihf == Ihf.VSHOWDANCE_RLVT.id()){
            retrieverType = RetrieverType.ttalentfresh_rlvt;
        }
        else {
            retrieverType = RetrieverType.svtalentfresh_rlvt;
        }
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(retrieverType.name())
                .setKey(retrieverType.name())
                .setReason("达人新作");
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }

}

