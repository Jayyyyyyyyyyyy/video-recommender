package com.td.recommend.video.rank.predictor;

import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.idgenerator.PredictIdGenerator;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/8/3.
 */
public class BackupPredictor implements IPredictor<DocItem> {
    private static Logger LOG = LoggerFactory.getLogger(BackupPredictor.class);
    private static ForkJoinPool forkJoinPool = new ForkJoinPool(24);
    private static final int timeout = 200;

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Timer.Context predictTime = taggedMetricRegistry.timer("uservideo.localpredict.latency").time();
        UserItem userItem = UserProfileUtils.getUserItem(queryItems);
        String predictId = PredictIdGenerator.getInstance().generate(userItem.getId());
        ConcurrentHashMap<String, Double> concurrentHashMap = new ConcurrentHashMap<>();

        ForkJoinTask<?> task = forkJoinPool.submit(() ->
                predictItems.getItems().parallelStream().forEach(item -> rank(concurrentHashMap, item))
        );

        PredictResult predictResult = new PredictResult();

        try {
            task.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOG.error("backup predictor failed in timeout={}", timeout, e);
        }

        List<Double> scoreList = predictItems.getItems().stream()
                .map(item -> concurrentHashMap.getOrDefault(item.getId(), 0.0))
                .collect(Collectors.toList());
        predictResult.setScores(scoreList);
        predictResult.setPredictId(predictId);//null will not be cached
        predictItems.setModelName("backup");

        predictTime.stop();
        return predictResult;

    }


    private void rank(ConcurrentHashMap<String, Double> concurrentHashMap, PredictItem<DocItem> predictItem) {
        double fresh = freshWeight(predictItem);
        double ctr = DocProfileUtils.getFeedDcCtr(predictItem.getItem());
        double star = DocProfileUtils.getTalentStar(predictItem.getItem());

        double score = fresh * ctr * (star / 10 + 1);
        concurrentHashMap.put(predictItem.getId(), score);
    }

    private double freshWeight(PredictItem<DocItem> predictItem) {
        LocalDateTime createDateTime = DocProfileUtils.getCreateDateTime(predictItem.getItem());
        LocalDateTime now = LocalDateTime.now();
        long monthDiff = ChronoUnit.MONTHS.between(createDateTime, now);
        return Math.pow(0.9, monthDiff);
    }

    public static void main(String[] args) {
        LocalDateTime fromDateTime = LocalDateTime.of(2020, 10, 1, 7, 45, 55);
        LocalDateTime toDateTime = LocalDateTime.of(2021, 10, 1, 6, 40, 45);
        long between = ChronoUnit.MONTHS.between(fromDateTime, toDateTime);
        System.out.println(between);
        System.out.println(Math.pow(0.9, between));
    }
}
