package com.td.recommend.video.retriever.keybuilder.channel;


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
public class ChannelRetrieveKeyBuilderFactory implements RetrieveKeyBuilderFactory {
    private static ChannelRetrieveKeyBuilderFactory instance = new ChannelRetrieveKeyBuilderFactory();

    public static ChannelRetrieveKeyBuilderFactory getInstance() {
        return instance;
    }

    public VideoRetrieveKeyBuilder create(VideoRecommenderContext context) {
        List<ItemBasedRetrieveKeyBuilder<UserItem>> retrieveKeyBuilders = new ArrayList<>();
        retrieveKeyBuilders.add(new ChannelRetrieveKeyBuilder(context));
        RetrieveKeyBuilderPipeline<UserItem> pipeline = new RetrieveKeyBuilderPipeline<>(retrieveKeyBuilders);

        return new VideoRetrieveKeyBuilder(pipeline, context);
    }

}
