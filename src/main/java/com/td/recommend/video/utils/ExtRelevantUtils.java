package com.td.recommend.video.utils;

import com.td.recommend.commons.request.Ihf;
import com.td.recommend.video.recommender.VideoRecommenderContext;

public class ExtRelevantUtils {
    public static boolean isExtRelevant(VideoRecommenderContext context, int ihf) {
        if (Ihf.isRelevant(ihf) && context.getRecommendRequest().getCid().equals("60000")) {
            return true;
        }
        return false;
    }

    public static boolean isWxAppExtRelevant(VideoRecommenderContext context, int ihf) {
        if (Ihf.isRelevant(ihf) && context.getRecommendRequest().getAppId().equals("t02")) {
            return true;
        }
        return false;
    }
}
