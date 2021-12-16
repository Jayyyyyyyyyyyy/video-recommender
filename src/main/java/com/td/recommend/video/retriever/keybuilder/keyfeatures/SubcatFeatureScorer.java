package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.recommend.commons.retriever.KeyTag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.*;

import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.longTerm;
import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.shortTerm;

/**
 * Created by admin on 2017/6/22.
 */
public class SubcatFeatureScorer implements RetrieveKeyScorer {
    private final VideoRecommenderContext recommendContext;
    private List<RetrieveFeatureScorer<SubcatRetrieveKeyFeature>> featureScorers;

    public SubcatFeatureScorer(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
        this.featureScorers = Arrays.asList(new LongTermScorer(), new ShortTermScorer());
    }

    @Override
    public void score(RetrieveKey retrieveKey, Map<String, Double> features) {
        SubcatRetrieveKeyFeature feature = new SubcatRetrieveKeyFeature(retrieveKey.getKey(), features);

        calBaseScore(retrieveKey, feature);
        double score = retrieveKey.getScore() * 100.0 + calRankScore(feature);
        retrieveKey.setScore(score);
    }

    private double calRankScore(SubcatRetrieveKeyFeature feature) {
        return feature.rawctr + feature.vrawctr;
    }

    private void calBaseScore(RetrieveKey retrieveKey, SubcatRetrieveKeyFeature feature) {
        if (feature.subcat_ck_negcnt > 10) {
            retrieveKey.addTag(KeyTag.confident.name());
        }

        if (feature.subcat_ck_negcnt > 15 && feature.subcat_ck_poscnt < 1) {
            retrieveKey.setScore(-1);
            return;
        }


        List<ScoreInfo> scoreInfos = new ArrayList<>();
        for (RetrieveFeatureScorer<SubcatRetrieveKeyFeature> featureScorer : featureScorers) {
            Optional<ScoreInfo> scoreInfoOpt = featureScorer.score(feature);
            if (scoreInfoOpt.isPresent()) {
                ScoreInfo scoreInfo = scoreInfoOpt.get();
                scoreInfos.add(scoreInfo);
            }
        }

        new ScoreInfos(scoreInfos).apply(retrieveKey);
    }

    private class LongTermScorer implements RetrieveFeatureScorer<SubcatRetrieveKeyFeature> {

        @Override
        public Optional<ScoreInfo> score(SubcatRetrieveKeyFeature feature) {
            if (feature.vrawctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(2.0, "vrawctr > 0.2", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.rawctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(2.0, "rawctr > 0.2", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }


            if (feature.rawctr > 0.14) {
                ScoreInfo scoreInfo = new ScoreInfo(1.0, "rawctr > 0.14", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.vsubcat_ck > 0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.2, "vsubcat_ck > 0", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.vsubcat_cs > 1.5) {
                ScoreInfo scoreInfo = new ScoreInfo(0.1, "vsubcat_cs > 1.5", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.subcat_ck > 0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.2, "subcat_ck > 0", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.subcat_cs > 1.5) {
                ScoreInfo scoreInfo = new ScoreInfo(0.1, "subcat_cs > 1.5", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }


            return Optional.empty();
        }
    }


    private class ShortTermScorer implements RetrieveFeatureScorer<SubcatRetrieveKeyFeature> {

        @Override
        public Optional<ScoreInfo> score(SubcatRetrieveKeyFeature feature) {
            if (feature.st_raw_ctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(1.0, "st_raw_ctr > 0.2", KeyTag.news, shortTerm);
                return Optional.of(scoreInfo);
            }

            return Optional.empty();
        }
    }
}
