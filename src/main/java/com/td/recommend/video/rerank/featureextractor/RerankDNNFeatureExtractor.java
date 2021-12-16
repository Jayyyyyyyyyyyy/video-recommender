package com.td.recommend.video.rerank.featureextractor;

import com.alibaba.fastjson.JSONObject;
import com.td.rerank.dnn.felib.*;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;

/**
 * @author zhanghongtao
 */
public interface RerankDNNFeatureExtractor {
    JSONObject extract(PredictItem<DocItem> predictItem, int index, FeatureConfig featureConfig, Vocabulary vocabulary, Buckets buckets, JSONObject docIrrelevantRawFeatures, JSONObject docIrrelevantBucketizedFeatures, VideoRecommenderContext recommendContext);
}
