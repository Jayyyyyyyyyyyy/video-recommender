package com.td.recommend.video.preprocessor;

import com.td.featurestore.item.ItemsProcessors;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;

/**
 * Created by admin on 2017/7/28.
 */
public class ChannelPreprocessorsFactory {
    public static ItemsProcessors<PredictItems<DocItem>> create(VideoRecommenderContext recommendContext) {
        ItemsProcessors<PredictItems<DocItem>> processors = new ItemsProcessors<>();
        processors.add(new ChannelInvalidItemFilterPreprocessor(recommendContext));
        return processors;
    }
}
