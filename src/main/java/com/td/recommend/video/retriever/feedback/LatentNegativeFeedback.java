package com.td.recommend.video.retriever.feedback;

import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class LatentNegativeFeedback {
    public static final int maxCount = 200;
    public static final int defaultCount = 80;
    private static final Logger logger = LoggerFactory.getLogger(LatentNegativeFeedback.class);

    public static Map<String, Integer> compute(UserItem userItem, String facetName) {
        Map<String, Integer> blendNumberMap = new HashMap<>();
        String stFacet = "st_" + facetName + "_ck";
        String longFacet = facetName + "_ck";

        Map<String, TVariance> stFeatureMap = UserProfileUtils.getVarianceFeatureMap(userItem, stFacet);
        Map<String, TVariance> longFeatureMap = UserProfileUtils.getVarianceFeatureMap(userItem, longFacet);

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
                logger.info("lnf filtered key:{}, ctr:{}, diu:{}", k, ctr, userItem.getId());
            }
            blendNumberMap.put(k, Math.min(count, maxCount));
        });

        longFeatureMap.forEach((k, v) -> {
            if (v.getMean() < -1.0) {
                blendNumberMap.put(k, 0);
            }
        });
        Integer dance = blendNumberMap.get("264");
        if (dance != null) {
            blendNumberMap.put("264", Math.max(defaultCount, dance));//保证舞蹈有一定量
        }
        Integer fitness = blendNumberMap.get("1006");
        if (fitness != null) {
            blendNumberMap.put("1006", Math.max(defaultCount, fitness));//保证健身有一定量
        }
        return blendNumberMap;
    }

    public static void main(String[] args) {
        System.out.println((int) (Math.pow(2, 0.014 / 0.05 - 1) * 20));
        System.out.println((int) (Math.pow(2, 0.03 / 0.05 - 1) * 20));
        System.out.println((int) (Math.pow(2, 0.05 / 0.05 - 1) * 20));
        System.out.println((int) (Math.pow(2, 0.08 / 0.05 - 1) * 20));
        System.out.println((int) (Math.pow(2, 0.11 / 0.05 - 1) * 20));
        System.out.println((int) (Math.pow(2, 0.21 / 0.05 - 1) * 20));
        Map<String, TVariance> vcat_ck = UserProfileUtils.getVarianceFeatureMap(new UserItemDao().get("868179040094073").get(), "vcat_ck");
    }
}
