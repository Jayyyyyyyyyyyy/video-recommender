package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.recommend.commons.retriever.KeyTag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.Map;

/**
 * Created by admin on 2017/10/11.
 */
public class MediaFeatureScorer implements RetrieveKeyScorer {
    private VideoRecommenderContext recommendContext;

    public MediaFeatureScorer(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void score(RetrieveKey retrieveKey, Map<String, Double> features) {
        MediaRetrieveKeyFeature feature = new MediaRetrieveKeyFeature(retrieveKey.getKey(), features);

        calBaseScore(retrieveKey, feature);
        double score = retrieveKey.getScore() * 100.0 + calRankScore(feature);
        retrieveKey.setScore(score);
    }

    private double calRankScore(MediaRetrieveKeyFeature feature) {
        return Math.max(feature.inner_source_rawctr, feature.media_name_rawctr);
    }

    private void calBaseScore(RetrieveKey retrieveKey, MediaRetrieveKeyFeature feature) {
        if (feature.media_name_ck_negcnt > 10 || feature.inner_source_ck_negcnt > 10) {
            retrieveKey.addTag(KeyTag.confident.name());
        }


        if (feature.media_name_ck_negcnt > 10 && feature.media_name_ck_poscnt < 1) {
            retrieveKey.setScore(-1);
            return;
        }

        if (feature.inner_source_ck_negcnt > 10 && feature.inner_source_ck_poscnt < 1) {
            retrieveKey.setScore(-1);
            return;
        }

        if (feature.media_name_rawctr > 0.2) {
            retrieveKey.setScore(1.0);
            retrieveKey.setReason("media_name_rawctr > 0.2");
            return;
        }

        if (feature.inner_source_rawctr > 0.2) {
            retrieveKey.setScore(1.0);
            retrieveKey.setReason("inner_source_rawctr > 0.2");
            return;
        }

        if (feature.media_name_rawctr > 0.14) {
            retrieveKey.setScore(0.0);
            retrieveKey.setReason("media_name_rawctr > 0.14");
            return;
        }

        if (feature.inner_source_rawctr > 0.14) {
            retrieveKey.setScore(0.0);
            retrieveKey.setReason("inner_source_rawctr > 0.14");
            return;
        }

        if (feature.media_name_ck < -0.5) {
            retrieveKey.setScore(-1.0);
            retrieveKey.setReason("media_name_ck < -0.5");
            return;
        }

        if (feature.inner_source_ck < -0.5) {
            retrieveKey.setScore(-1.0);
            retrieveKey.setReason("inner_source_ck < -0.5");
            return;
        }

        //TODO: should replace by short term
        if (feature.media_name_cs > 1.5) {
            retrieveKey.setScore(0.0);
            retrieveKey.setReason("media_name_cs > 1.5");
            return;
        }

        if (feature.inner_source_cs > 1.5) {
            retrieveKey.setScore(0.0);
            retrieveKey.setReason("inner_source_cs > 1.5");
            return;
        }

        if (feature.media_name_ck > 0) {
            retrieveKey.setScore(0.1);
            retrieveKey.setReason("media_name_ck > 0");
            return;
        }

        if (feature.inner_source_ck > 0) {
            retrieveKey.setScore(0.1);
            retrieveKey.setReason("inner_source_ck > 0");
            return;
        }

        retrieveKey.setScore(-1);
    }
}
