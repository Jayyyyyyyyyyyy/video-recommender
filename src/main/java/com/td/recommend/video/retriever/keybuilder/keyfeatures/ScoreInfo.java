package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.recommend.commons.retriever.KeyTag;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2018/3/6.
 */
@Getter
public class ScoreInfo {
    private FeatureType featureType;
    private double score;
    private String reason;
    private List<KeyTag> keyTags;

    public ScoreInfo(double score, String reason, List<KeyTag> keyTags, FeatureType featureType) {
        this.score = score;
        this.reason = reason;
        this.keyTags = keyTags;
        this.featureType = featureType;
    }

    public ScoreInfo(double score, String reason, KeyTag keyTag, FeatureType featureType) {
        this.score = score;
        this.reason = reason;
        this.keyTags = Arrays.asList(keyTag);
        this.featureType = featureType;
    }

    public ScoreInfo(double score, String reason, FeatureType featureType) {
        this(score, reason, Collections.emptyList(), featureType);
    }

    enum FeatureType {
        longTerm, shortTerm, thirdParty
    }

}
