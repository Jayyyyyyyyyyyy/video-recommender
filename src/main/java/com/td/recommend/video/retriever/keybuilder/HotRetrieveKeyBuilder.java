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
public class HotRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public HotRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {

        RetrieveKey retrieveKey = new RetrieveKey();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        retrieveKey.setIhf(String.valueOf(ihf))
                .setType(RetrieverType.vhot.name())
                .setKey(RetrieverType.vhot.name());
        if(ihf != Ihf.VSHOWDANCE_FEED.id() && ihf !=Ihf.VSHOWDANCE_RLVT.id()){
            retrieveKey.setReason("热播");
        }

        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
