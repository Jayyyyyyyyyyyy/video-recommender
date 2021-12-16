package com.td.recommend.video.rank.predictor;

import com.td.data.profile.TVariance;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.PosteriorFacetCtrMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NegativeFeedbackWeightedPredictor implements IPredictor<DocItem> {
    private final IPredictor<DocItem> predictor;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(NegativeFeedbackWeightedPredictor.class);

    public NegativeFeedbackWeightedPredictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems);
        UserItem userItem = (UserItem) queryItems.get(ItemKey.user).get();
        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> item = items.get(i);
            Double weight = computeWeight(userItem, item.getItem());
            double weightedScore = predictResult.getScores().get(i) * weight;
            predictResult.getScores().set(i, weightedScore);
            item.addAttribute("negboost", weight);
        }
        return predictResult;
    }

    public static Double computeWeight(UserItem userItem, DocItem item) {
        HashMap<String, Pair<String, Double>> docFacetIdMap = new HashMap<>();
        docFacetIdMap.put("vcat", Pair.of(DocProfileUtils.getFirstCat(item), 3.0));
//        docFacetIdMap.put("vsubcat", Pair.of(DocProfileUtils.getSecondCat(item), 1.5));
        docFacetIdMap.put("vauthor_uid", Pair.of(DocProfileUtils.getUid(item), 3.0));
        if (StringUtils.isNotBlank(DocProfileUtils.getMp3(item))) {
            docFacetIdMap.put("vmp3", Pair.of(DocProfileUtils.getMp3(item), 3.0));
        }
        if (DocProfileUtils.getExerciseBody(item).size() > 0) {
            docFacetIdMap.put("vexercise_body", Pair.of(DocProfileUtils.getExerciseBody(item).get(0), 3.0));
        }
//        if (DocProfileUtils.getTags(item).size() > 0) {
//            docFacetIdMap.put("vtag", Pair.of(DocProfileUtils.getTags(item).get(0), 2.0));
//        }
        double weightSum = 0.0;
        double scaleSum = 0;
        HashMap<String, Double> debugs = new LinkedHashMap<>();
        debugs.put(item.getId() + ":" + DocProfileUtils.getTitle(item), 1.0);
        for (Map.Entry<String, Pair<String, Double>> entry : docFacetIdMap.entrySet()) {
            String facet = entry.getKey();
            String id = entry.getValue().getLeft();
            Double scale = entry.getValue().getRight();
            Map<String, TVariance> stFeatureMap = UserProfileUtils.getVarianceFeatureMap(userItem, "st_" + facet + "_ck");
            TVariance tVariance = stFeatureMap.get(id);
            if (tVariance == null) {
                tVariance = new TVariance();
                tVariance.setNegCnt(0);
                tVariance.setPosCnt(0);
            }
            double ectr = new BetaDistribution(tVariance.getPosCnt() + 1, tVariance.getNegCnt() - tVariance.getPosCnt() + 1).sample();
            double posteriorCtr = PosteriorFacetCtrMap.get(facet).getOrDefault(id, 0.1);
            double facetWeight = Math.tanh(ectr / posteriorCtr);
            weightSum += scale * facetWeight;
            scaleSum += scale;
            debugs.put(facet + "_" + id, facetWeight);
        }
        if (scaleSum > 0) {
            debugs.put("all", weightSum / scaleSum);
//            log.info("negboost:{},diu:{}", debugs, userItem.getId());
            return weightSum / scaleSum;
        }
        return 1.0;
    }

    public static void main(String[] args) {
        Map<String, Double> weightMap = new HashMap<>();
        String stFacet = "st_vcat_ck";
        UserItem userItem = new UserItemDao().get("80E49198-188A-4F07-877B-A0E8D19B22E8").get();
        Map<String, TVariance> stFeatureMap = UserProfileUtils.getVarianceFeatureMap(userItem, stFacet);
        BetaDistribution betaDistribution = new BetaDistribution(8, -9);
        System.out.println(betaDistribution.sample());
    }
}
