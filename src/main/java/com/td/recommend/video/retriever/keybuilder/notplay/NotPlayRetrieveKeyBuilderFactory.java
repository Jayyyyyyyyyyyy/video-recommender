package com.td.recommend.video.retriever.keybuilder.notplay;


import com.td.recommend.commons.retriever.ItemBasedRetrieveKeyBuilder;
import com.td.recommend.commons.retriever.RetrieveKeyBuilderPipeline;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.keybuilder.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/7/14.
 */
public class NotPlayRetrieveKeyBuilderFactory implements RetrieveKeyBuilderFactory {
    private static NotPlayRetrieveKeyBuilderFactory instance = new NotPlayRetrieveKeyBuilderFactory();

    public static NotPlayRetrieveKeyBuilderFactory getInstance() {
        return instance;
    }

    public VideoRetrieveKeyBuilder create(VideoRecommenderContext context) {
        List<ItemBasedRetrieveKeyBuilder<UserItem>> retrieveKeyBuilders = new ArrayList<>();

        retrieveKeyBuilders.addAll(buildNormalUserKeys(context));

        RetrieveKeyBuilderPipeline<UserItem> pipeline = new RetrieveKeyBuilderPipeline<>(retrieveKeyBuilders);

        return new VideoRetrieveKeyBuilder(pipeline, context);
    }

    private List<ItemBasedRetrieveKeyBuilder<UserItem>> buildNormalUserKeys(VideoRecommenderContext context) {
        List<ItemBasedRetrieveKeyBuilder<UserItem>> retrieveKeyBuilders = new ArrayList<>();
        retrieveKeyBuilders.add(new PopularRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new HotRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new TalentRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new TalentFreshRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new ItemCFRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new NmfRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new UserCFRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new GemV2RetrieveKeyBuilder(context));
        return retrieveKeyBuilders;
    }

}
