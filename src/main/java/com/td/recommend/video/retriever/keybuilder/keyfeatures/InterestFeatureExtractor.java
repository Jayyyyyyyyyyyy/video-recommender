package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.explorer.*;
import com.td.recommend.video.retriever.feedback.InterestNegativeFeedback;
import com.td.recommend.video.utils.WilsonInterval;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by admin on 2017/6/20.
 */
public class InterestFeatureExtractor {
    @Getter
    private Map<RetrieveKey, Map<String, Double>> keyFeaturesMap;
    private VideoRecommenderContext recommendContext;
    private RetrieverType retrieverType;
    private static final double wilsonLimit = 0.1;
    private static final double wilsonConfidence = 0.8;
    private static final int defaultCnt = InterestNegativeFeedback.maxCount;
    private static Logger log = LoggerFactory.getLogger(InterestFeatureExtractor.class);
    private Map<String, Integer> negativeRetrieveKeyNumberMap;
    private static final Set<String> invalidTags = new HashSet<>(Arrays.asList("82,107,111,123,139,152,157,161,172,197,202,208,228,235,249,263,272,278,283,292,299,304,311,321,325,328,329,340,349,355,359,365,941,963,971,978,982,989,993,1030,1049,1093,1100,1739,2172,2176,2180,2184,4207".split(",")));

    public InterestFeatureExtractor(VideoRecommenderContext recommendContext,
                                    RetrieverType retrieverType) {
        this.recommendContext = recommendContext;
        this.keyFeaturesMap = new HashMap<>();
        this.retrieverType = retrieverType;
    }

    public InterestFeatureExtractor(VideoRecommenderContext recommendContext,
                                    Map<RetrieveKey, Map<String, Double>> keyFeaturesMap,
                                    RetrieverType retrieverType) {
        this.recommendContext = recommendContext;
        this.keyFeaturesMap = keyFeaturesMap;
        this.retrieverType = retrieverType;
    }

    public void extract(List<String> facets) {
        extractL2Map(facets);
        extractL2VarianceMap(facets);
        extractL2SValueMap(facets);
    }

    public void extractL2VarianceMap(List<String> interestFacets) {
        UserItem userItem = recommendContext.getUserItem();
        for (String interestFacet : interestFacets) {
            negativeRetrieveKeyNumberMap = InterestNegativeFeedback.compute(userItem, interestFacet);
            Map<String, TVariance> featureMap = UserProfileUtils.getVarianceFeatureMap(userItem, interestFacet);
            for (Map.Entry<String, TVariance> entry : featureMap.entrySet()) {
                addVarFeature(entry, interestFacet);
            }
        }
    }

    public void extractL2SValueMap(List<String> interestFacets) {
        UserItem userItem = recommendContext.getUserItem();
        for (String interestFacet : interestFacets) {
            negativeRetrieveKeyNumberMap = InterestNegativeFeedback.compute(userItem, interestFacet);
            Map<String, String> featureMap = UserProfileUtils.getSValueFeaturesMap(userItem, interestFacet);
            for (Map.Entry<String, String> entry : featureMap.entrySet()) {
                addSValueFeature(entry, interestFacet);
            }
        }
    }

    public void extractL2Map(List<String> interestFacets) {
        UserItem userItem = recommendContext.getUserItem();
        for (String interestFacet : interestFacets) {
            negativeRetrieveKeyNumberMap = InterestNegativeFeedback.compute(userItem, interestFacet);
            Map<String, Double> featureMap = UserProfileUtils.getValueFeaturesMap(userItem, interestFacet);
            for (Map.Entry<String, Double> entry : featureMap.entrySet()) {
                addValueFeature(entry, interestFacet);
            }
        }
    }

    private boolean isStrongInterest(Map.Entry<String, TVariance> entry, String facetName) {
        TVariance var = entry.getValue();
        if (invalidTags.contains(entry.getKey())) {//名为 "其他" 的标签id
            return false;
        }
        double varLimit, meanLimit;
        if (facetName.startsWith("st_")) {
            double wilsonScore = WilsonInterval.lowerBound(var.getPosCnt(), var.getNegCnt(), wilsonConfidence);
            return wilsonScore > wilsonLimit * 0.8;
        } else {
            varLimit = 0.9;
            meanLimit = facetName.endsWith("_cs") ? 0.2 : 0.1;
            return var.getVariance() < varLimit && var.getMean() > meanLimit;
        }
    }

    private Level interestLevel(Map.Entry<String, TVariance> entry, String facetName) {
        TVariance var = entry.getValue();
        if (invalidTags.contains(entry.getKey())) {//名为 "其他" 的标签id
            return Level.NONE;
        }
        double varLimit, meanLimit;
        if (facetName.startsWith("st_")) {
            double wilsonScore = WilsonInterval.lowerBound(var.getPosCnt(), var.getNegCnt(), wilsonConfidence);
            if (wilsonScore > wilsonLimit * 1.2) {
                return Level.STRONG;
            } else if (wilsonScore > wilsonLimit * 0.8) {
                return Level.WEAK;
            }
        } else {
            varLimit = 0.9;
            meanLimit = facetName.endsWith("_cs") ? 0.2 : 0.1;
            if (var.getVariance() < varLimit * 0.8 && var.getMean() > meanLimit * 1.5) {
                return Level.STRONG;
            } else if (var.getVariance() < varLimit && var.getMean() > meanLimit) {
                return Level.WEAK;
            }
        }
        return Level.NONE;
    }

