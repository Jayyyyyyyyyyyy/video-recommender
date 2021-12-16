package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.List;

/**
 * Created by zjl on 2019/11/18.
 */
public class InitialRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    public InitialRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }
    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        addInitialRetrieveKeys(userItem, retrieveKeyContext, RetrieverType.vcat_init);
        addInitialRetrieveKeys(userItem, retrieveKeyContext, RetrieverType.vsubcat_init);
        addInitialRetrieveKeys(userItem, retrieveKeyContext, RetrieverType.vtag_init);
        addInitialRetrieveKeys(userItem, retrieveKeyContext, RetrieverType.vdegree_init);
    }

    private void addInitialRetrieveKeys(UserItem userItem, RetrieveKeyContext retrieveKeyContext, RetrieverType type) {
        List<String> initList;
        try {
            initList = userItem.getUserRawData().get()
                    .getFeaturesMap().get(type.name()).getKey();
        } catch (Exception e) {
            return;
        }
        if (initList == null||initList.size()==0) {
            return;
        }
        double p = Math.min(1.0, 2.0 / (initList.size() + 0.1));
        BinomialDistribution bd = new BinomialDistribution(1, p);
        initList.stream().filter(r -> bd.sample() == 1).forEach(key -> {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setKey(key);
            retrieveKey.setType(type.name());
            retrieveKey.setAlias(type.alias());
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        });
    }
}
