package com.td.recommend.video.rank.predictor;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.datasource.HorseConfigs;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HorseWeightedPredictor implements IPredictor<DocItem> {
    private static final Logger LOG = LoggerFactory.getLogger(HorseWeightedPredictor.class);

    private IPredictor<DocItem> predictor;

    public HorseWeightedPredictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems);
        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> predictItem = items.get(i);
            String vid = predictItem.getId();
            double weight = 1.0;
            Set<String> retrieves = predictItem.getRetrieveKeys().stream().map(RetrieveKey::getType).filter(r -> r.matches("v\\d+_horse")).collect(Collectors.toSet());
            for (String retrieve : retrieves) {
                Map<String, HorseConfigs.Stat> statMap = HorseConfigs.get().get(retrieve).getStats().stream().collect(Collectors.toMap(HorseConfigs.Stat::getVid, h -> h));
                HorseConfigs.Stat stat = statMap.get(vid);
                if (stat != null && stat.getWeight() > 1.0) {
                    weight *= stat.getWeight();
                } else {
                    double click, view;
                    if (stat != null) {
                        click = stat.getTotal_click();
                        view = stat.getTotal_view();
                    } else {
                        click = view = 0.0;
                    }
                    BetaDistribution betaDistribution = new BetaDistribution(click + 1, view - click + 1);
                    weight *= betaDistribution.sample() + 1.0;
                }
            }

            Double weightedScore = predictResult.getScores().get(i) * weight;
            predictResult.getScores().set(i, weightedScore);
            predictItem.addAttribute("horseboost", weight);
        }
        return predictResult;
    }

}
