package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import org.apache.commons.lang3.StringUtils;

public class LocationRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public LocationRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String city = recommendContext.getRecommendRequest().getCity();
        if(StringUtils.isNotBlank(city) && city.length()>=1){
            RetrieveKey retrievekey = new RetrieveKey();
            retrievekey.setIhf(String.valueOf(recommendContext.getRecommendRequest().getIhf()));
            retrievekey.setType(RetrieverType.vlocation_city.name());
            retrievekey.setAlias(RetrieverType.vlocation_city.alias());
            retrievekey.setKey(city);
            if(city.length()<=3){
                retrievekey.setReason(city+"作品");
            }
            else{
                retrievekey.setReason("附近作品");
            }
            retrieveKeyContext.addRetrieveKey(retrievekey);
        }
    }
}
