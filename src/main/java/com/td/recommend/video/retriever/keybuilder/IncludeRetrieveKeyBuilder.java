package com.td.recommend.video.retriever.keybuilder;

import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.IncludeConfigs;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.Map;

/**
 * Created by admin on 2017/12/2.
 */
public class IncludeRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public IncludeRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        Map<String, IncludeConfigs.Conf> in = IncludeConfigs.get();
        in.forEach((key, conf) -> {
            TVariance cs = UserProfileUtils.getVarianceFeatureMap(userItem, conf.getType() + "_cs").get(key);
            TVariance ck = UserProfileUtils.getVarianceFeatureMap(userItem, conf.getType() + "_ck").get(key);
            if ((cs != null && cs.getMean() > 0.2 && cs.getVariance() < 0.9) ||
                    (ck != null && ck.getMean() > 0.1 && ck.getVariance() < 0.9)) {
                RetrieveKey retrieveKey = new RetrieveKey();
                retrieveKey.setType(conf.getType() + "_in")
                        .setAlias(conf.getAlias())
                        .setKey(key);
                retrieveKeyContext.addRetrieveKey(retrieveKey);
            }
        });
    }
}
