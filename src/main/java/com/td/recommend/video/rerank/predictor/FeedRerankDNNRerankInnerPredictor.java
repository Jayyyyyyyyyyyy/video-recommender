package com.td.recommend.video.rerank.predictor;

import com.alibaba.fastjson.JSONObject;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.td.data.profile.TVariance;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.userstore.data.UserRawData;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.rerank.featureextractor.FeedRerankDNNFeatureExtractor;
import com.td.recommend.video.rerank.featureextractor.RerankDNNFeatureExtractor;
import com.td.recommend.video.rerank.model.ReloadableRerankDNNModel;
import com.td.recommend.video.rerank.model.RerankDNNModel;
import com.td.recommend.video.rerank.monitor.RerankDNNFeatureMonitor;
import com.td.recommend.video.rerank.monitor.RerankDNNPredictMonitor;
import com.td.rerank.dnn.felib.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.codahale.metrics.Timer.Context;

/**
 * Created by admin on 2017/8/3.
 */
public class FeedRerankDNNRerankInnerPredictor implements RerankInnerPredictor {
    private static final Logger LOG = LoggerFactory.getLogger(FeedRerankDNNRerankInnerPredictor.class);

    private final VideoRecommenderContext recommendContext;
    private final ReloadableRerankDNNModel bucketPredictModel;
    private RerankDNNFeatureExtractor featureExtractor = FeedRerankDNNFeatureExtractor.getInstance();

    public FeedRerankDNNRerankInnerPredictor(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
        this.bucketPredictModel = ReloadableRerankDNNModel.feed;
    }

    @Override
    public List<TaggedItem<PredictItem<DocItem>>> predict(List<TaggedItem<PredictItem<DocItem>>> taggedItems) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        taggedMetricRegistry.meter("video-recommend.rerank.dnn.predict.qps").mark();
        Context predictTime = null;
        if (!recommendContext.isDebug()) {
            predictTime = taggedMetricRegistry.timer("video-recommend.rerank.dnn.predict.latency").time();
        }
        FeatureConfig featureConfig = bucketPredictModel.getFeatureConfig();
        Vocabulary vocabulary = bucketPredictModel.getVocabulary();
        Buckets buckets = bucketPredictModel.getBuckets();
        MtlConfigV3 mtlConfigV3 = bucketPredictModel.getMtlConfigV3();
        RerankDNNModel dnnModel = bucketPredictModel.getModel();
        OnlineConfig onlineConfig = bucketPredictModel.getOnlineConfig();
        BucketsItem bucketsItem = onlineConfig.rankPosBucket();
        if (!mtlConfigV3.exists(recommendContext.getModel())) {
            return taggedItems;
        }
        int taggedItemsSize = taggedItems.size();
        int topN = onlineConfig.topN();
        int videoSize = Math.min(taggedItems.size(), topN);
        List<JSONObject> supList = Lists.newArrayList();
        List<Integer> supRankList = Lists.newArrayList();
        List<Integer> supPosList = Lists.newArrayList();
        List<TaggedItem<PredictItem<DocItem>>> items = taggedItems;
        if (taggedItemsSize < topN) {
            int supSize = topN - taggedItemsSize;
            JSONObject jsonObject = FeatureExtractor.buildDefaultDocRelevantIdfeatures(featureConfig);
            for (int i = 0; i < supSize; i++) {
                supList.add(jsonObject);
                supRankList.add(0);
                supPosList.add(taggedItemsSize + i);
            }
        } else {
            items = new ArrayList<>(taggedItems.subList(0, topN));
        }
        UserItem userItem = recommendContext.getUserItem();
        Map<String, Map<String, TVariance>> userVarianceMap = UserProfileUtils.getVarianceMap(userItem);
        JSONObject featureJson = new JSONObject();
//        featureJson.put("userProfile", userVarianceMap);
        if (userVarianceMap.size() > 0){
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
            featureJson.put("st_vcat_ck", filterStFeat(userVarianceMap.get("st_vcat_ck"), "ck"));
            featureJson.put("st_vsubcat_ck", filterStFeat(userVarianceMap.get("st_vsubcat_ck"), "ck"));
            featureJson.put("st_vsubcat_cs", userVarianceMap.get("st_vsubcat_cs"));
            featureJson.put("st_vtag_cs", userVarianceMap.get("st_vtag_cs"));
            featureJson.put("st_vtag_ck", filterStFeat(userVarianceMap.get("st_vtag_ck"), "ck"));
            featureJson.put("st_vmp3_cs", userVarianceMap.get("st_vmp3_cs"));
            featureJson.put("st_vmp3_ck", filterStFeat(userVarianceMap.get("st_vmp3_ck"), "ck"));
            featureJson.put("st_vauthor_uid_cs", userVarianceMap.get("st_vauthor_uid_cs"));
            featureJson.put("st_vauthor_uid_ck", filterStFeat(userVarianceMap.get("st_vauthor_uid_ck"), "ck"));
        }

//        featureJson.put("timestamp", dateFormat(""));
        featureJson.put("hour", dateFormat("HH"));
        featureJson.put("clicked", recommendContext.getClicks());
        featureJson.put("played", recommendContext.getPlayed());
        featureJson.put("fav", recommendContext.getFav());
        featureJson.put("download", recommendContext.getDownLoad());

