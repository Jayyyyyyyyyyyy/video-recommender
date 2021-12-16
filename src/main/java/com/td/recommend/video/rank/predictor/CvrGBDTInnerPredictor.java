package com.td.recommend.video.rank.predictor;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.featurestore.feature.IFeature;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.rank.model.PredictModel;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.thread.BatchTaskExecutor;
import com.td.recommend.commons.thread.BatchTaskExecutor.TaskFactory;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.concurrent.ApplicationSharedExecutorService;
import com.td.recommend.video.rank.featuredumper.FeatureDumperSampler;
import com.td.recommend.video.rank.featureextractor.GBDTFeatureExtractor;
import com.td.recommend.video.rank.featureextractor.FeedGBDTFeatureExtractor;
import com.td.recommend.video.rank.featureextractor.RelevantGBDTFeatureExtractor;
import com.td.recommend.video.rank.model.ReloadableGBDTBucketModel;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static com.codahale.metrics.Timer.Context;

/**
 * Created by admin on 2017/8/3.
 */
public class CvrGBDTInnerPredictor implements InnerPredictor {
    private static final Logger LOG = LoggerFactory.getLogger(CvrGBDTInnerPredictor.class);

    //    private final PredictModel bucketPredictModel;
    private final FeatureDumperSampler featureDumperSampler;
    private final VideoRecommenderContext recommendContext;
    private final ReloadableGBDTBucketModel bucketPredictModel;
    private BatchTaskExecutor<PredictItem<DocItem>, PredictTaskItem> batchTaskExecutor;
    private int predictTimeoutMs;
    private GBDTFeatureExtractor featureExtractor = FeedGBDTFeatureExtractor.getInstance();

    public CvrGBDTInnerPredictor(VideoRecommenderContext recommendContext, FeatureDumperSampler featureDumperSampler, int predictTimeoutMs) {
        this.recommendContext = recommendContext;
//        this.bucketPredictModel = ReloadableGBDTModel.getInstance().getGBDTModel();
//      this.bucketPredictModel = ReloadableHDFSBucketModel.getInstance().getGBDTModel("default");
        bucketPredictModel = ReloadableGBDTBucketModel.getInstance();
        this.featureDumperSampler = featureDumperSampler;
        this.batchTaskExecutor = new BatchTaskExecutor<>(ApplicationSharedExecutorService.getInstance().getExecutorService(), 50);
        this.predictTimeoutMs = predictTimeoutMs;
    }

    public CvrGBDTInnerPredictor(VideoRecommenderContext recommendContext, FeatureDumperSampler featureDumperSampler) {
        this(recommendContext, featureDumperSampler, 200);
    }

    public void setPredictTimeoutMs(int predictTimeoutMs) {
        this.predictTimeoutMs = predictTimeoutMs;
    }

    @Override
    public Optional<PredictResult> predict(PredictItems<DocItem> predictItems, Items queryItems, String predictId) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Context predictTime = taggedMetricRegistry.timer("usernews.localpredict.latency").time();

//        boolean needDump = featureDumperSampler.needDump(recommendContext);
        boolean needDump = false;

        PredictModel predictModel_cvr;
        int ihf = recommendContext.getRecommendRequest().getIhf();
        if (Ihf.isRelevant(ihf)) {
            featureExtractor = RelevantGBDTFeatureExtractor.getInstance();
            predictModel_cvr = bucketPredictModel.getGBDTModel("cvr_model-rlvt");
        } else {//feed
            featureExtractor = FeedGBDTFeatureExtractor.getInstance();
            predictModel_cvr = bucketPredictModel.getGBDTModel("cvr_model-exp");
        }
//        if (recommendContext.hasBucket(BucketConstants.MODEL_EXP)) {
//            predictModel = bucketPredictModel.getGBDTModel(BucketConstants.MODEL_EXP);
//        } else {
//            predictModel = bucketPredictModel.getGBDTModel(BucketConstants.MODEL_BASE);
//        }

        PredictModel finalPredictModel = predictModel_cvr;
        PredictModel finalPredictModel_cvr = predictModel_cvr;
        TaskFactory<PredictItem<DocItem>, PredictTaskItem> taskFactory = items ->
                new PredictTask(finalPredictModel, finalPredictModel_cvr, queryItems, recommendContext, items, featureExtractor);

