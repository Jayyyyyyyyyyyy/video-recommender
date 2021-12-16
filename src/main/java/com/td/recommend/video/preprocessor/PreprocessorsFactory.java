package com.td.recommend.video.preprocessor;

import com.td.featurestore.item.ItemsProcessors;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;

/**
 * Created by admin on 2017/7/28.
 */
public class PreprocessorsFactory {
    public static ItemsProcessors<PredictItems<DocItem>> create(VideoRecommenderContext recommendContext) {
        ItemsProcessors<PredictItems<DocItem>> processors = new ItemsProcessors<>();
        int ihf = recommendContext.getRecommendRequest().getIhf();

        if (ihf == Ihf.VSMALL_FEED.id()) {
            processors.add(new SmallInvalidItemFilterPreprocessor(recommendContext));
        } else if (ihf == Ihf.VFITPACK_FEED.id()) {
            processors.add(new FitPackInvalidItemFilterPreprocessor(recommendContext));
        } else if (ihf == Ihf.VMIX_FOLLOW.id()) {
            processors.add(new FollowInvalidItemFilterPreprocessor(recommendContext));
        } else if (ihf == Ihf.VNOT_PLAY.id()) {
            processors.add(new NotPlayInvalidItemFilterPreprocessor(recommendContext));
        } else if (ihf == Ihf.VSHOWDANCE_FEED.id()) {
            processors.add(new ShowDanceFeedInvalidItemFilterPreprocessor(recommendContext));
        } else {
            processors.add(new FeedInvalidItemFilterPreprocessor(recommendContext));
            processors.add(new LatentFeedbackPreprocessor(recommendContext));
        }

        if (recommendContext.hasBucket("mp3copyright-yes")) {
            processors.add(new Mp3CopyrightItemFilterPreprocessor(recommendContext));
        }

        return processors;
    }
}
