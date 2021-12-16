package com.td.recommend.video.rank.predictor;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.datasource.BoostWeightMap;

import java.util.List;

public class TalentWeightedPredictor implements IPredictor<DocItem> {
    private IPredictor<DocItem> predictor;

    public TalentWeightedPredictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems);

        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> predictItem = items.get(i);
            String uid = DocProfileUtils.getUid(predictItem.getItem());
            double weight = BoostWeightMap.getTalentUidMap().getOrDefault(uid, 1.0);
            double weight2 = BoostWeightMap.getTalentUids_lt3().getOrDefault(uid,1.0);
            Double weightedScore = predictResult.getScores().get(i) * weight * weight2;
            predictResult.getScores().set(i, weightedScore);
            predictItem.addAttribute("talentboost", weight);
        }
        return predictResult;
    }
}
