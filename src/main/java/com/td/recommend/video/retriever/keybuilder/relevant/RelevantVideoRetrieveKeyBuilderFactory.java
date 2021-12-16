package com.td.recommend.video.retriever.keybuilder.relevant;


import com.td.featurestore.item.ItemKey;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.ItemBasedRetrieveKeyBuilder;
import com.td.recommend.commons.retriever.RetrieveKeyBuilderPipeline;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilderFactory;
import com.td.recommend.video.retriever.keybuilder.VideoRetrieveKeyBuilder;
import com.td.recommend.video.retriever.keybuilder.immersive.ImmersiveHotRetrieveKeyBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/7/14.
 */
public class RelevantVideoRetrieveKeyBuilderFactory implements RetrieveKeyBuilderFactory {
    private static RelevantVideoRetrieveKeyBuilderFactory instance = new RelevantVideoRetrieveKeyBuilderFactory();

    public static RelevantVideoRetrieveKeyBuilderFactory getInstance() {
        return instance;
    }

    public VideoRetrieveKeyBuilder create(VideoRecommenderContext context) {
        String template = context.getRecommendRequest().getTemplate();
        DocItem docItem = (DocItem) context.getQueryItems().get(ItemKey.doc).get();

        List<ItemBasedRetrieveKeyBuilder<UserItem>> retrieveKeyBuilders = new ArrayList<>();
        if (template.equals("author")) {
            retrieveKeyBuilders.add(new RelevantAuthorRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new RelevantAuthorFreshRetrieveKeyBuilder(context));
        } else if (template.equals("mp3")) {
            retrieveKeyBuilders.add(new RelevantMp3RetrieveKeyBuilder(context));
        } else if (template.equals("like")) {
            retrieveKeyBuilders.add(new RelevantItemCFRetrieveKeyBuilder(context));
        } else {
            retrieveKeyBuilders.add(new RelevantOriginRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new RelevantAuthorRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new RelevantItemCFRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new RelevantMp3RetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new RelevantBertRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new RelevantGemRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new RelevantAuthorFreshRetrieveKeyBuilder(context));

            if (context.getRecommendRequest().getIhf() == Ihf.VSHOWDANCE_RLVT.id()) {
                if (DocProfileUtils.isSv_trendVideo(docItem)) {
                    retrieveKeyBuilders.add(new RelevantTrendRetrieveKeyBuilder(context));
                }
                retrieveKeyBuilders.add(new RelevantTalentFreshRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new RelevantShowDanceRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new RelevantOfAlbumRetrieveKeyBuilder(context));
            }
            if (context.hasBucket("blast_rlvt-yes")) {
                retrieveKeyBuilders.add(new RelevantBlastRetrieveKeyBuilder(context));
            }
            if (context.getRecommendRequest().getIhf() == Ihf.VSMALL_RLVT.id()) {
                retrieveKeyBuilders.add(new RelevantSubcatRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new RelevantTagRetrieveKeyBuilder(context));
            }
            if (context.getRecommendRequest().getIhf() == Ihf.VIMMERSIVE_RLVT.id()) {
                retrieveKeyBuilders.add(new ImmersiveHotRetrieveKeyBuilder(context));
            }

        }
        RetrieveKeyBuilderPipeline<UserItem> pipeline = new RetrieveKeyBuilderPipeline<>(retrieveKeyBuilders);

        return new VideoRetrieveKeyBuilder(pipeline, context);
    }

}
