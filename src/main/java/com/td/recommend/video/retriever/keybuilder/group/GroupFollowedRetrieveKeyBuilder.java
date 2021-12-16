package com.td.recommend.video.retriever.keybuilder.group;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2017/12/2.
 */
public class GroupFollowedRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public GroupFollowedRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, RetrieverType.gfollow);
        featureExtractor.extract(Collections.singletonList("xfollow"));
        Set<RetrieveKey> retrieveKeys = featureExtractor.getKeyFeaturesMap().keySet();
        double p = Math.min(1.0, 3 / (retrieveKeys.size() + 0.1));
        BinomialDistribution bd = new BinomialDistribution(1, p);
        String lastFollow = "";
        try {
            List<String> Ids = userItem.getUserRawData().get()
                    .getFeaturesMap().get("xfollow").getKey();
            lastFollow = Ids.get(0);
        } catch (Exception ignored) {
        }
        String key = lastFollow;

        retrieveKeys.stream()
                .peek(r -> {
                    if (r.getKey().equals(key)) {
                        retrieveKeyContext.addRetrieveKey(r);
                    }
                })
                .filter(r -> bd.sample() == 1)
                .forEach(r->{
                    retrieveKeyContext.addRetrieveKey(r);
                });
    }

    public static void main(String[] args) {
        UserItem userItem = new UserItemDao().get("863525049525340").get();
//        TL2Entry follow = userItem.getUserRawData().get().getFeaturesMap().get("xfollow");
//        System.out.println(follow.getKey()+":"+follow.getSvalue());
        Map<String, String> featureMap = UserProfileUtils.getSValueFeaturesMap(userItem, "xfollow");
        for (Map.Entry<String, String> entry : featureMap.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}
