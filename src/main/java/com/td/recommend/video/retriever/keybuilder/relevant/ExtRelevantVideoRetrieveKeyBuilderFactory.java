package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.ItemBasedRetrieveKeyBuilder;
import com.td.recommend.commons.retriever.RetrieveKeyBuilderPipeline;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.keybuilder.*;
import com.td.recommend.video.retriever.keybuilder.immersive.ImmersiveHotRetrieveKeyBuilder;

import java.util.ArrayList;
import java.util.List;

public class ExtRelevantVideoRetrieveKeyBuilderFactory implements RetrieveKeyBuilderFactory {
    private static ExtRelevantVideoRetrieveKeyBuilderFactory instance = new ExtRelevantVideoRetrieveKeyBuilderFactory();

    public static ExtRelevantVideoRetrieveKeyBuilderFactory getInstance() {
        return instance;
    }

    public VideoRetrieveKeyBuilder create(VideoRecommenderContext context) {
        List<ItemBasedRetrieveKeyBuilder<UserItem>> retrieveKeyBuilders = new ArrayList<>();

        retrieveKeyBuilders.add(new RelevantItemCFRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new ExtRelevantUserCFRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new NmfRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new ItemCFRetrieveKeyBuilder(context));

        retrieveKeyBuilders.add(new GemRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new BprRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantGemRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new AuthorRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new Mp3RetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantMp3RetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantBertRetrieveKeyBuilder(context));
        retrieveKeyBuilders.add(new RelevantAuthorRetrieveKeyBuilder(context));


        RetrieveKeyBuilderPipeline<UserItem> pipeline = new RetrieveKeyBuilderPipeline<>(retrieveKeyBuilders);
        if (context.getRecommendRequest().getAppId().equals("t02")) {
            //           retrieveKeyBuilders.add(new RelevantMp3RetrieveKeyBuilder(context));
//            retrieveKeyBuilders.add(new RelevantGemRetrieveKeyBuilder(context));
//            retrieveKeyBuilders.add(new RelevantBertRetrieveKeyBuilder(context));
//            retrieveKeyBuilders.add(new RelevantAuthorRetrieveKeyBuilder(context));
//            retrieveKeyBuilders.add(new GemRetrieveKeyBuilder(context));
        }

        return new VideoRetrieveKeyBuilder(pipeline, context);
    }

}
