package com.td.recommend.video.retriever.keybuilder;

import com.google.common.collect.Lists;
import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.explorer.Arm;
import com.td.recommend.video.retriever.explorer.MultiArmedBandit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Liujikun on 2019/10/17.
 */
public class GEuRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(GEuRetrieveKeyBuilder.class);
    private VideoRecommenderContext recommendContext;
    private static final int maxSize = 1;
    private double minVar = 0.01;

    public GEuRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        for (RetrieverType.GEu type : RetrieverType.GEu.values()) {
            Set<RetrieveKey> retrieveKeys = generate(userItem,
                    Lists.newArrayList(type.getKey()),
                    type,
                    UserProfileUtils.getVarianceFeatureMap(userItem, type.getFacet()),
                    UserProfileUtils.getVarianceFeatureMap(userItem, "st_" + type.getFacet()));
            retrieveKeyContext.addRetrieveKeys(retrieveKeys);
        }
    }

    private Set<RetrieveKey> generate(UserItem userItem, List<String> catList, RetrieverType.GEu retrieverType, Map<String, TVariance> varianceMap, Map<String, TVariance> shortTermVarianceMap) {
        List<Arm> arms = new ArrayList<>();
        for (String candidate : catList) {
            TVariance variance = varianceMap.get(candidate);
            TVariance shortVariance = shortTermVarianceMap.get(candidate);
            double negCnt = getNegCnt(variance, shortVariance);
            double posCnt = getPosCnt(variance, shortVariance);
            double missCnt = negCnt - posCnt > 0 ? negCnt - posCnt : 0;
            Arm arm = new Arm(candidate, posCnt + 1, missCnt + 1);
            arms.add(arm);
        }

        List<Arm> topArms = MultiArmedBandit.thompsonSampling(arms);

        Set<RetrieveKey> keys = topArms.stream()
                .filter(arm -> {
                    if (arm.getVariance() > minVar) {
                        return true;
                    } else {
                        LOG.info("eu candidate:{} removed because win:{}, loose:{}, diu:{}", arm.getName(), arm.getWin(), arm.getLoose(), userItem.getId());
                        return false;
                    }
                })
                .sorted(Comparator.comparingDouble(Arm::getVariance))
                .limit(maxSize)
                .map(arm -> {
                    RetrieveKey retrieveKey = new RetrieveKey();
                    retrieveKey.setType(retrieverType.name())
                            .setAlias(retrieverType.getAlias())
                            .setKey(arm.getName())
                            .addAttribute("maxCnt", 5);
                    return retrieveKey;
                }).collect(Collectors.toSet());
        return keys;

    }

    private static double getNegCnt(TVariance longVariance, TVariance shortVariance) {
        double negCnt = 0.0;

        if (longVariance != null) {
            negCnt += longVariance.getNegCnt();
        }

        if (shortVariance != null) {
            negCnt += shortVariance.getNegCnt();
        }

        return negCnt;
    }

    private static double getPosCnt(TVariance longVariance, TVariance shortVariance) {
        double posCnt = 0.0;

        if (longVariance != null) {
            posCnt += longVariance.getPosCnt();
        }

        if (shortVariance != null) {
            posCnt += shortVariance.getPosCnt();
        }

        return posCnt;
    }
}
