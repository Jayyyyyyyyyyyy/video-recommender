package com.td.recommend.video.rerank;

import com.td.recommend.commons.rerank.RatioRule;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.List;

/**
 * Created by admin on 2017/10/9.
 */
public interface RatioRulePlugin {
    List<RatioRule> apply(List<RatioRule> ratioRules, VideoRecommenderContext recommendContext);
}
