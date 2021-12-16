package com.td.recommend.video.rank.predictor;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.idgenerator.PredictIdGenerator;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.rank.model.ModelType;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.concurrent.ApplicationSharedExecutorService;
import com.td.recommend.video.rank.featuredumper.UserNewsFeatureDumperSampler;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Created by admin on 2017/6/10.
 */
public class FeedPredictor implements IPredictor<DocItem> {
    private static final Logger LOG = LoggerFactory.getLogger(FeedPredictor.class);

    private InnerPredictor innerPredictor;
    private BackupPredictor backupPredictor;
    private VideoRecommenderContext recommendContext;
    String modelName;

    private ExecutorService executorService = ApplicationSharedExecutorService.getInstance().getExecutorService();
    TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();

    public FeedPredictor(VideoRecommenderContext recommendContext, boolean userRemote) {
        this.recommendContext = recommendContext;
        backupPredictor = new BackupPredictor();
    }

    private String generatePredictId(VideoRecommenderContext videoRecommenderContext) {
        String version = videoRecommenderContext.getRecommendRequest().getVersion();
        String diu = videoRecommenderContext.getRecommendRequest().getDiu();
        if (version.compareTo("6.8.6.121622") > 0) {
            return PredictIdGenerator.getInstance().generateNew("");
        } else {
            return PredictIdGenerator.getInstance().generate(diu);
        }
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        String predictId = generatePredictId(recommendContext);
        if (predictItems.isEmpty()) {
            PredictResult predictResult = new PredictResult();
            predictResult.setPredictId(predictId);
            predictResult.setScores(Collections.emptyList());
            return predictResult;
        }
        if (recommendContext.hasBucket("model-dnn")) {
            modelName = "dnn";
            innerPredictor = new FeedDNNInnerPredictor(recommendContext, new UserNewsFeatureDumperSampler());
        } else if (recommendContext.hasBucket("model-dnn2")) {
            modelName = "dnn2";
            innerPredictor = new FeedDNNInnerPredictor2(recommendContext, new UserNewsFeatureDumperSampler());
        } else {
            modelName = "dnn3";
            innerPredictor = new FeedDNNInnerPredictor3(recommendContext, new UserNewsFeatureDumperSampler());
        }
        predictItems.setRankHost("localhost");
        return getPredictResult(predictItems, queryItems, predictId);
    }

    private PredictResult getPredictResult(PredictItems<DocItem> predictItems, Items queryItems, String predictId) {
        try {
            Optional<PredictResult> ctr = innerPredictor.predict(predictItems, queryItems, predictId);
            predictItems.setModelName(modelName);
            PredictResult predictResult = ctr.get();
            taggedMetricRegistry.histogram("uservideo.dnnpredict.errrate").update(0);
            return predictResult;
        } catch (Exception e) {
            predictItems.setModelName(ModelType.coec.name());
            taggedMetricRegistry.histogram("uservideo.dnnpredict.errrate").update(100);
            LOG.error("feed with dnn predict get empty result, use backup predict {}", predictId, e);
            return backupPredictor.predict(predictItems, queryItems);
        }
    }
}
