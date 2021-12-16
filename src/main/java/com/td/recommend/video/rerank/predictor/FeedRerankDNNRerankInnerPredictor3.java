package com.td.recommend.video.rerank.predictor;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.Maps;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.codahale.metrics.Timer.Context;

public class FeedRerankDNNRerankInnerPredictor3 implements RerankInnerPredictor {
    private static final Logger LOG = LoggerFactory.getLogger(FeedRerankDNNRerankInnerPredictor3.class);

    private final VideoRecommenderContext recommendContext;

    public FeedRerankDNNRerankInnerPredictor3(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public List<TaggedItem<PredictItem<DocItem>>> predict(List<TaggedItem<PredictItem<DocItem>>> taggedItems) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        taggedMetricRegistry.meter("video-recommend.rerank.dnn3.predict.qps").mark();
        Context predictTime = null;
        if (!recommendContext.isDebug()) {
            predictTime = taggedMetricRegistry.timer("video-recommend.rerank.dnn3.predict.latency").time();
        }

        int topN = 12;
        int videoSize = Math.min(taggedItems.size(), topN);

        Map<Integer, TaggedItem<PredictItem<DocItem>>> taggedItemMap = Maps.newLinkedHashMap();
        for (int i = 0; i < videoSize; i++) {
            TaggedItem<PredictItem<DocItem>> predictItemTaggedItem = taggedItems.get(i);
            PredictItem<DocItem> item = predictItemTaggedItem.getItem();
            item.setRerankScore(item.getScore());
            item.setRerankPredictScore(item.getPredictScore());
            List<RetrieveKey> retrieveKeys = item.getRetrieveKeys();
            List<RetrieveKey> collect = retrieveKeys.stream().filter(retrieveKey -> {
                String type = retrieveKey.getType();
                return "vself".equals(type) || type.endsWith("_tr") || type.endsWith("_eu");
            }).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                taggedItemMap.put(i, predictItemTaggedItem);
            }
        }
        taggedItems.sort((o1, o2) -> -Double.compare(o1.getItem().getRerankScore(), o2.getItem().getRerankScore()));
        for (TaggedItem<PredictItem<DocItem>> value : taggedItemMap.values()) {
            taggedItems.remove(value);
        }
        taggedItemMap.forEach(taggedItems::add);

        if (!recommendContext.isDebug()) {
            predictTime.stop();
        }
        return taggedItems;
    }
}
