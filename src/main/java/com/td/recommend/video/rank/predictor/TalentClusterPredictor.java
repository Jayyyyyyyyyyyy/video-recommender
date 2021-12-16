package com.td.recommend.video.rank.predictor;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;

import java.util.List;

public class TalentClusterPredictor implements IPredictor<DocItem> {
    private IPredictor<DocItem> predictor;

    public TalentClusterPredictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems);
        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> predictItem = items.get(i);

            if (predictItem.getRetrieveKeys().stream().map(RetrieveKey::getType).anyMatch(k -> k.equals("vtalentcluster"))){
                Double weightedScore = predictResult.getScores().get(i) * 1.3;
                predictResult.getScores().set(i, weightedScore);
                predictItem.addAttribute("vtalentclusterboost", 1.3);

            }
        }
        return predictResult;
    }
}
