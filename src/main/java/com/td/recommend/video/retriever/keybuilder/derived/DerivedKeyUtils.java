package com.td.recommend.video.retriever.keybuilder.derived;


import com.td.recommend.commons.retriever.RetrieveKey;

/**
 * Created by admin on 2017/10/30.
 */
public class DerivedKeyUtils {
    public static RetrieveKey createDerivedKey(RetrieveKey originKey, String type, String key) {
        RetrieveKey derivedRetrieveKey = new RetrieveKey();
        derivedRetrieveKey.setKey(key);
        derivedRetrieveKey.setType(type);
        derivedRetrieveKey.setScore(originKey.getScore());
        derivedRetrieveKey.setReason(originKey.getReason());
        derivedRetrieveKey.addTags(originKey.getTags());

        return derivedRetrieveKey;
    }
}
