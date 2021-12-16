package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

public class CityRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public CityRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String city = recommendContext.getRecommendRequest().getCity();
        if (city != null && city.length() > 1 && city.length() < 7) {
            RetrieveKey retrievekey = new RetrieveKey();
            retrievekey.setType(RetrieverType.vcity.name());
            retrievekey.setAlias(RetrieverType.vcity.alias());
            retrievekey.setKey(city);
            retrievekey.setReason(city+"最新流行");
            retrieveKeyContext.addRetrieveKey(retrievekey);
        }

    }
}
