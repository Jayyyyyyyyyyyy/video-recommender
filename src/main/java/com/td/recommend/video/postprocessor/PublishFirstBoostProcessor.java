package com.td.recommend.video.postprocessor;

import com.td.featurestore.item.ItemsProcessor;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by Frang on 2018/8/7.
 */
public class PublishFirstBoostProcessor implements ItemsProcessor<PredictItems<DocItem>> {
    private static final double BOOST_FACTOR = 1.2;

    @Override
    public PredictItems<DocItem> process(PredictItems<DocItem> predictItems) {
        for (PredictItem<DocItem> predictItem : predictItems) {
            boolean shouldBoost = false;
            for (RetrieveKey retrieveKey : predictItem.getRetrieveKeys()) {
                if (RetrieverType.vcat.name().equals(retrieveKey.getType())
                        || RetrieverType.vsubcat.name().equals(retrieveKey.getType())
                        || RetrieverType.vtag.name().equals(retrieveKey.getType())) {
                    shouldBoost = true;
                    break;
                }
            }

            if (shouldBoost) {
                predictItem.setScore(predictItem.getScore() * BOOST_FACTOR);
            }
        }

        predictItems.sort();
        return predictItems;
    }
}
