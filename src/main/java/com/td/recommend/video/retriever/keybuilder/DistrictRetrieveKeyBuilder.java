package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

public class DistrictRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public DistrictRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String district = recommendContext.getRecommendRequest().getDistrict();
        if (district != null && district.length() > 1 && district.length() < 7) {
            RetrieveKey retrievekey = new RetrieveKey();
            retrievekey.setType(RetrieverType.vdistrict.name());
            retrievekey.setAlias(RetrieverType.vdistrict.alias());
            retrievekey.setKey(district);
            retrievekey.setReason(district+"最新流行");
            retrieveKeyContext.addRetrieveKey(retrievekey);
        }

    }
}
