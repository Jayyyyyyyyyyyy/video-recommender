package com.td.recommend.video.retriever.keybuilder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.explorer.Arm;
import com.td.recommend.video.retriever.explorer.MultiArmedBandit;
import com.td.recommend.video.utils.InterestUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Liujikun on 2019/10/17.
 */
public class PriorityEuRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(PriorityEuRetrieveKeyBuilder.class);
    private VideoRecommenderContext recommendContext;
    private static final int maxSize = 1;
    private double minVar = 0.01;
    private double minVar_special = 0.015;
    private static final Map<String, String> targetInterestGroup = ImmutableMap.<String, String>builder()
            //+:有兴趣，-：无兴趣，","之间是"或"的关系
            .put("1034", "vsubcat:+300,+285")//对300或285有兴趣则试探1034
            .put("1010", "vcat:-1006")//对1006无兴趣则试探1010
            .put("1705", "vcat:-1006")
            .put("305", "vcat:-1006")
            .put("306", "vcat:-1006")
            .put("1094", "vcat:-83")
            .put("4113", "vsubcat:+1009")
            .put("1053", "vsubcat:+1009")
            .build();

    public PriorityEuRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<RetrieverType.Eu> euList = Arrays.stream(RetrieverType.Eu.values()).collect(Collectors.toList());
        Set<RetrieveKey> retrieveKeys = generate(userItem, euList);
        retrieveKeyContext.addRetrieveKeys(retrieveKeys);
    }

    private Set<RetrieveKey> generate(UserItem userItem, List<RetrieverType.Eu> euList) {
        List<Arm> arms = new ArrayList<>();
        for (RetrieverType.Eu candidate : euList) {
            TVariance variance = UserProfileUtils.getVarianceFeatureMap(userItem, candidate.getFacet()).get(candidate.getKey());
            TVariance shortVariance = UserProfileUtils.getVarianceFeatureMap(userItem, "st_" + candidate.getFacet()).get(candidate.getKey());
            String group = targetInterestGroup.get(candidate.getKey());
            if (group == null) {
                add2ArmList(candidate.getKey(), arms, variance, shortVariance);
            } else {
                String facet = group.split(":")[0].trim();
                String[] keys = group.split(":")[1].split(",");
                boolean shouldExplore = false;
                for (String key : keys) {
                    boolean isStrong = InterestUtils.isStrongInterest(userItem, key.trim().substring(1).trim(), ImmutablePair.of(facet + "_ck", facet + "_cs"));
                    shouldExplore |= key.trim().startsWith("+") == isStrong;
                }
                if (shouldExplore) {
                    add2ArmList(candidate.getKey(), arms, variance, shortVariance);
                } else {
                    LOG.info("eu candidate:{} removed because group not match, diu:{}", candidate.getKey(), userItem.getId());
                }
            }
        }

        List<Arm> topArms = MultiArmedBandit.thompsonSampling(arms);
        Map<String, RetrieverType.Eu> keyToType = euList.stream().collect(Collectors.toMap(RetrieverType.Eu::getKey, k -> k));
        Set<RetrieveKey> keys = topArms.stream()
                .filter(arm -> {
                    if (ImmutableSet.of("1705", "1588", "4113", "1053").contains(arm.getName())) {
                        if (arm.getVariance() > minVar_special) {
                            return true;
                        }
                    } else {
                        if (arm.getVariance() > minVar) {
                            return true;
                        }
                    }
                    LOG.info("eu candidate:{} removed because win:{}, loose:{}, diu:{}", arm.getName(), arm.getWin(), arm.getLoose(), userItem.getId());
                    return false;
                })
                .sorted(Comparator.comparing(Arm::getScore).reversed())
                .limit(maxSize)
                .map(arm -> {
                    RetrieveKey retrieveKey = new RetrieveKey();
                    retrieveKey.setType(keyToType.get(arm.getName()).name())
                            .setAlias(keyToType.get(arm.getName()).getAlias())
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

    public static void add2ArmList(String facet, List arms, TVariance variance, TVariance shortVariance) {
        double negCnt = getNegCnt(variance, shortVariance);
        double posCnt = getPosCnt(variance, shortVariance);
        double missCnt = negCnt - posCnt > 0 ? negCnt - posCnt : 0;
        Arm arm = new Arm(facet, posCnt + 1, missCnt + 1);
        arms.add(arm);
    }
}
