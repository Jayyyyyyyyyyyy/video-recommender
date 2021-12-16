package com.td.recommend.video.rank.monitor;

import com.google.common.collect.Lists;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.td.rank.deepfm.felib.MtlConfig;
import com.td.rank.deepfm.felib.MtlConfigV2;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class DNNPredictMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(DNNPredictMonitor.class);

    private DNNPredictMonitor() {
    }

    private static final ExecutorService monitorExecutor = new ThreadPoolExecutor(8, 15,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(5000),
            new ThreadFactoryBuilder().setNameFormat("predict-monitor-%d").build());

    private static final DNNPredictMonitor predictMonitor = new DNNPredictMonitor();

    public static DNNPredictMonitor getInstance() {
        return predictMonitor;
    }

    public void asyncMonitor(List<Float> predictList, String model) {
        if (predictList != null && predictList.size() > 0) {
            CompletableFuture.runAsync(() -> monitor(predictList, model, ""), monitorExecutor);
        }
    }

    public void asyncMonitor(float[][] predictArray, MtlConfig mtlConfig, String model) {
        if (predictArray != null && predictArray.length > 0) {
            CompletableFuture.runAsync(() -> mtlMonitor(predictArray, mtlConfig, model), monitorExecutor);
        }
    }

    public void asyncMonitor(float[][] predictArray, MtlConfigV2 mtlConfigV2, String model) {
        if (predictArray != null && predictArray.length > 0) {
            CompletableFuture.runAsync(() -> mtlMonitor(predictArray, mtlConfigV2, model), monitorExecutor);
        }
    }

    private void mtlMonitor(float[][] predictArray, MtlConfig mtlConfig, String model) {
        for (int i = 0; i < mtlConfig.size(); i++) {
            try {
                String taskName = String.valueOf(mtlConfig.task(i));
                List<Float> scoreList = Lists.newArrayList();
                for (float[] floats : predictArray) {
                    scoreList.add(floats[i]);
                }
                monitor(scoreList, model, taskName);
            } catch (Exception e) {
                LOG.error("felib get taskName error", e);
            }
        }
    }

    private void mtlMonitor(float[][] predictArray, MtlConfigV2 mtlConfigV2, String model) {
        for (int i = 0; i < mtlConfigV2.size(); i++) {
            try {
                String taskName = String.valueOf(mtlConfigV2.task(i));
                List<Float> scoreList = Lists.newArrayList();
                for (float[] floats : predictArray) {
                    scoreList.add(floats[i]);
                }
                monitor(scoreList, model, taskName);
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
                taggedMetricRegistry.histogram("monitor." + model + ".predict." + task + "score").update(scoreL);
            }
        });
    }
}
