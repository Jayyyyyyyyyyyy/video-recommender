package com.td.recommend.video.api.thrift;

import com.google.common.collect.ImmutableList;
import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.EnsureVideoStats;
import com.td.recommend.video.retriever.explorer.Arm;
import com.td.recommend.video.retriever.explorer.MultiArmedBandit;
import com.td.recommend.video.retriever.keybuilder.RandomEnsureRetrieveKeyBuilder;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zjl on 2019/12/12.
 */
public class ArmTest {

    private static final double minVar = 0.01;
    private static final double minScore = 0.1;
    private static final int maxSize = 2;
    private static final Logger LOG = LoggerFactory.getLogger(ArmTest.class);

    public static void main(String[] args) {
        UserItem userItem = new UserItemDao().get("861916047758359").get();
        //List<String> candidates = EnsureVideoStats.getFirstCats();
        List<String> candidates = ImmutableList.of("1006","124","198");
        candidates.stream().forEach(x -> System.out.print(x+" "));
        Map<String, TVariance> varianceMap = UserProfileUtils.getVarianceFeatureMap(userItem, "vcat_ck");
        Map<String, TVariance> shortTermVarianceMap = UserProfileUtils.getVarianceFeatureMap(userItem, "st_vcat_ck");
        List<Arm> arms = new ArrayList<>();
//        for (String candidate : candidates) {
//            TVariance variance = varianceMap.get(candidate);
//            TVariance shortVariance = shortTermVarianceMap.get(candidate);
//            double negCnt = getNegCnt(variance, shortVariance);
//            double posCnt = getPosCnt(variance, shortVariance);
//            double missCnt = negCnt - posCnt > 0 ? negCnt - posCnt : 0;
//            Arm arm = new Arm(candidate, posCnt + 1, missCnt + 1);
//            arms.add(arm);
//        }
//        for (int i = 0; i < 120; i++) {
//
//            List<Arm> topArms = MultiArmedBandit.thompsonSampling(arms);
//
//            topArms.stream().filter(arm -> {
//                if (arm.getScore() > minScore || arm.getVariance() > minVar) {
//                    return true;
//                } else {
//                    LOG.info("en candidate:{} removed because win:{}, loose:{}, diu:{}", arm.getName(), arm.getWin(), arm.getLoose(), userItem.getId());
//                    return false;
//                }
//            })
//                    .limit(maxSize).forEach(x -> System.out.println(x.getName()+"--"));
//        }
        for (int i = 0; i < 100; i++) {
            double negCnt = 5;
            double posCnt = i ;
            double missCnt = negCnt - posCnt > 0 ? negCnt - posCnt : 1;
            BetaDistribution betaDistribution = new BetaDistribution(posCnt+1, missCnt+1);
            double score = betaDistribution.sample();
            double variance = betaDistribution.getNumericalVariance();
            System.out.println("posCnt:"+posCnt+"     negCnt:"+negCnt+"      missCnt:"+missCnt+"    variance:"+variance);
        }
        System.out.println("==============================================================================");
        for (int j = 0; j < 11; j++) {
            for (int i = 1; i < 31; i++) {
                double negCnt = i ;
                double posCnt = j;
                double missCnt = negCnt - posCnt > 0 ? negCnt - posCnt : 1;
                BetaDistribution betaDistribution = new BetaDistribution(posCnt+1, missCnt+1);
                double score = betaDistribution.sample();
                double variance = betaDistribution.getNumericalVariance();
                System.out.println("posCnt:"+posCnt+"     negCnt:"+negCnt+"      missCnt:"+missCnt+"    variance:"+variance);
            }

        }

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
