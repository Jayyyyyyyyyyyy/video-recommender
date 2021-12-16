package com.td.recommend.video.rank.predictor;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.datasource.BoostWeightMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RelevantHorseWeightedPredictor implements IPredictor<DocItem> {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantHorseWeightedPredictor.class);

    private IPredictor<DocItem> predictor;

    public RelevantHorseWeightedPredictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems);

        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> predictItem = items.get(i);
            if (predictItem.getRetrieveKeys().stream().map(RetrieveKey::getType).anyMatch(k -> k.endsWith("vtalentmp3_rlvt"))
                    && BoostWeightMap.getHorseUidSet().contains(DocProfileUtils.getUid(predictItem.getItem()))) {
                Double weightedScore = predictResult.getScores().get(i) * 10.0;
                predictResult.getScores().set(i, weightedScore);
                predictItem.addAttribute("vmp3boost", 10.0);
            }
        }
        return predictResult;
    }

}
