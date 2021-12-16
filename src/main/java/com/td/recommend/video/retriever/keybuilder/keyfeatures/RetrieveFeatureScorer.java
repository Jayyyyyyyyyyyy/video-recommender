package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import java.util.Optional;

/**
 * Created by admin on 2018/3/6.
 */
public interface RetrieveFeatureScorer<T> {
    Optional<ScoreInfo> score(T feature);
}
