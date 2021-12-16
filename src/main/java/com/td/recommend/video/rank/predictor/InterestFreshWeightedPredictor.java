package com.td.recommend.video.rank.predictor;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.datasource.FrConfigs;

import java.util.List;

public class InterestFreshWeightedPredictor implements IPredictor<DocItem> {
    private final IPredictor<DocItem> predictor;

    public InterestFreshWeightedPredictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems);

        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> item = items.get(i);
            if (item.getRetrieveKeys().stream().map(RetrieveKey::getType).anyMatch(k -> k.endsWith("_fr"))) {
                double weight = 1.0;
                String secondCat = DocProfileUtils.getSecondCat(item.getItem());
                double feedShowNum = DocProfileUtils.getFeedShowNum(item.getItem());
                double feedCtr = DocProfileUtils.getFeedCtr(item.getItem());
                double feedWellUsedRate = DocProfileUtils.getFeedWellUsedRate(item.getItem());

                FrConfigs.Conf fr = FrConfigs.get().getOrDefault(secondCat, new FrConfigs.Conf("default", 0.1, 1.0, 0.01, 1.0));
                if (feedShowNum >= 5000 && feedCtr > fr.getCtr() && feedCtr < 1.0 && feedWellUsedRate > fr.getWur() && feedWellUsedRate < 1.0) {
                    weight = fr.getWeight2();
                } else if (feedShowNum >= 2000 && feedCtr > fr.getCtr() && feedCtr < 1.0) {
                    weight = fr.getWeight1();
                }
                double weightedScore = predictResult.getScores().get(i) * weight;
                predictResult.getScores().set(i, weightedScore);
                item.addAttribute("frboost", weight);
            }
        }
        return predictResult;
    }
}
