package com.td.recommend.video.rank.featuredumper;


import com.td.recommend.video.recommender.VideoRecommenderContext;

/**
 * Created by admin on 2017/8/3.
 */
public interface FeatureDumperSampler {
    boolean needDump(VideoRecommenderContext uservideoRecommendContext);
}
