package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.recommend.commons.retriever.KeyTag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.*;

import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.*;

/**
 * Created by admin on 2017/6/20.
 */
public class CatFeatureScorer implements RetrieveKeyScorer {

    private final VideoRecommenderContext recommendContext;
    private List<RetrieveFeatureScorer<CatRetrieveKeyFeature>> featureScorers;

    public CatFeatureScorer(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
        featureScorers = Arrays.asList(new LongTermScorer(), new ShortTermScorer(), new AppScorer());
    }

    @Override
    public void score(RetrieveKey retrieveKey, Map<String, Double> features) {
        CatRetrieveKeyFeature feature =
                new CatRetrieveKeyFeature(retrieveKey.getKey(), features);

        calBaseScore(retrieveKey, feature);

        double score = retrieveKey.getScore() * 100 + calRankScore(feature);

        retrieveKey.setScore(score);
    }

    private double calRankScore(CatRetrieveKeyFeature feature) {
        return 1.0 * feature.rawctr + 1.0 * feature.vrawctr;
    }

    private void calBaseScore(RetrieveKey retrieveKey, CatRetrieveKeyFeature feature) {
        if (feature.cat.equals("其他")) {
            retrieveKey.setScore(-1);
            return;
        }

        if (feature.cat_ck_negcnt > 10) {
            retrieveKey.addTag(KeyTag.confident.name());
        }

        List<ScoreInfo> scoreInfos = new ArrayList<>();

        for (RetrieveFeatureScorer<CatRetrieveKeyFeature> featureScorer : featureScorers) {
            Optional<ScoreInfo> scoreInfoOpt = featureScorer.score(feature);
            if (scoreInfoOpt.isPresent()) {
                scoreInfos.add(scoreInfoOpt.get());
            }
        }

        new ScoreInfos(scoreInfos).apply(retrieveKey);
    }


    private class LongTermScorer implements RetrieveFeatureScorer<CatRetrieveKeyFeature> {
        @Override
        public Optional<ScoreInfo> score(CatRetrieveKeyFeature feature) {
            if (feature.vrawctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(2.0, "vrawctr > 0.2", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.rawctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(2.0, "rawctr > 0.2",KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.vcat_ck > 0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.2, "vcat_ck > 0", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.rawctr > 0.14) {
                ScoreInfo scoreInfo = new ScoreInfo(1.0, "rawctr > 0.14", KeyTag.news,longTerm);
                return Optional.of(scoreInfo);
            }


            if (feature.cat_ck > 0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.2, "cat_ck > 0", KeyTag.news,longTerm);
                return Optional.of(scoreInfo);
            }


            if (feature.vcat_cs > 1.5) {
                ScoreInfo scoreInfo = new ScoreInfo(0.1, "vcat_cs > 1.5", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.cat_cs > 1.5) {
                ScoreInfo scoreInfo = new ScoreInfo(0.1, "cat_cs > 1.5",KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }


            if (feature.cat_cs > 0 && feature.cat_ck_poscnt > 10) {
                ScoreInfo scoreInfo = new ScoreInfo(0.0, "cat_cs > 0 && cat_ck_poscnt > 10", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }


            return Optional.empty();
        }
    }

    private class AppScorer implements RetrieveFeatureScorer<CatRetrieveKeyFeature> {
        @Override
        public Optional<ScoreInfo> score(CatRetrieveKeyFeature feature) {
            if (feature.app_ct > 0.1 && feature.cat_ck_negcnt < 10) {
                ScoreInfo scoreInfo = new ScoreInfo(0.0, "app_ct > 0.1 && cat_ck_negcnt < 10", KeyTag.app_cat, thirdParty);
                return Optional.of(scoreInfo);
            }

            if (feature.app_ecat > 0.8 && feature.cat_ck_negcnt < 10) {
                ScoreInfo scoreInfo = new ScoreInfo(0.0, "app_ecat > 0.8 && cat_ck_negcnt < 10", KeyTag.app_ecat, thirdParty);
                return Optional.of(scoreInfo);
            }

            return Optional.empty();
        }
    }

    private class ShortTermScorer implements RetrieveFeatureScorer<CatRetrieveKeyFeature> {
        public Optional<ScoreInfo> score(CatRetrieveKeyFeature feature) {
            if (feature.cat_st_ck > 0.2) {
                return Optional.of(new ScoreInfo(1.0, "short term rawctr > 0.2", KeyTag.news, shortTerm));
            }

            if (feature.cat_st_ck_negcnt < 10 && feature.cat_st_ck_poscnt > 0) {
                return Optional.of(new ScoreInfo(0.0, "cat_st_ck_negcnt < 10 && cat_st_ck_poscnt > 0", KeyTag.news, shortTerm));
            }

            return Optional.empty();
        }
    }
}
