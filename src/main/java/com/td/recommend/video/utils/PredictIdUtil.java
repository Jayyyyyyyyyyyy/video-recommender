package com.td.recommend.video.utils;

import com.td.recommend.commons.idgenerator.PredictIdGenerator;
import com.td.recommend.video.recommender.VideoRecommenderContext;

public class PredictIdUtil {
    public static String generatePredictId(VideoRecommenderContext videoRecommenderContext) {
        String version = videoRecommenderContext.getRecommendRequest().getVersion();
        String diu = videoRecommenderContext.getRecommendRequest().getDiu();
        if (version.compareTo("6.8.6.121622") > 0) {
            return PredictIdGenerator.getInstance().generateNew("");
        } else {
            return PredictIdGenerator.getInstance().generate(diu);
        }
    }
}
