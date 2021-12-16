package com.td.recommend.video.retriever.keybuilder.follow;


import com.td.recommend.commons.retriever.ItemBasedRetrieveKeyBuilder;
import com.td.recommend.commons.retriever.RetrieveKeyBuilderPipeline;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilderFactory;
import com.td.recommend.video.retriever.keybuilder.VideoRetrieveKeyBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/7/14.
 */
public class FollowRetrieveKeyBuilderFactory implements RetrieveKeyBuilderFactory {
    private static FollowRetrieveKeyBuilderFactory instance = new FollowRetrieveKeyBuilderFactory();

    public static FollowRetrieveKeyBuilderFactory getInstance() {
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
        retrieveKeyBuilders.add(new FollowRetrieveKeyBuilder(context));
        return retrieveKeyBuilders;
    }

}
