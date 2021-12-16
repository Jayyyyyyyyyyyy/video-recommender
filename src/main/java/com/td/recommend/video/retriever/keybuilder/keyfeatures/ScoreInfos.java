package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.recommend.commons.retriever.KeyTag;
import com.td.recommend.commons.retriever.RetrieveKey;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.longTerm;
import static com.td.recommend.video.retriever.keybuilder.keyfeatures.ScoreInfo.FeatureType.shortTerm;

/**
 * Created by admin on 2018/3/6.
 */
public class ScoreInfos {
    private List<ScoreInfo> scoreInfos;

    public ScoreInfos(List<ScoreInfo> scoreInfos) {
        this.scoreInfos = scoreInfos;
    }

    public void apply(RetrieveKey retrieveKey) {
        if (scoreInfos.isEmpty()) {
            retrieveKey.setScore(-1.0);
        } else if (scoreInfos.size() == 1) {
            ScoreInfo scoreInfo = scoreInfos.get(0);
            retrieveKey.setScore(scoreInfo.getScore()).setReason(scoreInfo.getReason());

            List<KeyTag> keyTags = scoreInfo.getKeyTags();
            if (keyTags != null && !keyTags.isEmpty()) {
                for (KeyTag keyTag : keyTags) {
                    retrieveKey.addTag(keyTag.name());
                }
            }

            if (scoreInfo.getFeatureType() == longTerm) {
                retrieveKey.addTag(KeyTag.long_term.name());
            } else if (scoreInfo.getFeatureType() == shortTerm){
                retrieveKey.addTag(KeyTag.short_term.name());
            }
        } else {
            //ScoreInfo scoreInfo = Collections.max(scoreInfos, (o1, o2) -> Double.compare(o1.getScore(), o2.getScore()));
            ScoreInfo scoreInfo = Collections.max(scoreInfos, Comparator.comparingDouble(ScoreInfo::getScore));

            String reason = scoreInfos.stream().map(ScoreInfo::getReason).collect(Collectors.joining(" && "));

            retrieveKey.setScore(scoreInfo.getScore()).setReason(reason);
            List<KeyTag> keyTags = scoreInfo.getKeyTags();
            if (keyTags != null && !keyTags.isEmpty()) {
                for (KeyTag keyTag : keyTags) {
                    retrieveKey.addTag(keyTag.name());
                }
            }
        }
    }
}