        Optional<UserRawData> userRawDataOptional = userItem.getUserRawData();
        if (userRawDataOptional.isPresent()) {
            Map<String, Map<String, Double>> dataMap = userRawDataOptional.get().getDataMap();
            if (dataMap != null && dataMap.size() > 0){
                featureJson.put("xfollow", dataMap.get("xfollow"));
            }
        }
//        Optional<IItem> contextItem = queryItems.get(ItemKey.context);
//        contextItem.ifPresent(iItem -> featureJson.put("context", iItem.getTags()));
//        List<TaggedItem<PredictItem<DocItem>>> rankList = new ArrayList<>(items);
        List<TaggedItem<PredictItem<DocItem>>> clickRankList = new ArrayList<>(items);
        clickRankList.sort((o1, o2) -> -Double.compare(o1.getItem().getModelScore().get("click"), o2.getItem().getModelScore().get("click")));
//        rankList.sort((o1, o2) -> -Double.compare(o1.getItem().getScore(), o2.getItem().getScore()));
        List<Integer> clickRankIndexList = Lists.newArrayList();
        List<Integer> rankIndexList = Lists.newArrayList();
        List<Integer> posList = Lists.newArrayList();
        JSONObject docIrrelevantIdFeatures = new JSONObject();
        List<JSONObject> dumpInfoList = Lists.newArrayList();
        try {
            Context userFeature = taggedMetricRegistry.timer("video-recommend.rerank.dnn.user.feature.extract.latency").time();
            JSONObject docIrrelevantRawFeatures = FeatureExtractor.extractDocIrrelevantRawFeatures(featureJson, featureConfig, FieldNavigatorModule.ONLINE());
            JSONObject docIrrelevantBucketizedFeatures = FeatureExtractor.extractDocIrrelevantBucketizedFeatures(docIrrelevantRawFeatures, featureConfig, FieldNavigatorModule.ONLINE(), buckets);
            docIrrelevantIdFeatures = FeatureExtractor.extractDocIrrelevantIdFeatures(docIrrelevantBucketizedFeatures, featureConfig, vocabulary);
            userFeature.stop();
            RerankDNNFeatureMonitor.getInstance().asynMonitor(featureConfig, docIrrelevantBucketizedFeatures, 0, "dnn", recommendContext);
            for (int i = 0; i < videoSize; i++) {
                posList.add(i);
                TaggedItem<PredictItem<DocItem>> predictItemTaggedItem = taggedItems.get(i);
                int clickRankListIndex = clickRankList.indexOf(predictItemTaggedItem);
                clickRankIndexList.add(clickRankListIndex);
                int initIndex = predictItemTaggedItem.getInitIndex();
                int bucketize = bucketsItem.bucketize(initIndex);
                rankIndexList.add(bucketize);
                PredictItem<DocItem> item = predictItemTaggedItem.getItem();
                JSONObject extract = featureExtractor.extract(item, i, featureConfig, vocabulary, buckets, docIrrelevantRawFeatures, docIrrelevantBucketizedFeatures, recommendContext);
                dumpInfoList.add(extract);
            }
        } catch (Exception e) {
            LOG.error("docIrrelevantIdFeatures extract failed", e);
        }

