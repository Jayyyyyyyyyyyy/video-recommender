package com.td.recommend.video.rank.featuredumper;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.td.featurestore.feature.Feature;
import com.td.featurestore.feature.IFeature;
import com.td.recommend.commons.json.JsonUtils;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.video.utils.KafkaClient;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static com.codahale.metrics.Timer.Context;

/**
 * Created by admin on 2017/6/23.
 */
public class ExploreDumper {
    private static final Logger LOG = LoggerFactory.getLogger(ExploreDumper.class);

    private static final ExecutorService dumpExecutor= new ThreadPoolExecutor(8, 15,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(5000),
                new ThreadFactoryBuilder().setNameFormat("explore-dump-%d").build());

    private static ExploreDumper instance =  new ExploreDumper();

    private KafkaClient kafkaClient;
    private static final String TOPIC = "explore_finish";

    public static ExploreDumper getInstance() {
        return instance;
    }

    private ExploreDumper() {
        kafkaClient = KafkaClient.getInstance();
    }

    public void asyncDumpAll(List<DumpInfo> dumpInfos) {
        CompletableFuture.runAsync(() -> dumpAll(dumpInfos), dumpExecutor);
    }
    public void asyncDump(DumpInfo dumpInfo){
        CompletableFuture.runAsync(() -> dump(dumpInfo), dumpExecutor);
    }

    public void dumpAll(List<DumpInfo> dumpInfos) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Context featureDumpTime = taggedMetricRegistry.timer("uservideo.exploredump.latency").time();
        for (DumpInfo dumpInfo : dumpInfos) {
            dump(dumpInfo);
        }
        featureDumpTime.stop();
    }

    public void dump(DumpInfo dumpInfo) {
        Optional<String> dumpJsonOpt = JsonUtils.toJson(dumpInfo);
        dumpJsonOpt.ifPresent(info -> kafkaClient.send(TOPIC, String.valueOf(info.hashCode()), info));
    }

    public enum InfoType{
        explore_video, ensure_video
    }
    @Getter
    public static class DumpInfo {
        List<String> ids;
        String type;
        long ts;

        public DumpInfo(List<String> ids, String type, long ts) {
            this.ids = ids;
            this.type = type;
            this.ts = ts;
        }
    }

    public static void main(String[] args) {
        ExploreDumper featureDumper = new ExploreDumper();
        List<IFeature> features = new ArrayList<>();
        features.add(new Feature("u:cat_cs:体育", 0.2));
        features.add(new Feature("u:cat_ck:体育", 0.1));
        features.add(new Feature("u:cat_cs:军事", 0.4));

//        featureDumper.dump("221", "123", "32323", features, 0.2);
    }
}
