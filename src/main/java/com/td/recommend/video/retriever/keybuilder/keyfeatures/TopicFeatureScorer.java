package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.recommend.commons.retriever.KeyTag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.*;

import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.longTerm;
import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.shortTerm;

/**
 * Created by admin on 2017/7/15.
 */
public class TopicFeatureScorer implements RetrieveKeyScorer {
    private final VideoRecommenderContext recommendContext;
    private String topicType;
    private List<RetrieveFeatureScorer<TopicRetrieveKeyFeature>> featureScorers;

    public TopicFeatureScorer(VideoRecommenderContext recommendContext, String topicType) {
        this.topicType = topicType;
        this.recommendContext = recommendContext;
        featureScorers = Arrays.asList(new LongTermScorer(), new ShortTermScorer());
    }

    @Override
    public void score(RetrieveKey retrieveKey, Map<String, Double> features) {
        TopicRetrieveKeyFeature feature = new TopicRetrieveKeyFeature(retrieveKey, topicType, features);

        calBaseScore(retrieveKey, feature);
        double score = retrieveKey.getScore() * 100.0 + calRankScore(feature);
        retrieveKey.setScore(score);
    }

    private double calRankScore(TopicRetrieveKeyFeature feature) {
        return feature.rawctr + feature.vrawctr;
    }

    private void calBaseScore(RetrieveKey retrieveKey, TopicRetrieveKeyFeature feature) {
        if (feature.topic_ck_negcnt > 10) {
            retrieveKey.addTag(KeyTag.confident.name());
        }

        if (feature.topic_st_ck_negcnt > 6 && feature.topic_st_ck_poscnt < 1) {
            retrieveKey.setScore(-1.0);
            retrieveKey.setReason("topic_st_ck_negcnt > 5 && topic_st_ck_poscnt < 1");
            return;
        }

        if (feature.topic_ck_negcnt > 15 && feature.topic_ck_poscnt < 2) {
            retrieveKey.setReason("topic_ck_negcnt > 15 && topic_ck_poscnt < 2");
            retrieveKey.setScore(-1);
            return;
        }

        List<ScoreInfo> scoreInfos = new ArrayList<>();
        for (RetrieveFeatureScorer<TopicRetrieveKeyFeature> featureScorer : featureScorers) {
            Optional<ScoreInfo> scoreInfoOpt = featureScorer.score(feature);
            if (scoreInfoOpt.isPresent()) {
                ScoreInfo scoreInfo = scoreInfoOpt.get();
                scoreInfos.add(scoreInfo);
            }
        }

        new ScoreInfos(scoreInfos).apply(retrieveKey);
    }


    private class LongTermScorer implements RetrieveFeatureScorer<TopicRetrieveKeyFeature> {

        @Override
        public Optional<ScoreInfo> score(TopicRetrieveKeyFeature feature) {

            if (feature.rawctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(2.0, "rawctr > 0.2", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.vrawctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(2.0, "vrawctr > 0.2", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.vtopic_ck > 0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.2, "vtopic_ck > 0", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.rawctr > 0.14) {
                ScoreInfo scoreInfo = new ScoreInfo(1.0, "rawctr > 0.14", KeyTag.news,longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.vtopic_cs > 2.0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.1, "vtopic_cs > 2.0", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.topic_ck > 0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.2, "topic_ck > 0", KeyTag.news,longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.topic_cs > 2.0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.1, "topic_cs > 2.0", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }


            return Optional.empty();
        }
    }

    private class ShortTermScorer implements RetrieveFeatureScorer<TopicRetrieveKeyFeature> {

        @Override
        public Optional<ScoreInfo> score(TopicRetrieveKeyFeature feature) {
            if (feature.st_raw_ctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(1.0, "st_raw_ctr > 0.2", shortTerm);
                return Optional.of(scoreInfo);
            }

            return Optional.empty();
        }
    }
}