        if (supList.size() > 0) {
            dumpInfoList.addAll(supList);
            clickRankIndexList.addAll(supRankList);
            rankIndexList.addAll(supRankList);
            posList.addAll(supPosList);
        }
        float[][] predictArray;
        try {
            predictArray = dnnModel.predict(dumpInfoList, docIrrelevantIdFeatures, featureConfig, onlineConfig.modelTaskNum(), videoSize, posList, rankIndexList, clickRankIndexList);
            if (predictArray.length == topN) {
                taggedMetricRegistry.histogram("video-recommend.rerank.dnn.score.failure.rate").update(0);
            } else {
                taggedMetricRegistry.histogram("video-recommend.rerank.dnn.score.failure.rate").update(100);
            }
        } catch (Exception e) {
            taggedMetricRegistry.histogram("video-recommend.rerank.dnn.score.failure.rate").update(100);
            LOG.error("rerank dnn predict failed", e);
            return taggedItems;
        }
        List<Float> scoreMonitor = Lists.newArrayList();
        Map<Integer, TaggedItem<PredictItem<DocItem>>> taggedItemMap = Maps.newLinkedHashMap();
        for (int i = 0; i < videoSize; i++) {
            try {
                TaggedItem<PredictItem<DocItem>> predictItemTaggedItem = taggedItems.get(i);
                PredictItem<DocItem> predictItem = predictItemTaggedItem.getItem();
                float[] itemPredictArray = predictArray[i];
                if (itemPredictArray != null && itemPredictArray.length > 0) {
                    Map<String, Double> modelScoreMap = new HashMap<>();
                    int taskNum = mtlConfigV3.taskNum(recommendContext.getModel());
                    float[] array = new float[taskNum];
                    for (int j = 0; j < taskNum; j++) {
                        String task = mtlConfigV3.task(recommendContext.getModel(), j);
                        if (mtlConfigV3.isScoreFromRank(recommendContext.getModel(), j)) {
                            Double taskScore = predictItem.getModelScore().get(task);
                            array[j] = taskScore.floatValue();
                            modelScoreMap.put(task, taskScore);
                        } else {
                            int rerankScoreIndex = mtlConfigV3.rerankScoreIndex(recommendContext.getModel(), j);
                            float taskScore = itemPredictArray[rerankScoreIndex];
                            array[j] = taskScore;
                            modelScoreMap.put(task, (double)taskScore);
                        }
                    }
                    float score = mtlConfigV3.predict(recommendContext.getModel(), array);
                    predictItem.setRerankScore(score);
                    predictItem.setRerankPredictScore(score);
                    predictItem.setRerankModelScore(modelScoreMap);
                    scoreMonitor.add(score);
                }
            } catch (Exception e) {
                LOG.error("rerank felib predict error", e);
            }
        }
        taggedItems.sort((o1, o2) -> -Double.compare(o1.getItem().getRerankScore(), o2.getItem().getRerankScore()));
        RerankDNNPredictMonitor.getInstance().asyncMonitor(predictArray, mtlConfigV3, "dnn", recommendContext.getModel());
        RerankDNNPredictMonitor.getInstance().asyncMonitor(scoreMonitor, "dnn");
        if (!recommendContext.isDebug()) {
            predictTime.stop();
        }
        return taggedItems;
    }

    private Map<String, TVariance> filterFeat(Map<String, TVariance> variance, String key){
        if (variance != null && variance.size() > 0){
            return variance.entrySet().stream().filter(x -> {
                TVariance variance1 = x.getValue();
                if (key.equals("cs")){
                    return variance1.posCnt >= 3;
                }
                if (key.equals("ck")){
                    return variance1.negCnt >= 3;
                }
                return false;
            }).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));
        }
        return variance;
    }

    private Map<String, TVariance> filterStFeat(Map<String, TVariance> variance, String key){
        if (variance != null && variance.size() > 0){
            return variance.entrySet().stream().filter(x -> {
                TVariance variance1 = x.getValue();
                if (key.equals("cs")){
                    return variance1.posCnt >= 3;
                }
                if (key.equals("ck")){
                    return variance1.negCnt >= 2;
                }
                return false;
            }).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));
        }
        return variance;
    }

    private String dateFormat(String pattern){
        if (StringUtils.isBlank(pattern)){
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }
}
