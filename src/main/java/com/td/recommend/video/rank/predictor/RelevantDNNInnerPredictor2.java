package com.td.recommend.video.rank.predictor;

import com.alibaba.fastjson.JSONObject;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.Lists;
import com.td.data.profile.TVariance;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.rank.deepfm.felib.*;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.rank.model.DNNModel2;
import com.td.recommend.commons.thread.BatchTaskExecutor;
import com.td.recommend.commons.thread.BatchTaskExecutor.TaskFactory;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.userstore.data.UserRawData;
import com.td.recommend.video.concurrent.ApplicationSharedExecutorService;
import com.td.recommend.video.rank.featuredumper.FeatureDumperSampler;
import com.td.recommend.video.rank.featureextractor.DNNFeatureExtractor;
import com.td.recommend.video.rank.featureextractor.RelevantDNNFeatureExtractor2;
import com.td.recommend.video.rank.model.ReloadableDNNModel;
import com.td.recommend.video.rank.monitor.DNNFeatureMonitor;
import com.td.recommend.video.rank.monitor.DNNPredictMonitor;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.codahale.metrics.Timer.Context;

/**
 * Created by admin on 2017/8/3.
 */
public class RelevantDNNInnerPredictor2 implements InnerPredictor {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantDNNInnerPredictor2.class);

    private final FeatureDumperSampler featureDumperSampler;
    private final VideoRecommenderContext recommendContext;
    private final ReloadableDNNModel bucketPredictModel;
    private BatchTaskExecutor<PredictItem<DocItem>, PredictTaskItem> batchTaskExecutor;
    private int predictTimeoutMs;
    private DNNFeatureExtractor featureExtractor = RelevantDNNFeatureExtractor2.getInstance();

    public RelevantDNNInnerPredictor2(VideoRecommenderContext recommendContext, FeatureDumperSampler featureDumperSampler, int predictTimeoutMs) {
        this.recommendContext = recommendContext;
        this.bucketPredictModel = ReloadableDNNModel.rlvt2;
        this.featureDumperSampler = featureDumperSampler;
        this.batchTaskExecutor = new BatchTaskExecutor<>(ApplicationSharedExecutorService.getInstance().getExecutorService(), 50);
        this.predictTimeoutMs = predictTimeoutMs;
    }

    public RelevantDNNInnerPredictor2(VideoRecommenderContext recommendContext, FeatureDumperSampler featureDumperSampler) {
        this(recommendContext, featureDumperSampler, 300);
    }

    public void setPredictTimeoutMs(int predictTimeoutMs) {
        this.predictTimeoutMs = predictTimeoutMs;
    }

    @Override
    public Optional<PredictResult> predict(PredictItems<DocItem> predictItems, Items queryItems, String predictId) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        taggedMetricRegistry.meter("rlvt.dnn2.predict.qps").mark();
        Context predictTime = taggedMetricRegistry.timer("rlvt.dnn2predict.latency").time();
        FeatureConfig featureConfig = bucketPredictModel.getFeatureConfig();
        Vocabulary vocabulary = bucketPredictModel.getVocabulary();
        Buckets buckets = bucketPredictModel.getBuckets();
        MtlConfigV2 mtlConfigV2 = bucketPredictModel.getMtlConfigV2();
        DNNModel2 dnnModel = bucketPredictModel.getDNNModel2();

        UserItem userItem = UserProfileUtils.getUserItem(queryItems);
        Map<String, Map<String, TVariance>> userVarianceMap = UserProfileUtils.getVarianceMap(userItem);
        JSONObject featureJson = new JSONObject();

        //current viewing target video features
        DocItem currentDocItem = (DocItem) recommendContext.getQueryItems().get(ItemKey.doc).get();
        RelevantDNNFeatureExtractor2.extractStaticDocFeatures(featureJson, currentDocItem, "t_");

        if (userVarianceMap.size() > 0) {
            featureJson.put("vcat_cs", filterFeat(userVarianceMap.get("vcat_cs"), "cs"));
            featureJson.put("vcat_ck", filterFeat(userVarianceMap.get("vcat_ck"), "ck"));
            featureJson.put("vsubcat_ck", filterFeat(userVarianceMap.get("vsubcat_ck"), "ck"));
            featureJson.put("vsubcat_cs", filterFeat(userVarianceMap.get("vsubcat_cs"), "cs"));
            featureJson.put("vtag_cs", filterFeat(userVarianceMap.get("vtag_cs"), "cs"));
            featureJson.put("vtag_ck", filterFeat(userVarianceMap.get("vtag_ck"), "ck"));
            featureJson.put("vmp3_cs", filterFeat(userVarianceMap.get("vmp3_cs"), "cs"));
            featureJson.put("vmp3_ck", filterFeat(userVarianceMap.get("vmp3_ck"), "ck"));
            featureJson.put("vauthor_uid_cs", filterFeat(userVarianceMap.get("vauthor_uid_cs"), "cs"));
            featureJson.put("vauthor_uid_ck", filterFeat(userVarianceMap.get("vauthor_uid_ck"), "ck"));

            featureJson.put("st_vcat_cs", userVarianceMap.get("st_vcat_cs"));
            featureJson.put("st_vcat_ck", userVarianceMap.get("st_vcat_ck"));
            featureJson.put("st_vsubcat_ck", userVarianceMap.get("st_vsubcat_ck"));
            featureJson.put("st_vsubcat_cs", userVarianceMap.get("st_vsubcat_cs"));
            featureJson.put("st_vtag_cs", userVarianceMap.get("st_vtag_cs"));
            featureJson.put("st_vtag_ck", userVarianceMap.get("st_vtag_ck"));
            featureJson.put("st_vmp3_cs", userVarianceMap.get("st_vmp3_cs"));
            featureJson.put("st_vmp3_ck", userVarianceMap.get("st_vmp3_ck"));
            featureJson.put("st_vauthor_uid_cs", userVarianceMap.get("st_vauthor_uid_cs"));
            featureJson.put("st_vauthor_uid_ck", userVarianceMap.get("st_vauthor_uid_ck"));
        }

        featureJson.put("timestamp", dateFormat(""));
        featureJson.put("hour", dateFormat("HH"));
        featureJson.put("clicked", recommendContext.getClicks());
        featureJson.put("fav", recommendContext.getFav());
        featureJson.put("download", recommendContext.getDownLoad());

        Optional<UserRawData> userRawDataOptional = userItem.getUserRawData();
        if (userRawDataOptional.isPresent()) {
            Map<String, Map<String, Double>> dataMap = userRawDataOptional.get().getDataMap();
            if (dataMap != null && dataMap.size() > 0) {
                featureJson.put("xfollow", dataMap.get("xfollow"));
            }
        }
        Optional<IItem> contextItem = queryItems.get(ItemKey.context);
        contextItem.ifPresent(iItem -> featureJson.put("context", iItem.getTags()));

        //******
