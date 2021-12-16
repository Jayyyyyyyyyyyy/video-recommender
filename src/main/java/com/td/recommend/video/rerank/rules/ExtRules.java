package com.td.recommend.video.rerank.rules;

import com.td.recommend.commons.rerank.RatioRule;
import com.td.recommend.commons.rerank.RatioRuleBuilder;
import com.td.recommend.commons.rerank.RuleTag;
import com.td.recommend.commons.rerank.WindowType;

/**
 * create by pansm at 2019/08/13
 */
public class ExtRules {
    public static final RatioRule extRule;

    static {
        extRule = RatioRuleBuilder.create()
                .setId(101)
                .setPriority(3)
                .setWindowSize(4)
                .setWindowType(WindowType.slide)
                .setRuleTag(new RuleTag("ext", "retrieve"))
                .setMax(1)
                .build();
    }
}
