package com.td.recommend.video.rank.predictor;

import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.feature.ftrl.conf.FeatureConfig;
import com.td.feature.process.FtrlFeatureProcess;
import com.td.featurestore.feature.IFeature;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.rank.model.FtrlModel;
import com.td.recommend.commons.rank.model.PredictModel;
import com.td.recommend.commons.thread.BatchTaskExecutor;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.concurrent.ApplicationSharedExecutorService;
import com.td.recommend.video.rank.featuredumper.FeatureDumperSampler;
import com.td.recommend.video.rank.featureextractor.GBDTFeatureExtractor;
import com.td.recommend.video.rank.model.LocalFtrlModel;
import com.td.recommend.video.rank.model.ReloadableDNNModel;
import com.td.recommend.video.rank.model.ReloadableFtrlModel;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.instrument.classloading.ResourceOverridingShadowingClassLoader;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FtrlRetrieveInnerPredictor implements InnerPredictor {
    private static final Logger LOG = LoggerFactory.getLogger(FtrlRetrieveInnerPredictor.class);

    private final ReloadableFtrlModel bucketPredictModel;

    private final VideoRecommenderContext recommendContext;

    private BatchTaskExecutor<PredictItem<DocItem>, PredictTaskItem> batchTaskExecutor;

    private int predictTimeoutMs;

    private Random random = new Random();
    private int samperate = 2;


    public FtrlRetrieveInnerPredictor(VideoRecommenderContext recommendContext, int predictTimeoutMs) {
        this.recommendContext = recommendContext;
        this.predictTimeoutMs = predictTimeoutMs;
        this.bucketPredictModel = ReloadableFtrlModel.ftrl_base;
        this.batchTaskExecutor = new BatchTaskExecutor<>(ApplicationSharedExecutorService.getInstance().getExecutorService(), 50);
    }


    @Override
    public Optional<PredictResult> predict(PredictItems<DocItem> predictItems, Items queryItems, String predictId) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Timer.Context predictTime = taggedMetricRegistry.timer("retrieve.ftrlpredict.latency").time();

        LocalFtrlModel localFtrlModel = bucketPredictModel.getLocalmodel();
        boolean isDump = isDumpFeat();

        BatchTaskExecutor.TaskFactory<PredictItem<DocItem>, PredictTaskItem> taskFactory = items ->
                new PredictTask(localFtrlModel, queryItems, recommendContext, items, predictId, isDump);

        List<Future<List<PredictTaskItem>>> resultFutureList = batchTaskExecutor.submit(predictItems.getItems(), taskFactory, predictTimeoutMs);

        Map<String, ImmutablePair<Double, Double>> scoreMap = new HashMap<>();

        for (Future<List<PredictTaskItem>> listFuture : resultFutureList) {
            if (!listFuture.isCancelled()) {
                try {
                    List<PredictTaskItem> predictTaskItems = listFuture.get();
                    for (PredictTaskItem predictTaskItem : predictTaskItems) {
                        scoreMap.put(predictTaskItem.getDocId(), ImmutablePair.of(predictTaskItem.getScore(), predictTaskItem.getPredictScore()));
                    }
                    taggedMetricRegistry.histogram("retrieve.ftrlpredict.taskfailrate").update(0);
                } catch (InterruptedException e) {
                    taggedMetricRegistry.histogram("retrieve.ftrlpredict.taskfailrate").update(100);
                    LOG.error("batch predict is interrupted!", e);
                } catch (ExecutionException e) {
                    taggedMetricRegistry.histogram("retrieve.ftrlpredict.taskfailrate").update(100);
                    LOG.error("batch predict execution failed!", e);
                }
            } else {
                taggedMetricRegistry.histogram("retrieve.ftrlpredict.taskfailrate").update(100);
                LOG.error("batch predict is canceled!");
            }
        }

        if (scoreMap.isEmpty()) {//if all failed
            return Optional.empty();
        }

        if (scoreMap.size() < predictItems.getItems().size()) {
            taggedMetricRegistry.histogram("retrieve.ftrlpredict.lessrate").update(100);
        } else {
            taggedMetricRegistry.histogram("retrieve.ftrlpredict.lessrate").update(0);
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
        private Items items;
        private List<PredictItem<DocItem>> predictItems;
        private VideoRecommenderContext recommendContext;
        private LocalFtrlModel localFtrlModell;
        private String predictId;
        private boolean isDumpFeat = false;

        public PredictTask(LocalFtrlModel localFtrlModel, Items queryItems,
                           VideoRecommenderContext recommendContext, List<PredictItem<DocItem>> predictItems,
                           String predictId, boolean isDumpFeat) {
            this.localFtrlModell = localFtrlModel;
            this.predictItems = predictItems;
            items = new Items();
            Optional<IItem> contextOpt = queryItems.get(ItemKey.context);
            if (contextOpt.isPresent()) {
                items.add(ItemKey.context, contextOpt.get());
            }
            Optional<IItem> userItemOpt = queryItems.get(ItemKey.user);
            if (userItemOpt.isPresent()) {
                items.add(ItemKey.user, userItemOpt.get());
            }
            this.recommendContext = recommendContext;
            this.predictId = predictId;
            this.isDumpFeat = isDumpFeat;
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
            FtrlFeatureProcess featureProcess = localFtrlModell.getFtrlFeatureProcess();
            FtrlModel ftrlModel = localFtrlModell.getFtrlModel();

            int count = 0;
            boolean isDumpFeature = false;
            for (PredictItem<DocItem> predictItem : predictItems) {
                DocItem docItem = predictItem.getItem();
                docItem.addTag("docId", docItem.getId());
                items.add(ItemKey.doc, docItem);
                Map<String, Double> features = new HashMap<>();
                isDumpFeature = isDumpFeat && (count%20==0);
                featureProcess.process(items, FeatureConfig.FeatureType.ALL, features);
                double score = ftrlModel.predict(features, predictId, isDumpFeature);
                PredictTaskItem predictTaskItem = new PredictTaskItem(docItem.getId(), score, score);
                predictTaskItems.add(predictTaskItem);
                count++;
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


    public boolean isDumpFeat() {
        int sample = random.nextInt(100);
        if (sample < samperate) {
            return true;
        }
        return false;
    }

    public static void main(String[] argv) {
        FtrlRetrieveInnerPredictor ftrlRetrieveInnerPredictor = new FtrlRetrieveInnerPredictor(null, 200);
        for (int i=0; i<10; i++) {
            if (ftrlRetrieveInnerPredictor.isDumpFeat()) {
                System.out.println("true");
            }else {
                System.out.println("false");
            }
        }
    }

}
