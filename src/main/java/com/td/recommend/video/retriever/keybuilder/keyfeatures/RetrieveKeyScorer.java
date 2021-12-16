package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.recommend.commons.retriever.RetrieveKey;

import java.util.Map;

/**
 * Created by admin on 2017/6/20.
 */
public interface RetrieveKeyScorer {
    void score(RetrieveKey retrieveKey, Map<String, Double> features);
}
