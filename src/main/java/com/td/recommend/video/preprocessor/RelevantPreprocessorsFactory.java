package com.td.recommend.video.preprocessor;

import com.td.featurestore.item.ItemsProcessors;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;

/**
 * Created by admin on 2017/7/28.
 */
public class RelevantPreprocessorsFactory {
    public static ItemsProcessors<PredictItems<DocItem>> create(VideoRecommenderContext recommendContext) {
        ItemsProcessors<PredictItems<DocItem>> processors = new ItemsProcessors<>();
        if (Ihf.VSHOWDANCE_RLVT.id() == recommendContext.getRecommendRequest().getIhf()) {
            processors.add(new ShowDanceRelevantInvalidItemFilterPreprocessor(recommendContext));
        } else {
            processors.add(new RelevantInvalidItemFilterPreprocessor(recommendContext));
            if (recommendContext.hasBucket("relevant_mp3copyright-yes")) {
                processors.add(new Mp3CopyrightItemFilterPreprocessor(recommendContext));
            }
        }
        return processors;
    }
}
