package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.recommend.commons.retriever.KeyTag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.*;

import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.longTerm;
import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.shortTerm;
import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.thirdParty;

/**
 * Created by admin on 2017/6/22.
 */
public class TagFeatureScorer implements RetrieveKeyScorer {
    private final VideoRecommenderContext recommendContext;
    private List<RetrieveFeatureScorer<TagRetrieveKeyFeature>> retrieveFeatureScorers;

    public TagFeatureScorer(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
        retrieveFeatureScorers = Arrays.asList(new LongTermScorer(), new ShortTermScorer(), new ThirdPartyScorer());
    }

    @Override
    public void score(RetrieveKey retrieveKey, Map<String, Double> features) {
        TagRetrieveKeyFeature feature = new TagRetrieveKeyFeature(retrieveKey.getKey(), features);

        calBaseScore(retrieveKey, feature);
        double score = retrieveKey.getScore() * 100.0 + rankScore(feature);
        retrieveKey.setScore(score);
    }

    private double rankScore(TagRetrieveKeyFeature feature) {
        return feature.rawctr + feature.vrawctr;
    }

    private void calBaseScore(RetrieveKey retrieveKey, TagRetrieveKeyFeature feature) {
        if (feature.tag_ck_negcnt > 5) {
            retrieveKey.addTag(KeyTag.confident.name());
        }

        if (feature.tag_st_ck_negcnt > 5 && feature.tag_ck_poscnt < 1) {
            retrieveKey.setReason("feature.tag_st_ck_negcnt > 5 && feature.tag_ck_poscnt < 1");
            retrieveKey.setScore(-1);
            return;
        }

        if (feature.tag_ck_negcnt > 10 && feature.tag_ck_poscnt <= 1) {
            retrieveKey.setScore(-1);
            return;
        }

        List<ScoreInfo> scoreInfos = new ArrayList<>();
        for (RetrieveFeatureScorer<TagRetrieveKeyFeature> retrieveFeatureScorer : retrieveFeatureScorers) {
            Optional<ScoreInfo> scoreInfoOpt = retrieveFeatureScorer.score(feature);
            if (scoreInfoOpt.isPresent()) {
                scoreInfos.add(scoreInfoOpt.get());
            }
        }

        new ScoreInfos(scoreInfos).apply(retrieveKey);
    }


    private class LongTermScorer implements RetrieveFeatureScorer<TagRetrieveKeyFeature> {

        @Override
        public Optional<ScoreInfo> score(TagRetrieveKeyFeature feature) {

            if (feature.rawctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(2.0, "rawctr > 0.2", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.vrawctr > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(2.0, "vrawctr > 0.2", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }


            if (feature.rawctr > 0.14) {
                ScoreInfo scoreInfo = new ScoreInfo(1.0, "rawctr > 0.14", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.tag_ck > 0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.2, "tag_ck > 0", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.tag_cs > 1.5) {
                ScoreInfo scoreInfo = new ScoreInfo(0.1, "tag_cs > 1.5", KeyTag.news, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.vtag_ck > 0) {
                ScoreInfo scoreInfo = new ScoreInfo(0.2, "vtag_ck > 0", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            if (feature.vtag_cs > 1.5) {
                ScoreInfo scoreInfo = new ScoreInfo(0.1, "vtag_cs > 1.5", KeyTag.video, longTerm);
                return Optional.of(scoreInfo);
            }

            return Optional.empty();
        }
    }

    private class ShortTermScorer implements RetrieveFeatureScorer<TagRetrieveKeyFeature> {

        @Override
        public Optional<ScoreInfo> score(TagRetrieveKeyFeature feature) {
            if (feature.tag_st_ck > 0.2) {
                ScoreInfo scoreInfo = new ScoreInfo(1.0, "short term rawctr > 0.2", shortTerm);
                return Optional.of(scoreInfo);
            }

            return Optional.empty();
        }
    }

    private class ThirdPartyScorer implements RetrieveFeatureScorer<TagRetrieveKeyFeature> {

        @Override
        public Optional<ScoreInfo> score(TagRetrieveKeyFeature feature) {
            if (feature.app_etag > 0.8 && feature.tag_ck_negcnt < 10) {
                    ScoreInfo scoreInfo = new ScoreInfo(1.0, "app_etag > 0.8 && tag_ck_negcnt < 10", KeyTag.app_etag,  thirdParty);
                    return Optional.of(scoreInfo);
            }

            return Optional.empty();
        }
    }
}
