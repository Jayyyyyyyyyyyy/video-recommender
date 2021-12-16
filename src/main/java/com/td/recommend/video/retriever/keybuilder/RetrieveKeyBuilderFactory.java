package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.video.recommender.VideoRecommenderContext;

/**
 * Created by Frang on 2018/5/8.
 */
public interface RetrieveKeyBuilderFactory {
     VideoRetrieveKeyBuilder create(VideoRecommenderContext context);
}
