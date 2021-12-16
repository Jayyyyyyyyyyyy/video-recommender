package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.preprocessor.FitPackInvalidItemFilterPreprocessor;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.Set;

/**
 * Created by admin on 2017/6/21.
 */
public class FitPackSubcatRetrieveKeyBuilder implements RetrieveKeyBuilder {
    public VideoRecommenderContext recommendContext;

    public FitPackSubcatRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        Set<String> subcats = FitPackInvalidItemFilterPreprocessor.subcats;
        for (String subcat : subcats) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(RetrieverType.vsubcat.name());
            retrieveKey.setAlias(RetrieverType.vsubcat.alias());
            retrieveKey.setKey(subcat);
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }
    }
}