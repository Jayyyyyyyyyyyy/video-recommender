package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.retriever.ItemBasedRetrieveKeyBuilder;
import com.td.recommend.commons.retriever.RetrieveKeyBuilderPipeline;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.keybuilder.*;

import java.util.ArrayList;
import java.util.List;

public class WxAppRelevantVideoRetrieveKeyBuilderFactory implements RetrieveKeyBuilderFactory {
    private static WxAppRelevantVideoRetrieveKeyBuilderFactory instance = new WxAppRelevantVideoRetrieveKeyBuilderFactory();

    public static WxAppRelevantVideoRetrieveKeyBuilderFactory getInstance() {
        return instance;
    }

    public VideoRetrieveKeyBuilder create(VideoRecommenderContext context) {
        List<ItemBasedRetrieveKeyBuilder<UserItem>> retrieveKeyBuilders = new ArrayList<>();

        retrieveKeyBuilders.add(new NmfRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new ItemCFRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new UserCFRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new GemRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantOriginRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantAuthorRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantItemCFRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantMp3RetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantBertRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantGemRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantBlastRetrieveKeyBuilder(context));


        RetrieveKeyBuilderPipeline<UserItem> pipeline = new RetrieveKeyBuilderPipeline<>(retrieveKeyBuilders);

        return new VideoRetrieveKeyBuilder(pipeline, context);
    }
}
