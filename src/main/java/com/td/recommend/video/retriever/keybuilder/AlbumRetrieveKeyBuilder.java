package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by admin on 2017/12/2.
 */
public class AlbumRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public AlbumRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String version = recommendContext.getRecommendRequest().getVersion();
        String client = recommendContext.getRecommendRequest().getClient();

        if (client == null || version == null) {
            return;
        }
        if (client.equals("1") && version.compareTo("6.9.4") <= 0) {//ios
            return;
        }
        if (client.equals("2") && version.compareTo("6.8.7.010600") <= 0) {//android
            return;
        }
        if (client.equals("2") && version.equals("6.8.8.011314")) {//android
            return;
        }
        if (client.equals("2") && version.equals("6.8.9")) {//android
            return;
        }
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(RetrieverType.valbum.name())
                .setKey(RetrieverType.valbum.name())
                .setReason("专辑");

        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
