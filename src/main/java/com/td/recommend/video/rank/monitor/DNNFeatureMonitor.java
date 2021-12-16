package com.td.recommend.video.rank.monitor;

import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Reservoir;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.td.rank.deepfm.felib.FeatureConfig;
import com.td.rank.deepfm.felib.FeatureConfigItem;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DNNFeatureMonitor {

    private DNNFeatureMonitor() {
    }

    private static final ExecutorService monitorExecutor = new ThreadPoolExecutor(8, 15,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(5000),
            new ThreadFactoryBuilder().setNameFormat("feature-monitor-%d").build());
    private static DNNFeatureMonitor featureMonitor = new DNNFeatureMonitor();
    Map<String, Map<String, Reservoir>> modelFeatureReservoirMap = new ConcurrentHashMap<>();

    public static DNNFeatureMonitor getInstance() {
        return featureMonitor;
    }

    public void asynMonitor(FeatureConfig featureConfig, JSONObject extractFeatures, int docRelevant, String model, VideoRecommenderContext recommendContext) {
        String appId = recommendContext.getRecommendRequest().getAppId();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        if (extractFeatures != null && appId.equals("t01") && ihf < 3) {
            CompletableFuture.runAsync(() -> monitor(featureConfig, extractFeatures, docRelevant, model, recommendContext), monitorExecutor);
        }
    }

    public void asynMonitor(FeatureConfig featureConfig, List<JSONObject> extractFeatures, int docRelevant, String model, VideoRecommenderContext recommendContext) {
        String appId = recommendContext.getRecommendRequest().getAppId();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        if (extractFeatures != null && extractFeatures.size() > 0 && appId.equals("t01") && ihf < 3){
            CompletableFuture.runAsync(() -> monitor(featureConfig, extractFeatures, docRelevant, model, recommendContext), monitorExecutor);
        }
    }

    public void monitor(FeatureConfig featureConfig, List<JSONObject> extractFeature, int docRelevant, String model, VideoRecommenderContext recommendContext) {
        Random random = new Random();
        extractFeature.forEach(jsonObject -> {
            float r = random.nextFloat();
            if (r < 0.01) {
                monitor(featureConfig, jsonObject, docRelevant, model, recommendContext);
            }
        });
    }

    public void monitor(FeatureConfig featureConfig, JSONObject extractFeatures, int docRelevant, String model, VideoRecommenderContext recommendContext) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        modelFeatureReservoirMap.putIfAbsent(model, new ConcurrentHashMap<>());
        Map<String, Reservoir> featureReservoir = modelFeatureReservoirMap.get(model);
        JSONObject features = featureConfig.features();
        Set<String> indexs = features.keySet().stream().filter(x -> {
            FeatureConfigItem item = features.getObject(x, FeatureConfigItem.class);
            return item.docRelevant() == docRelevant;
        }).collect(Collectors.toSet());
        indexs.forEach(index -> {
            String monitorName = "monitor.predict.feature.loss";
            String featureName = featureConfig.index2name(index).getOrElse(() -> "missing");
            featureReservoir.putIfAbsent(index, new ExponentiallyDecayingReservoir());
            HashMap<String, String> tags = new HashMap<>();
            tags.put("model", model);
            tags.put("feature", featureName);
            if (extractFeatures.containsKey(index)) {
                taggedMetricRegistry.taggedHistogram(featureReservoir.get(index), monitorName, tags).update(0);
            } else {
                taggedMetricRegistry.taggedHistogram(featureReservoir.get(index), monitorName, tags).update(100);
            }
        });
    }
}
