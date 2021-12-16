package com.td.recommend.video.retriever.feedback;

import com.google.common.collect.ImmutableSet;
import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.userstore.data.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class InterestNegativeFeedback {
    public static final int maxCount = 100;
    public static final int defaultCount = 40;
    private static final Logger logger = LoggerFactory.getLogger(LatentNegativeFeedback.class);
    private static final ImmutableSet<String> kept = ImmutableSet.<String>builder()
            .add("1006").add("264")
            .add("1007").add("1009").add("265").add("285").add("300").add("305")
            .add("267").add("268").add("280").add("1519")
            .add("1905").add("1897").add("1586")
            .build();

    public static Map<String, Integer> compute(UserItem userItem, String facetName) {
        Map<String, Integer> blendNumberMap = new HashMap<>();
        String stFacet;
        if (facetName.equals("xfollow")) {
            stFacet = "st_vauthor_uid_ck";
        } else if (facetName.equals("sevmp3")) {
            stFacet = "st_vmp3_ck";
        } else if (facetName.equals("sevteacher")) {
            stFacet = "st_vteacher_ck";
        } else if (!facetName.startsWith("st_")) {
            stFacet = facetName.endsWith("_ck") ? facetName : facetName.replaceFirst("_cs", "_ck");
            stFacet = "st_" + stFacet;
        } else {
            return blendNumberMap;
        }
        Map<String, TVariance> stFeatureMap = UserProfileUtils.getVarianceFeatureMap(userItem, stFacet);
        stFeatureMap.forEach((k, v) -> {
            double ctr = v.getPosCnt() / (v.getNegCnt() + 0.1);
            double prob = Math.max(0.1, (ctr - 0.05) * 10);//以这个概率召回此类别
            if (v.getNegCnt() < 4) {
                prob = 1.0;
            }
            int count;//召回多少个
            if (ThreadLocalRandom.current().nextDouble() < prob) {
                count = (int) (Math.pow(2, ctr / 0.05 - 1) * 20);
            } else {
                count = 0;
                logger.info("inf filtered key:{}, ctr:{}, diu:{}", k, ctr, userItem.getId());
            }
            if (kept.contains(k)) {
                count = Math.max(count, maxCount);
            }
            blendNumberMap.put(k, Math.min(count, maxCount));
        });

        return blendNumberMap;
    }

    public static void main(String[] args) {

        System.out.println((int) (Math.pow(2, 0.005 / 0.05 - 1) * 10));
        System.out.println((int) (Math.pow(2, 0.05 / 0.05 - 1) * 10));
        System.out.println((int) (Math.pow(2, 0.10 / 0.05 - 1) * 10));
        System.out.println((int) (Math.pow(2, 0.12 / 0.05 - 1) * 10));
        System.out.println((int) (Math.pow(2, 0.15 / 0.05 - 1) * 10));
        System.out.println((int) (Math.pow(2, 0.20 / 0.05 - 1) * 10));
        System.out.println((int) (Math.pow(2, 0.25 / 0.05 - 1) * 10));

    }
}