//        LOG.info("userFeatures : [{}]", JSON.toJSONString(featureJson));
        //******
        List<Future<List<PredictTaskItem>>> resultFutureList = Collections.EMPTY_LIST;
        JSONObject docIrrelevantIdFeatures = new JSONObject();
        try {
            Context userFeature = taggedMetricRegistry.timer("rlvt.dnn2predict.userFeature.latency").time();
            JSONObject docIrrelevantRawFeatures = com.td.rank.deepfm.felib.FeatureExtractor.extractDocIrrelevantRawFeatures(featureJson, featureConfig, FieldNavigatorModule.ONLINE());
            JSONObject docIrrelevantBucketizedFeatures = com.td.rank.deepfm.felib.FeatureExtractor.extractDocIrrelevantBucketizedFeatures(docIrrelevantRawFeatures, featureConfig, FieldNavigatorModule.ONLINE(), buckets);
            docIrrelevantIdFeatures = com.td.rank.deepfm.felib.FeatureExtractor.extractDocIrrelevantIdFeatures(docIrrelevantBucketizedFeatures, featureConfig, vocabulary);
            userFeature.stop();
            DNNFeatureMonitor.getInstance().asynMonitor(featureConfig, docIrrelevantBucketizedFeatures, 0, "rlvtdnn2", recommendContext);
            TaskFactory<PredictItem<DocItem>, PredictTaskItem> taskFactory = items ->
                    new PredictTask(dnnModel, predictId, items, featureExtractor, featureConfig, vocabulary, buckets, featureJson, docIrrelevantRawFeatures, docIrrelevantBucketizedFeatures, recommendContext);
            resultFutureList = batchTaskExecutor.submit(predictItems.getItems(), taskFactory, predictTimeoutMs);
            taggedMetricRegistry.histogram("rlvt.dnn2predict.docIrrelevantIdFeatures.failrate").update(0);
        } catch (Exception e) {
            taggedMetricRegistry.histogram("rlvt.dnn2predict.docIrrelevantIdFeatures.failrate").update(100);
            LOG.error("rlvt dnn2 docIrrelevantIdFeatures extract failed", e);
        }
        List<JSONObject> dumpInfoList = Lists.newArrayList();
        List<String> docIdList = Lists.newArrayList();
        Map<String, ImmutablePair<Float, float[]>> scoreMap = new HashMap<>();
        //*****
