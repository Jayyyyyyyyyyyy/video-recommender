package com.td.recommend.video.rank.predictor;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.datasource.EvConfigs;

import java.util.List;

public class ExploreVideoWeightedPredictor implements IPredictor<DocItem> {
    private final IPredictor<DocItem> predictor;
    private final int[] starShowLimit = {0, 0, 0, 50, 500, 1000, 1000, 1000, 2000, 2000};

    public ExploreVideoWeightedPredictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems); //打分
        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> item = items.get(i);
            if (item.getRetrieveKeys().stream().map(RetrieveKey::getType).anyMatch(k -> k.endsWith("_ev"))) {
                double weight = 1.0;
                String secondCat = DocProfileUtils.getSecondCat(item.getItem());
                double feedShowNum = DocProfileUtils.getFeedShowNum(item.getItem());
                double feedCtr = DocProfileUtils.getFeedCtr(item.getItem());
                double feedWellUsedRate = DocProfileUtils.getFeedWellUsedRate(item.getItem());
                int talentStar = Math.min(9, DocProfileUtils.getTalentStar(item.getItem()));
                EvConfigs.Conf ev = EvConfigs.get().getOrDefault(secondCat, new EvConfigs.Conf("default", 0.1, 0.1, 1.0)); // 权重配置，siying写的一些
                if (feedShowNum > starShowLimit[talentStar] && feedShowNum < 3000 && feedCtr > ev.getCtr2() && feedCtr < 1.0 && feedWellUsedRate > 0.1 && feedWellUsedRate < 1.0) {
                    weight = ev.getWeight();
                }

                double weightedScore = predictResult.getScores().get(i) * weight;
                predictResult.getScores().set(i, weightedScore);
                item.addAttribute("evboost", weight);
            }
        }
        return predictResult;
    }

}
