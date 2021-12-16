package com.td.recommend.video.rerank.monitor;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.td.rank.deepfm.felib.MtlConfig;
import com.td.rerank.dnn.felib.*;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class RerankDNNPredictMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(RerankDNNPredictMonitor.class);

    private RerankDNNPredictMonitor() {
    }

    private static final ExecutorService monitorExecutor = new ThreadPoolExecutor(8, 15,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(5000),
            new ThreadFactoryBuilder().setNameFormat("rerank-predict-monitor-%d").build());

    private static final RerankDNNPredictMonitor predictMonitor = new RerankDNNPredictMonitor();

    public static RerankDNNPredictMonitor getInstance() {
        return predictMonitor;
    }

    public void asyncMonitor(List<Float> predictList, String model) {
        if (predictList != null && predictList.size() > 0) {
            CompletableFuture.runAsync(() -> monitor(predictList, model, ""), monitorExecutor);
        }
    }

    public void asyncMonitor(float[][] predictArray, MtlConfigV3 mtlConfigV3, String model, String rankModelName) {
        if (predictArray != null && predictArray.length > 0) {
            CompletableFuture.runAsync(() -> mtlMonitor(predictArray, mtlConfigV3, model, rankModelName), monitorExecutor);
        }
    }

    private void mtlMonitor(float[][] predictArray, MtlConfigV3 mtlConfigV3, String model, String rankModelName) {
        int taskNum = mtlConfigV3.taskNum(rankModelName);
        for (int i = 0; i < taskNum; i++) {
            try {
                if (!mtlConfigV3.isScoreFromRank(rankModelName, i)) {
                    String taskName = String.valueOf(mtlConfigV3.task(rankModelName, i));
                    List<Float> scoreList = Lists.newArrayList();
                    for (float[] floats : predictArray) {
                        int rerankScoreIndex = mtlConfigV3.rerankScoreIndex(rankModelName, i);
                        scoreList.add(floats[rerankScoreIndex]);
                    }
                    monitor(scoreList, model, taskName);
                }
            } catch (Exception e) {
                LOG.error("felib get taskName error", e);
            }
        }
    }

    public void monitor(List<Float> predictList, String model, String task) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        predictList.sort(Comparator.reverseOrder());
        if (predictList.size() > 20) {
            predictList = predictList.subList(0, 20);
        }
        Random random = new Random();
        predictList.forEach(score -> {
            if (random.nextFloat() < 0.1){
                long scoreL = Float.valueOf(score * 1000).longValue();
                taggedMetricRegistry.histogram("monitor.rerank." + model + ".predict." + task + "score").update(scoreL);
            }
        });
    }
}
