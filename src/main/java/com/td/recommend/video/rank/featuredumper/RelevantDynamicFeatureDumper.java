package com.td.recommend.video.rank.featuredumper;

import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.td.recommend.commons.json.JsonUtils;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.video.rank.featuredumper.bean.RelevantDynamicDumpInfo;
import com.td.recommend.video.utils.KafkaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class RelevantDynamicFeatureDumper {
    private static final Logger LOG = LoggerFactory.getLogger("FeatureDumper");
    private static final String tab = "\t";
    private static final ExecutorService dumpExecutor= new ThreadPoolExecutor(8, 15,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(5000),
            new ThreadFactoryBuilder().setNameFormat("feature-dump-%d").build());

    private static RelevantDynamicFeatureDumper instance =  new RelevantDynamicFeatureDumper();

    private KafkaClient kafkaClient;
    private static final String TOPIC = "relevant_dynamic_features";

    public static RelevantDynamicFeatureDumper getInstance() {
        return instance;
    }

    private RelevantDynamicFeatureDumper() {
        kafkaClient = KafkaClient.getInstance();
    }

    public void asyncDumpAll(List<RelevantDynamicDumpInfo> relevantDumpInfos) {
        CompletableFuture.runAsync(() -> dumpAll(relevantDumpInfos), dumpExecutor);
    }
    public void asyncDump(RelevantDynamicDumpInfo relevantDumpInfo){
        CompletableFuture.runAsync(() -> dump(relevantDumpInfo), dumpExecutor);
    }

    public void dumpAll(List<RelevantDynamicDumpInfo> relevantDumpInfos) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Timer.Context featureDumpTime = taggedMetricRegistry.timer("relevant.dynamic_featuredump.latency").time();

        int dumpTopK = Math.max(250, relevantDumpInfos.size());
        relevantDumpInfos.sort(Comparator.comparing(RelevantDynamicDumpInfo::getPredictScore).reversed());

        int index = 0;
        for (RelevantDynamicDumpInfo relevantDumpInfo : relevantDumpInfos) {
            dump(relevantDumpInfo);

            if (index > dumpTopK) {
                break;
            }
            ++index;
        }

        featureDumpTime.stop();
    }

    public void dump(RelevantDynamicDumpInfo relevantDumpInfo) {
        Optional<String> dumpJsonOpt = JsonUtils.toJson(relevantDumpInfo);
        if (dumpJsonOpt.isPresent()) {
            String currentItemId = relevantDumpInfo.getCurrentDocId()!= null? relevantDumpInfo.getCurrentDocId() : "";
            String predictId = relevantDumpInfo.getPredictId() != null? relevantDumpInfo.getPredictId() : "";
            String userId = relevantDumpInfo.getUserId() != null? relevantDumpInfo.getUserId() : "";
            String docId = relevantDumpInfo.getDocId() != null? relevantDumpInfo.getDocId() : "";
            String dumpJsonStr = predictId.concat(tab)
                    .concat(userId).concat(tab)
                    .concat(currentItemId).concat(tab)
                    .concat(docId).concat(tab)
                    .concat(dumpJsonOpt.get());
            kafkaClient.send(TOPIC, userId, dumpJsonStr);
        }
    }


}
