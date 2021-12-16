package com.td.recommend.video.rank.featuredumper;

import com.td.recommend.video.abtest.BucketConstants;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by admin on 2017/8/3.
 */
public class UserNewsFeatureDumperSampler implements FeatureDumperSampler {
    @Override
    public boolean needDump(VideoRecommenderContext recommendContext) {
//        if (recommendContext.hasBucket(BucketConstants.MODEL_GBDT)) {
//            ThreadLocalRandom random = ThreadLocalRandom.current();
//            int randInt = random.nextInt(100);
//            return randInt < 25;
//        }
//
//        return false;
        return true;
    }
}
