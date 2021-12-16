package com.td.recommend.video.retriever.keybuilder;

import com.google.common.collect.Lists;
import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.ExploreUserCatsMap;
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
public class ExploreUserRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ExploreUserRetrieveKeyBuilder.class);
    private VideoRecommenderContext recommendContext;
    private static final RetrieverType subcatDerivedType = RetrieverType.vsubcat_eu;
    private static final RetrieverType vxsubcatDerivedType = RetrieverType.vxsubcat_eu;
    private static final RetrieverType tagDerivedType = RetrieverType.vtag_eu;
    private static final int maxSize = 1;
    private double minVar;
    private static final double minScore = 0.15;

    public ExploreUserRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
        minVar = 0.01;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        Set<RetrieveKey> derivedKeys = generate(userItem,
                ExploreUserCatsMap.getSubcats(),
                subcatDerivedType,
                UserProfileUtils.getVarianceFeatureMap(userItem, "vsubcat_ck"),
                UserProfileUtils.getVarianceFeatureMap(userItem, "st_vsubcat_ck"));
        retrieveKeyContext.addRetrieveKeys(derivedKeys);

        Set<RetrieveKey> xDerivedKeys = generate(userItem,
                ExploreUserCatsMap.getVirtualSubcats(),
                vxsubcatDerivedType,
                UserProfileUtils.getVarianceFeatureMap(userItem, "vxsubcat_ck"),
                UserProfileUtils.getVarianceFeatureMap(userItem, "st_vxsubcat_ck"));
        retrieveKeyContext.addRetrieveKeys(xDerivedKeys);

        Set<RetrieveKey> tagDerivedKeys = generate(userItem,
                Lists.newArrayList("268"),
                tagDerivedType,
                UserProfileUtils.getVarianceFeatureMap(userItem, "vtag_ck"),
                UserProfileUtils.getVarianceFeatureMap(userItem, "st_vtag_ck"));
        retrieveKeyContext.addRetrieveKeys(tagDerivedKeys);

    }

    private Set<RetrieveKey> generate(UserItem userItem, List<String> catList, RetrieverType retrieverType, Map<String, TVariance> varianceMap, Map<String, TVariance> shortTermVarianceMap) {
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
                            .setAlias(retrieverType.alias())
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