    private boolean isStrongInterestExp(Map.Entry<String, TVariance> entry, String facetName) {
        TVariance var = entry.getValue();
        if (facetName.startsWith("st_")) {
            double wilsonScore = WilsonInterval.lowerBound(var.getPosCnt(), var.getNegCnt(), wilsonConfidence);
            return wilsonScore > wilsonLimit * 0.8;
        } else {
            return var.getPosCnt() > 1 && var.getMean() > 0.1;
        }
    }

    private void addVarFeature(Map.Entry<String, TVariance> entry, String facetName) {
        Integer blendNum = negativeRetrieveKeyNumberMap.getOrDefault(entry.getKey(), defaultCnt);
        Level interestLevel = interestLevel(entry, facetName);
        if (interestLevel != Level.NONE) {
            if (!recommendContext.hasBucket("interest-exp")) {
                addInterestKeys(entry, facetName, blendNum, interestLevel);
            }
            if (recommendContext.hasBucket("vlive-yes")) {
                LiveRetrieveKeyBuilder.build(retrieverType, entry.getKey(), blendNum, keyFeaturesMap);
            }
            if (recommendContext.hasBucket("fresh_mp3-yes")) {
                FreshMp3RetrieveKeyBuilder.build(retrieverType, entry.getKey(), blendNum, keyFeaturesMap);
            }
            if (recommendContext.getUserType() == UserProfileUtils.UserType.old_interest) {

                ExploreVideoRetrieveKeyBuilder.build(recommendContext, retrieverType, entry.getKey(), blendNum, keyFeaturesMap);
                InterestFreshRetrieveKeyBuilder.build(recommendContext, retrieverType, entry.getKey(), blendNum, keyFeaturesMap);
                if(!Ihf.isTrend(recommendContext.getRecommendRequest().getIhf())){
                    EnsureRetrieveKeyBuilder.build(retrieverType, entry.getKey(), blendNum, keyFeaturesMap);
                }
                if (recommendContext.hasBucket("teaching_research-yes")) {
                    TeachingResearchRetrieveKeyBuilder.build(retrieverType, entry.getKey(), blendNum, keyFeaturesMap);
                }
                if (recommendContext.hasBucket("titleresearch-yes")) {
                    TitleResearchRetrieveKeyBuilder.build(retrieverType, entry.getKey(), blendNum, keyFeaturesMap);
                }
                RecomeRetrieveKeyBuilder.build(retrieverType, entry.getKey(), blendNum, keyFeaturesMap);

                if (recommendContext.hasBucket("operator_interest-yes")) {
                    OperatorInterestRetrieveKeyBuilder.build(retrieverType, entry.getKey(), blendNum, keyFeaturesMap);
                }
            }
        }
    }

    private void addInterestKeys(Map.Entry<String, TVariance> entry, String facetName, Integer blendNum, Level interestLevel) {
        if (blendNum <= 0) {
            return;
        }
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(retrieverType.name());
        retrieveKey.setAlias(retrieverType.alias());
        retrieveKey.setKey(entry.getKey());
        retrieveKey.addAttribute("interestLevel", interestLevel);
        retrieveKey.addAttribute("maxCnt", blendNum);
        retrieveKey.setIhf(String.valueOf(recommendContext.getRecommendRequest().getIhf()));
        String bucket = recommendContext.hasBucket("interest-exp") ? "interest-exp" : "";
        retrieveKey.setPlaceholder(bucket);
        Map<String, Double> featureMap = keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());
        TVariance var = entry.getValue();
        featureMap.put(facetName, var.getMean());
        featureMap.put(facetName + "_poscnt", var.getPosCnt());
        featureMap.put(facetName + "_negcnt", var.getNegCnt());
        featureMap.put(facetName + "_variance", var.getVariance());
    }

    private void addSValueFeature(Map.Entry<String, String> entry, String facetName) {

        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(retrieverType.name());
        retrieveKey.setAlias(retrieverType.alias());
        retrieveKey.setKey(entry.getKey());

        Integer blendNum = negativeRetrieveKeyNumberMap.getOrDefault(entry.getKey(), defaultCnt);
        if (blendNum > 0) {
            retrieveKey.addAttribute("maxCnt", blendNum);
            keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());
        } else {
            log.info("retrieve {}_{} filtered because maxCnt = 0", retrieverType.name(), entry.getKey());
        }
    }

    private void addValueFeature(Map.Entry<String, Double> entry, String facetName) {
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(retrieverType.name());
        retrieveKey.setAlias(retrieverType.alias());
        retrieveKey.setKey(entry.getKey());
        Integer blendNum = negativeRetrieveKeyNumberMap.getOrDefault(entry.getKey(), defaultCnt);
        FreshMp3RetrieveKeyBuilder.build(retrieverType, entry.getKey(), blendNum, keyFeaturesMap);
        if (blendNum > 0) {
            retrieveKey.addAttribute("maxCnt", blendNum);
            Map<String, Double> featureMap = keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());
            featureMap.put(facetName, entry.getValue());
        } else {
            log.info("retrieve {}_{} filtered because maxCnt = 0", retrieverType.name(), entry.getKey());
        }
    }

    public enum Level {
        NONE, WEAK, STRONG;
    }

    public static void main(String[] args) {
        System.out.println(WilsonInterval.lowerBound(2, 10.47, 0.8));
    }
}
