package com.td.recommend.video.rerank.monitor;

import com.alibaba.fastjson.JSONObject;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.td.rerank.dnn.felib.*;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RerankDNNFeatureMonitor {

    private RerankDNNFeatureMonitor() {
    }

    private static final ExecutorService monitorExecutor = new ThreadPoolExecutor(8, 15,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(5000),
            new ThreadFactoryBuilder().setNameFormat("rerank-feature-monitor-%d").build());
    private static RerankDNNFeatureMonitor featureMonitor = new RerankDNNFeatureMonitor();

    public static RerankDNNFeatureMonitor getInstance() {
        return featureMonitor;
    }

    public void asynMonitor(FeatureConfig featureConfig, JSONObject extractFeatures, int docRelevant, String model, VideoRecommenderContext recommendContext) {
        if (extractFeatures != null && "80000".equals(recommendContext.getRecommendRequest().getCid())){
            CompletableFuture.runAsync(() -> monitor(featureConfig, extractFeatures, docRelevant, model, recommendContext), monitorExecutor);
        }
    }

    public void asynMonitor(FeatureConfig featureConfig, List<JSONObject> extractFeatures, int docRelevant, String model, VideoRecommenderContext recommendContext) {
        if (extractFeatures != null && extractFeatures.size() > 0 && "80000".equals(recommendContext.getRecommendRequest().getCid())){
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
        JSONObject features = featureConfig.features();
        Set<String> indexs = features.keySet().stream().filter(x -> {
            FeatureConfigItem item = features.getObject(x, FeatureConfigItem.class);
            return item.docRelevant() == docRelevant;
        }).collect(Collectors.toSet());
        indexs.forEach(index -> {
            String monitorName = "monitor.rerank." + model + ".predict.feature." + index + ".loss";
            if (extractFeatures.containsKey(index)) {
                taggedMetricRegistry.histogram(monitorName).update(0);
            } else {
                taggedMetricRegistry.histogram(monitorName).update(100);
            }
        });
    }
}
