package com.td.recommend.video.rerank.predictor;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;

import java.util.List;
import java.util.Optional;

/**
 * Created by admin on 2017/8/3.
 */
public interface RerankInnerPredictor {
    List<TaggedItem<PredictItem<DocItem>>> predict(List<TaggedItem<PredictItem<DocItem>>> taggedItems);
}
