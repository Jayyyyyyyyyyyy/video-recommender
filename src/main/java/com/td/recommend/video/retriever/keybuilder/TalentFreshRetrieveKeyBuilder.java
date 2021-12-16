package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by admin on 2017/12/2.
 */
public class TalentFreshRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public TalentFreshRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        int ihf = recommendContext.getRecommendRequest().getIhf();

        RetrieverType retrieverType;

        if(ihf == Ihf.VSHOWDANCE_FEED.id()){
            retrieverType = RetrieverType.ttalentfresh;
        }
        else{
            retrieverType = RetrieverType.vtalentfresh;
        }

        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setIhf(String.valueOf(ihf))
                .setType(retrieverType.name())
                .setKey(retrieverType.name())
                .addAttribute("maxCnt", 20);
        if(ihf != Ihf.VSHOWDANCE_FEED.id()){
            retrieveKey.setReason("达人新作");
        }
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
