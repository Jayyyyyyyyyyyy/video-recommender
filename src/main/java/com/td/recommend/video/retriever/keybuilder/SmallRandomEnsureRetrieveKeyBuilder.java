package com.td.recommend.video.retriever.keybuilder;

import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.EnsureVideoStats;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.explorer.Arm;
import com.td.recommend.video.retriever.explorer.MultiArmedBandit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * Created by zjl on 2019/12/11.
 */
public class SmallRandomEnsureRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final RetrieverType derivedType = RetrieverType.svrandom_en;
    private static final int maxSize = 2;
    private static final double minVar = 0.01;
    private static final double minScore = 0.1;
    private static final Logger LOG = LoggerFactory.getLogger(SmallRandomEnsureRetrieveKeyBuilder.class);

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<String> candidates = EnsureVideoStats.getSmallFirstCats();
        Map<String, TVariance> varianceMap = UserProfileUtils.getVarianceFeatureMap(userItem, "vcat_ck");
        Map<String, TVariance> shortTermVarianceMap = UserProfileUtils.getVarianceFeatureMap(userItem, "st_vcat_ck");

        List<Arm> arms = new ArrayList<>();

        for (String candidate : candidates) {
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
                    if (arm.getScore() > minScore || arm.getVariance() > minVar) {
                        return true;
                    } else {
                        LOG.info("en candidate:{} removed because win:{}, loose:{}, diu:{}", arm.getName(), arm.getWin(), arm.getLoose(), userItem.getId());
                        return false;
                    }
                })
                .limit(maxSize)
                .map(arm -> {
                    RetrieveKey retrieveKey = new RetrieveKey();
                    retrieveKey.setType(derivedType.name())
                            .setAlias(derivedType.alias())
                            .setKey(arm.getName())
                            .addAttribute("maxCnt", 5);
                    return retrieveKey;
                }).collect(Collectors.toSet());

        retrieveKeyContext.addRetrieveKeys(keys);
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
