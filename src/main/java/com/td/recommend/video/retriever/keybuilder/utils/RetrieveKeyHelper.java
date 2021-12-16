package com.td.recommend.video.retriever.keybuilder.utils;

import java.util.Map;

/**
 * Created by admin on 2017/6/20.
 */
public class RetrieveKeyHelper {
    public static double getScore(String key, Map<String, Double> features) {
        Double value = features.get(key);
        if (value == null) {
            return 0.0;
        }

        return value;
    }
}