        List<Future<List<PredictTaskItem>>> resultFutureList = batchTaskExecutor.submit(predictItems.getItems(), taskFactory, predictTimeoutMs);

        Map<String, ImmutablePair<Double, Double>> scoreMap = new HashMap<>();

        for (Future<List<PredictTaskItem>> listFuture : resultFutureList) {
            if (!listFuture.isCancelled()) {
                try {
                    List<PredictTaskItem> predictTaskItems = listFuture.get();
                    for (PredictTaskItem predictTaskItem : predictTaskItems) {
                        scoreMap.put(predictTaskItem.getDocId(), ImmutablePair.of(predictTaskItem.getScore(), predictTaskItem.getPredictScore()));
                    }
                    taggedMetricRegistry.histogram("usernews.localpredict.taskfailrate").update(0);
                } catch (InterruptedException e) {
                    taggedMetricRegistry.histogram("usernews.localpredict.taskfailrate").update(100);
                    LOG.error("batch predict is interrupted!", e);
                } catch (ExecutionException e) {
                    taggedMetricRegistry.histogram("usernews.localpredict.taskfailrate").update(100);
                    LOG.error("batch predict execution failed!", e);
                }
            } else {
                taggedMetricRegistry.histogram("usernews.localpredict.taskfailrate").update(100);
                LOG.error("batch predict is canceled!");
            }
        }

        if (scoreMap.isEmpty()) {//if all failed
            return Optional.empty();
        }

        if (scoreMap.size() < predictItems.getItems().size()) {
            taggedMetricRegistry.histogram("usernews.localpredict.lessrate").update(100);
        } else {
            taggedMetricRegistry.histogram("usernews.localpredict.lessrate").update(0);
        }

        List<Double> scoreList = new ArrayList<>();
        List<Double> predictScoreList = new ArrayList<>();
        for (PredictItem<DocItem> predictItem : predictItems) {
            ImmutablePair<Double, Double> scorePair = scoreMap.get(predictItem.getId());
            if (scorePair == null) {
                scorePair = new ImmutablePair<>(0.0, 0.0);
            }
            scoreList.add(scorePair.getLeft());
            predictScoreList.add(scorePair.getRight());

        }

        PredictResult predictResult = new PredictResult();
        predictResult.setPredictId(predictId);
        predictResult.setScores(scoreList);
        predictResult.setPredictScores(predictScoreList);

        predictTime.stop();
        return Optional.of(predictResult);
    }


    static class PredictTask implements Callable<List<PredictTaskItem>> {
        private PredictModel predictModel;
        private PredictModel predictModel_cvr;
        private Items queryItems;
        private List<PredictItem<DocItem>> predictItems;
        private VideoRecommenderContext recommendContext;
        private GBDTFeatureExtractor featureExtractor;

        public PredictTask(PredictModel predictModel, PredictModel predictModel_cvr, Items queryItems, VideoRecommenderContext recommendContext,
                           List<PredictItem<DocItem>> predictItems, GBDTFeatureExtractor featureExtractor) {
            this.predictModel = predictModel;
            this.predictModel_cvr = predictModel_cvr;
            this.predictItems = predictItems;
            this.queryItems = queryItems;
            this.recommendContext = recommendContext;
            this.featureExtractor = featureExtractor;
        }

        @Override
        public List<PredictTaskItem> call() throws Exception {

            List<PredictTaskItem> predictTaskItems = new ArrayList<>();

            int nullCount = 0;
            for (PredictItem<DocItem> predictItem : predictItems) {
                if (predictItem.getItem() == null) {
                    ++nullCount;
                }
            }

            if (nullCount > 0) {
                LOG.error("Found null docItem count={}, items size={}", nullCount, predictItems.size());
            }
            for (PredictItem<DocItem> predictItem : predictItems) {
                DocItem docItem = predictItem.getItem();
                List<IFeature> features = featureExtractor.extract(predictItem, queryItems);
                double cvrScore = this.predictModel_cvr.predict(features);


                PredictTaskItem predictTaskItem = new PredictTaskItem(docItem.getId(), cvrScore, cvrScore);
                predictTaskItems.add(predictTaskItem);
            }
            return predictTaskItems;
        }
    }

    @Getter
    public static class PredictTaskItem {
        private String docId;
        private double score;
        private double predictScore;

        private PredictTaskItem(String docId, double score, double predictScore) {
            this.docId = docId;
            this.score = score;
            this.predictScore = predictScore;
        }
    }
}
