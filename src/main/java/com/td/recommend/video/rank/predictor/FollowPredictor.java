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
public class FollowPredictor implements IPredictor<DocItem> {
    private static Logger LOG = LoggerFactory.getLogger(FollowPredictor.class);
    private static ForkJoinPool forkJoinPool = new ForkJoinPool(24);
    private static final int timeout = 200;

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Timer.Context predictTime = taggedMetricRegistry.timer("follow.localpredict.latency").time();
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
            LOG.error("follow predictor failed in timeout={}", timeout, e);
        }

        List<Double> scoreList = predictItems.getItems().stream()
                .map(item -> concurrentHashMap.getOrDefault(item.getId(), 0.0))
                .collect(Collectors.toList());
        predictResult.setScores(scoreList);
        predictResult.setPredictId(predictId);//null will not be cached
        predictItems.setModelName("follow");

        predictTime.stop();
        return predictResult;

    }


    private void rank(ConcurrentHashMap<String, Double> concurrentHashMap, PredictItem<DocItem> predictItem) {
        double days = daysWeight(predictItem);
        double minute = minuteWeight(predictItem);
        double teach = teachWeight(predictItem);
        double follow = strongFollowWeight(predictItem);

        double score = days * 100 + teach * 10 + follow * 5 + minute;
        concurrentHashMap.put(predictItem.getId(), score);
    }

    private double daysWeight(PredictItem<DocItem> predictItem) {
        LocalDateTime createDateTime = DocProfileUtils.getCreateDateTime(predictItem.getItem());
        LocalDateTime now = LocalDateTime.now();
        long dayDiff = ChronoUnit.DAYS.between(createDateTime, now);
        if (dayDiff > 300) {
            dayDiff = 300;
        }
        return 100 - (float) (dayDiff / 3);
    }

    private double minuteWeight(PredictItem<DocItem> predictItem) {
        LocalDateTime createDateTime = DocProfileUtils.getCreateDateTime(predictItem.getItem());
        LocalDateTime now = LocalDateTime.now();
        long diff = ChronoUnit.MINUTES.between(createDateTime, now);
        return 1.0 / (diff + 1);
    }

    private double teachWeight(PredictItem<DocItem> predictItem) {
        String contentTeach = DocProfileUtils.getContentTeach(predictItem.getItem());
        if (contentTeach.equals("教学")) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    private double strongFollowWeight(PredictItem<DocItem> predictItem) {
        if (predictItem.getRetrieveKeys().stream().anyMatch(k -> k.getType().equals("vfollow_ext"))) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    public static void main(String[] args) {
        LocalDateTime fromDateTime = LocalDateTime.of(2020, 10, 1, 7, 45, 55);
        LocalDateTime toDateTime = LocalDateTime.of(2020, 10, 1, 7, 40, 45);
        long between = ChronoUnit.DAYS.between(fromDateTime, toDateTime);
        System.out.println(between);
        System.out.println(Math.pow(0.9, between));
    }
}
