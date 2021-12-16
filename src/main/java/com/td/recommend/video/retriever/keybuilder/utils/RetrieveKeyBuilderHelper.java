package com.td.recommend.video.retriever.keybuilder.utils;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.RetrieveKeyScorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/7/15.
 */
public class RetrieveKeyBuilderHelper {
    public static List<RetrieveKey> getScoredRetrieveKeys(InterestFeatureExtractor featureExtractor, RetrieveKeyScorer featureScorer) {
        List<RetrieveKey> retrieveKeys = new ArrayList<>();
        Map<RetrieveKey, Map<String, Double>> keyFeaturesMap = featureExtractor.getKeyFeaturesMap();
        for (Map.Entry<RetrieveKey, Map<String, Double>> keyFeatureEntry : keyFeaturesMap.entrySet()) {
            RetrieveKey retrieveKey = keyFeatureEntry.getKey();
            featureScorer.score(retrieveKey, keyFeatureEntry.getValue());
            if (retrieveKey.getScore() >= 0) {
                retrieveKeys.add(retrieveKey);
            }
        }

        return retrieveKeys;
    }
}
