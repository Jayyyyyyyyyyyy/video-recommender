package com.td.recommend.video.rank.featuredumper;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.td.featurestore.feature.Feature;
import com.td.featurestore.feature.IFeature;
import com.td.recommend.commons.json.JsonUtils;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.video.utils.FastDouble;
import com.td.recommend.video.utils.KafkaClient;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static com.codahale.metrics.Timer.Context;

/**
 * Created by admin on 2017/6/23.
 */
public class FeatureDumper {
    private static final Logger LOG = LoggerFactory.getLogger("FeatureDumper");

    private static final ExecutorService dumpExecutor= new ThreadPoolExecutor(8, 15,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(5000),
                new ThreadFactoryBuilder().setNameFormat("feature-dump-%d").build());

    private static FeatureDumper instance =  new FeatureDumper();

    private KafkaClient kafkaClient;
    private static final String TOPIC = "video_features";

    public static FeatureDumper getInstance() {
        return instance;
    }

    private FeatureDumper() {
        kafkaClient = KafkaClient.getInstance();
    }

    public void  asyncDumpAll(List<DumpInfo> dumpInfos) {
        CompletableFuture.runAsync(() -> dumpAll(dumpInfos), dumpExecutor);
    }
    public void asyncDump(DumpInfo dumpInfo){
        CompletableFuture.runAsync(() -> dump(dumpInfo), dumpExecutor);
    }

    public void dumpAll(List<DumpInfo> dumpInfos) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Context featureDumpTime = taggedMetricRegistry.timer("uservideo.featuredump.latency").time();

        int dumpTopK = Math.max(250, dumpInfos.size());
        dumpInfos.sort(Comparator.comparing(FeatureDumper.DumpInfo::getPredictScore).reversed());

        int index = 0;
        for (DumpInfo dumpInfo : dumpInfos) {
            dump(dumpInfo);

            if (index > dumpTopK) {
                break;
            }
            ++index;
        }

        featureDumpTime.stop();
    }

    public void dump(DumpInfo dumpInfo) {
        Optional<String> dumpJsonOpt = JsonUtils.toJson(dumpInfo);
        if (dumpJsonOpt.isPresent()) {
            kafkaClient.send(TOPIC, dumpInfo.getUserId(), dumpJsonOpt.get());
        }
    }

    @Getter
    public static class DumpInfo {
        Map<String, Double> features;
        String predictId;
        String docId;
        String userId;
        double predictScore;
        long timestamp;

        public DumpInfo(List<IFeature> features,String predictId, String docId, String userId, double predictScore) {
            this.features = new HashMap<>();
            for (IFeature feature : features) {
                this.features.put(feature.getName(), FastDouble.round(feature.getValue(),4));
            }
            this.predictId = predictId;
            this.docId = docId;
            this.userId = userId;
            this.timestamp = System.currentTimeMillis();
            this.predictScore = predictScore;
        }
    }

    public static void main(String[] args) {
        FeatureDumper featureDumper = new FeatureDumper();
        List<IFeature> features = new ArrayList<>();
        features.add(new Feature("u:cat_cs:??????", 0.2));
        features.add(new Feature("u:cat_ck:??????", 0.1));
        features.add(new Feature("u:cat_cs:??????", 0.4));

//        featureDumper.dump("221", "123", "32323", features, 0.2);
    }
}