//        List<Map<String, Object>> printList = Lists.newArrayList();
        //******
        for (Future<List<PredictTaskItem>> listFuture : resultFutureList) {
            if (!listFuture.isCancelled()) {
                try {
                    List<PredictTaskItem> predictTaskItems = listFuture.get();
                    for (PredictTaskItem predictTaskItem : predictTaskItems) {
                        docIdList.add(predictTaskItem.getDocId());
                        dumpInfoList.add(predictTaskItem.getFeature());
                        //*******
//                        printList.add(ImmutableMap.of(predictTaskItem.getDocId(), predictTaskItem.getFeature()));
                        //*******
                    }
                    taggedMetricRegistry.histogram("rlvt.dnn2predict.taskfailrate").update(0);
                } catch (InterruptedException e) {
                    taggedMetricRegistry.histogram("rlvt.dnn2predict.taskfailrate").update(100);
                    LOG.error("rlvt dnn2 batch predict is interrupted!", e);
                } catch (ExecutionException e) {
                    taggedMetricRegistry.histogram("rlvt.dnn2predict.taskfailrate").update(100);
                    LOG.error("rlvt dnn2 batch predict execution failed!", e);
                }
            } else {
                taggedMetricRegistry.histogram("rlvt.dnn2predict.taskfailrate").update(100);
                LOG.error("rlvt dnn2 batch predict is canceled!");
            }
        }
        // ******
