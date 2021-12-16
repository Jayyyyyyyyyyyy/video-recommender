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
import org.apache.skywalking.apm.toolkit.trace.SupplierWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Created by admin on 2017/6/10.
 */
public class RelevantPredictor implements IPredictor<DocItem> {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantPredictor.class);

    private InnerPredictor innerPredictor;
    private InnerPredictor cvrInnerPredictor;
    private BackupPredictor backupPredictor;
    private VideoRecommenderContext recommendContext;
    private String modelName;
    double cvrWeight;

    private ExecutorService executorService = ApplicationSharedExecutorService.getInstance().getExecutorService();
    TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();

    public RelevantPredictor(VideoRecommenderContext recommendContext, boolean userRemote) {
        this.recommendContext = recommendContext;
        cvrInnerPredictor = new CvrGBDTInnerPredictor(recommendContext, new UserNewsFeatureDumperSampler());
        backupPredictor = new BackupPredictor();

        if (recommendContext.hasBucket("relevant_model-dnn2")) {
            modelName = "rlvt_dnn2";
            innerPredictor = new RelevantDNNInnerPredictor2(recommendContext, new UserNewsFeatureDumperSampler());
        } else {
            modelName = "rlvt_dnn";
            innerPredictor = new RelevantDNNInnerPredictor(recommendContext, new UserNewsFeatureDumperSampler());
        }
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
        predictItems.setRankHost("localhost");
        return getPredictResult(predictItems, queryItems, predictId);
    }

    private PredictResult getPredictResult(PredictItems<DocItem> predictItems, Items queryItems, String predictId) {
        CompletableFuture<Optional<PredictResult>> ctr = CompletableFuture.supplyAsync(SupplierWrapper.of(() -> innerPredictor.predict(predictItems, queryItems, predictId)), executorService);
        try {
            predictItems.setModelName(modelName);
            taggedMetricRegistry.histogram("rlvt.dnnpredict.errrate").update(0);
            return ctr.get().get();
        } catch (Exception e) {
            predictItems.setModelName(ModelType.coec.name());
            taggedMetricRegistry.histogram("rlvt.dnnpredict.errrate").update(100);
            LOG.error("relevant with cvr predict get empty result, use backup predict {}", predictId, e);
            return backupPredictor.predict(predictItems, queryItems);
        }
    }
}
