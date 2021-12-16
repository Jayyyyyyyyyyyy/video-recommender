package com.td.recommend.video.rerank.predictor;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.idgenerator.PredictIdGenerator;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.rank.model.ModelType;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.rank.featuredumper.UserNewsFeatureDumperSampler;
import com.td.recommend.video.rank.predictor.*;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by admin on 2017/6/10.
 */
public class FeedRerankPredictor {
    private static final Logger LOG = LoggerFactory.getLogger(FeedRerankPredictor.class);

    private RerankInnerPredictor innerPredictor;
    private VideoRecommenderContext recommendContext;
    String modelName;

    TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();

    public FeedRerankPredictor(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    public List<TaggedItem<PredictItem<DocItem>>> predict(List<TaggedItem<PredictItem<DocItem>>> taggedItems) {
        if (recommendContext.hasBucket("rerank-dnn")) {
            modelName = "dnn";
            innerPredictor = new FeedRerankDNNRerankInnerPredictor(recommendContext);
        } else if (recommendContext.hasBucket("rerank-dnn2")) {
            modelName = "dnn2";
            innerPredictor = new FeedRerankDNNRerankInnerPredictor2(recommendContext);
        } else if (recommendContext.hasBucket("rerank-dnn3")){
            modelName = "dnn3";
            innerPredictor = new FeedRerankDNNRerankInnerPredictor3(recommendContext);
        } else {
            return taggedItems;
        }
        return getPredictResult(taggedItems);
    }

    private List<TaggedItem<PredictItem<DocItem>>> getPredictResult(List<TaggedItem<PredictItem<DocItem>>> taggedItems) {
        try {
            List<TaggedItem<PredictItem<DocItem>>> rerankTaggedItems = innerPredictor.predict(taggedItems);
            recommendContext.setRerankModel(modelName);
            taggedMetricRegistry.histogram("uservideo.rerank.dnnpredict.errrate").update(0);
            return rerankTaggedItems;
        } catch (Exception e) {
            taggedMetricRegistry.histogram("uservideo.rerank.dnnpredict.errrate").update(100);
            LOG.error("feed with rerank dnn predict get empty result", e);
            return taggedItems;
        }
    }
}
