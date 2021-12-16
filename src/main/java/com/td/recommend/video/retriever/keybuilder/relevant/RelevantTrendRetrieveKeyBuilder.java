package com.td.recommend.video.retriever.keybuilder.relevant;

import com.google.common.collect.ImmutableSet;
import com.td.featurestore.item.ItemKey;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by admin on 2017/6/21.
 */
public class RelevantTrendRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantTrendRetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public RelevantTrendRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        int ihf = recommendContext.getRecommendRequest().getIhf();
        String uid = recommendContext.getRecommendRequest().getUid();
        DocItem docItem = (DocItem) recommendContext.getQueryItems().get(ItemKey.doc).get();
        Optional<UserItem> userItemFollow = UserVideoItemDataSource.getInstance().getUserItemDao().getUserFollow(uid);
        if (DocProfileUtils.isSv_trendVideo(docItem)) {
//        if (DocProfileUtils.getCtype(docItem) == 503) {
//            //hot
//            RetrieveKey retrieveKeyHot = new RetrieveKey();
//            retrieveKeyHot.setIhf(String.valueOf(ihf))
//                    .setType(RetrieverType.thot_rlvt.name())
//                    .setAlias(RetrieverType.thot_rlvt.alias())
//                    .setKey("");
//            retrieveKeyContext.addRetrieveKey(retrieveKeyHot);

            //topic
            String topic = DocProfileUtils.getTopic(docItem);
            RetrieveKey retrieveKeyTopic = new RetrieveKey();
            retrieveKeyTopic.setIhf(String.valueOf(ihf))
                    .setType(RetrieverType.ttopic_rlvt.name())
                    .setAlias(RetrieverType.ttopic_rlvt.alias())
                    .setKey(topic);
            retrieveKeyContext.addRetrieveKey(retrieveKeyTopic);
            //follow
            if (userItemFollow.isPresent()) {
                Map<String, Double> vfollow = UserProfileUtils.getValueFeaturesMap(userItemFollow.get(), "tfollow_rlvt");
                ArrayList<String> followedUids = new ArrayList<>(vfollow.keySet());
                Collections.shuffle(followedUids);
                followedUids.subList(0, Math.min(followedUids.size(), 10)).forEach(followedUid -> {
                    RetrieveKey retrieveKeyFollow = new RetrieveKey();
                    retrieveKeyFollow.setIhf(String.valueOf(ihf))
                            .setType(RetrieverType.tfollow_rlvt.name())
                            .setAlias(RetrieverType.tfollow_rlvt.alias())
                            .setKey(followedUid);
                    retrieveKeyContext.addRetrieveKey(retrieveKeyFollow);
                });
            }
        }
    }
}