//        LOG.info("extracted user Features  || [{}]", JSON.toJSONString(docIrrelevantIdFeatures));
//        printList.forEach(x->{
//            LOG.info("extracted doc Features  || [{}]", JSON.toJSONString(x));
//        });
        // ******
        float[][] predictArray;
        try {
            predictArray = dnnModel.predict(dumpInfoList, docIrrelevantIdFeatures, featureConfig, mtlConfigV2.size(), "rlvtdnn2");
            if (predictArray.length == dumpInfoList.size()) {
                taggedMetricRegistry.histogram("rlvt.dnn2predict.score.length").update(0);
            } else {
                taggedMetricRegistry.histogram("rlvt.dnn2predict.score.length").update(100);
            }
            taggedMetricRegistry.histogram("rlvt.dnn2predict.score").update(0);
        } catch (Exception e) {
            taggedMetricRegistry.histogram("rlvt.dnn2predict.score").update(100);
            LOG.error("rlvt dnn2 predict failed", e);
            return Optional.empty();
        }
        String mtlBucket = mtlBucket(userVarianceMap);
        if (recommendContext.getRecommendRequest().getAppId().equals("t01")) {
            taggedMetricRegistry.meter("rlvt.dnn2.mtlBucket." + mtlBucket + ".qps").mark();
        }
        List<Float> scoreMonitor = Lists.newArrayList();
        for (int i = 0; i < predictArray.length; i++) {
            try {
                float score = mtlConfigV2.predict(mtlBucket, predictArray[i]);
                scoreMap.put(docIdList.get(i), ImmutablePair.of(score, predictArray[i]));
                scoreMonitor.add(score);
            } catch (Exception e) {
                LOG.error("rlvt dnn2 felib predict error", e);
            }
        }
        DNNPredictMonitor.getInstance().asyncMonitor(predictArray, mtlConfigV2, "rlvtdnn2");
        DNNPredictMonitor.getInstance().asyncMonitor(scoreMonitor, "rlvtdnn2");
        LOG.info("rlvt dnn2 predictItems length : {} || dumpInfoList length : {} || predictList length : {}", predictItems.getSize(), dumpInfoList.size(), predictArray.length);
        if (scoreMap.size() < predictItems.getItems().size()) {
            taggedMetricRegistry.histogram("rlvt.dnn2predict.lessrate").update(100);
        } else {
            taggedMetricRegistry.histogram("rlvt.dnn2predict.lessrate").update(0);
        }
        List<Double> scoreList = Lists.newArrayList();
        List<Map<String, Double>> modelScoreList = Lists.newArrayList();
        for (PredictItem<DocItem> predictItem : predictItems) {
            ImmutablePair<Float, float[]> pair = scoreMap.get(predictItem.getId());
            Float score = pair.getLeft();
            if (score != null) {
                scoreList.add((double) score);
            } else {
                scoreList.add((double) Integer.MIN_VALUE);
            }
            float[] modelScores = pair.getRight();
            Map<String, Double> modelScoreMap = new HashMap<>();
            for (int i = 0; i < mtlConfigV2.size(); i++) {
                try {
                    modelScoreMap.put(String.valueOf(mtlConfigV2.task(i)), (double) modelScores[i]);
                } catch (Exception e) {
                    LOG.error("rlvt dnn2 felib get taskName error", e);
                }
            }
            modelScoreList.add(modelScoreMap);
        }

        PredictResult predictResult = new PredictResult();
        predictResult.setPredictId(predictId);
        predictResult.setScores(scoreList);
        predictResult.setPredictScores(scoreList);
        predictResult.setModelScores(modelScoreList);
        predictResult.setMtlBucket(mtlBucket);
        if (!recommendContext.isDebug()) {
            predictTime.stop();
        }
        return Optional.of(predictResult);
    }

    static class PredictTask implements Callable<List<PredictTaskItem>> {
        private DNNModel2 dnnModel;
        private List<PredictItem<DocItem>> predictItems;
        private String predictId;
        private DNNFeatureExtractor featureExtractor;
        private FeatureConfig featureConfig;
        private Vocabulary vocabulary;
        private Buckets buckets;
        private JSONObject docIrrelevantRawJSON;
        private JSONObject docIrrelevantRawFeatures;
        private JSONObject docIrrelevantBucketizedFeatures;
        private VideoRecommenderContext recommendContext;

        public PredictTask(DNNModel2 dnnModel, String predictId,
                           List<PredictItem<DocItem>> predictItems, DNNFeatureExtractor featureExtractor,
                           FeatureConfig featureConfig, Vocabulary vocabulary, Buckets buckets, JSONObject docIrrelevantRawJSON,
                           JSONObject docIrrelevantRawFeatures, JSONObject docIrrelevantBucketizedFeatures,
                           VideoRecommenderContext recommendContext) {
            this.dnnModel = dnnModel;
            this.predictItems = predictItems;
            this.predictId = predictId;
            this.featureExtractor = featureExtractor;
            this.featureConfig = featureConfig;
            this.vocabulary = vocabulary;
            this.buckets = buckets;
            this.docIrrelevantRawJSON = docIrrelevantRawJSON;
            this.docIrrelevantRawFeatures = docIrrelevantRawFeatures;
            this.docIrrelevantBucketizedFeatures = docIrrelevantBucketizedFeatures;
            this.recommendContext = recommendContext;
        }

        @Override
        public List<PredictTaskItem> call() {
            List<PredictTaskItem> predictTaskItems = new ArrayList<>();
            int nullCount = 0;
            for (PredictItem<DocItem> predictItem : predictItems) {
                if (predictItem.getItem() == null) {
                    ++nullCount;
                }
            }

            if (nullCount > 0) {
                LOG.error("rlvt dnn2 Found null docItem count={}, items size={}", nullCount, predictItems.size());
            }
            for (PredictItem<DocItem> predictItem : predictItems) {
                DocItem docItem = predictItem.getItem();
                JSONObject feature = featureExtractor.extract(predictItem, featureConfig, vocabulary, buckets, docIrrelevantRawJSON, docIrrelevantRawFeatures, docIrrelevantBucketizedFeatures, recommendContext);
                PredictTaskItem predictTaskItem = new PredictTaskItem(docItem.getId(), feature);
                predictTaskItems.add(predictTaskItem);
            }
            return predictTaskItems;
        }
    }

    @Getter
    public static class PredictTaskItem {
        private String docId;
        private JSONObject feature;

        private PredictTaskItem(String docId, JSONObject feature) {
            this.docId = docId;
            this.feature = feature;
        }
    }

    private String mtlBucket(Map<String, Map<String, TVariance>> userVarianceMap) {
        return "default";
    }
}
