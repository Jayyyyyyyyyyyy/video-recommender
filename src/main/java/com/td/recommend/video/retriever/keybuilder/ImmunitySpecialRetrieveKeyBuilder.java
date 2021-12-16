package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create by pansm at 2020/01/26
 */
public class ImmunitySpecialRetrieveKeyBuilder implements RetrieveKeyBuilder{
    private static Logger LOG = LoggerFactory.getLogger(ImmunitySpecialRetrieveKeyBuilder.class);
    private VideoRecommenderContext recommendContext;

    public ImmunitySpecialRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {

        String version = recommendContext.getRecommendRequest().getVersion();
        String client = recommendContext.getRecommendRequest().getClient();
        LOG.info("ImmunitySpecialRetrieveKeyBuilder client:{},version:{}",client,version);

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

        RetrieveKey retrievekey = new RetrieveKey();
        retrievekey.setType(RetrieverType.vimmunity_album.name())
                .setKey(RetrieverType.vimmunity_album.name())
                .setReason("专辑");

        retrievekey.setScore(1.0);
        retrieveKeyContext.addRetrieveKey(retrievekey);
    }

}
