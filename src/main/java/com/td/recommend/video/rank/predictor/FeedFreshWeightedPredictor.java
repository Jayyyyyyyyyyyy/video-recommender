package com.td.recommend.video.rank.predictor;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class FeedFreshWeightedPredictor implements IPredictor<DocItem> {
    private IPredictor<DocItem> predictor;

    public FeedFreshWeightedPredictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems);

        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> predictItem = items.get(i);
            if (DocProfileUtils.getFirstCat(predictItem.getItem()).equals("264")) {
                double weight = freshWeight(predictItem);
                Double weightedScore = predictResult.getScores().get(i) * weight;
                predictResult.getScores().set(i, weightedScore);
                predictItem.addAttribute("feed_fresh_boost", weight);
            }
        }
        return predictResult;
    }

    private double freshWeight(PredictItem<DocItem> predictItem) {
        LocalDateTime createDateTime = DocProfileUtils.getCreateDateTime(predictItem.getItem());
        LocalDateTime now = LocalDateTime.now();
        long diff = ChronoUnit.WEEKS.between(createDateTime, now);
        return Math.pow(0.99, diff);
    }
}
