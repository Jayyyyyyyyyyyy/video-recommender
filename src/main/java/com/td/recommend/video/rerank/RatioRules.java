package com.td.recommend.video.rerank;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.rerank.RatioRule;
import com.td.recommend.core.rerank.RatioRuleLoader;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.UserVideoConfig;
import com.typesafe.config.Config;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/9/26.
 */
public class RatioRules {
    private static final RatioRules instance = new RatioRules();

    private List<RatioRule> relevantRules;
    private List<RatioRule> relevantExpRules;

    private List<RatioRule> oldInterestUserRules;
    private List<RatioRule> oldBareUserRules;
    private List<RatioRule> newInterestUserRules;
    private List<RatioRule> newBareUserRules;
    private List<RatioRule> newAllUserRules;
    private List<RatioRule> feedBaseRules;
    private List<RatioRule> feedExpRules;
    private List<RatioRule> followRules;
    private List<RatioRule> channelRules;
    private List<RatioRule> smallFeedRules;
    private List<RatioRule> showDanceRules;
    private List<RatioRule> showDanceTrendBaseRules;
    private List<RatioRule> showDanceTrendStrongRules;
    private List<RatioRule> showDanceTrendWeakRules;
    private List<RatioRule> showDanceTrendNoneRules;
    private List<RatioRule> showDanceRlvtRules;
    private List<RatioRule> groupFeedRules;
    private List<RatioRule> fitPackRules;
    private List<RatioRule> wxAppBaseRules;
    private List<RatioRule> afterRerankRules;
    private RatioRuleLoader ratioRuleLoader;


    public static RatioRules getInstance() {
        return instance;
    }

    public RatioRules() {
        ratioRuleLoader = new RatioRuleLoader();
        Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
        this.oldInterestUserRules = ratioRuleLoader.load(userNewsConfig.getConfigList("old-interest-user-rules"));
        this.oldBareUserRules = ratioRuleLoader.load(userNewsConfig.getConfigList("old-bare-user-rules"));
        this.fitPackRules = ratioRuleLoader.load(userNewsConfig.getConfigList("fit-pack-rules"));
        this.newInterestUserRules = ratioRuleLoader.load(userNewsConfig.getConfigList("new-interest-user-rules"));
        this.newBareUserRules = ratioRuleLoader.load(userNewsConfig.getConfigList("new-bare-user-rules"));
        this.newAllUserRules = ratioRuleLoader.load(userNewsConfig.getConfigList("new-all-user-rules"));
        this.feedBaseRules = ratioRuleLoader.load(userNewsConfig.getConfigList("feed-base-rules"));
        this.feedExpRules = ratioRuleLoader.load(userNewsConfig.getConfigList("feed-exp-rules"));
        this.followRules = ratioRuleLoader.load(userNewsConfig.getConfigList("follow-rules"));
        this.channelRules = ratioRuleLoader.load(userNewsConfig.getConfigList("channel-rules"));
        this.smallFeedRules = ratioRuleLoader.load(userNewsConfig.getConfigList("small-feed-rules"));
        this.showDanceRules = ratioRuleLoader.load(userNewsConfig.getConfigList("show-dance-rules"));
        this.showDanceTrendBaseRules = ratioRuleLoader.load(userNewsConfig.getConfigList("show-dance-trend-base-rules"));
        this.showDanceTrendStrongRules = ratioRuleLoader.load(userNewsConfig.getConfigList("show-dance-trend-strong-rules"));
        this.showDanceTrendWeakRules = ratioRuleLoader.load(userNewsConfig.getConfigList("show-dance-trend-weak-rules"));
        this.showDanceTrendNoneRules = ratioRuleLoader.load(userNewsConfig.getConfigList("show-dance-trend-none-rules"));
        this.showDanceRlvtRules = ratioRuleLoader.load(userNewsConfig.getConfigList("show-dance-rlvt-rules"));
        this.groupFeedRules = ratioRuleLoader.load(userNewsConfig.getConfigList("group-feed-rules"));
        this.relevantRules = ratioRuleLoader.load(userNewsConfig.getConfigList("relevant-base-rules"));
        this.relevantExpRules = ratioRuleLoader.load(userNewsConfig.getConfigList("relevant-exp-rules"));
        this.wxAppBaseRules = ratioRuleLoader.load(userNewsConfig.getConfigList("wxapp-base-rules"));
        this.afterRerankRules = ratioRuleLoader.load(userNewsConfig.getConfigList("after-rerank-rules"));
    }

    public List<RatioRule> getRelevantRules(VideoRecommenderContext recommendContext) {
        if (recommendContext.getRecommendRequest().getAppId().equals("t02")) {
            return wxAppBaseRules;
        }
        if (recommendContext.getRecommendRequest().getIhf() == Ihf.VSHOWDANCE_RLVT.id()) {
            return showDanceRlvtRules;
        }
        if (recommendContext.hasBucket("relevant_rule-base")) {
            return relevantRules;
        } else {
            return relevantExpRules;
        }
    }

    public List<RatioRule> getFollowRules(VideoRecommenderContext recommendContext) {
        return followRules;
    }

    public List<RatioRule> getChannelRules(VideoRecommenderContext recommendContext) {
        return channelRules;
    }

    public List<RatioRule> getFeedRules(VideoRecommenderContext recommendContext) {

        if (recommendContext.getRecommendRequest().getAppId().equals("t02")) {
            return wxAppBaseRules;
        }
        if (recommendContext.getRecommendRequest().getIhf() == Ihf.VSMALL_FEED.id()) {
            return this.smallFeedRules;
        }
        if (recommendContext.getRecommendRequest().getIhf() == Ihf.VSHOWDANCE_FEED.id()) {
            if(recommendContext.hasBucket("showDanceTrend_strategy-yes")){
                List<RatioRule> result;
                if(recommendContext.getTrendUserType() == UserProfileUtils.TrendUserType.trend_strong){
                    result = new ArrayList<>(showDanceTrendBaseRules);
                    result.addAll(showDanceTrendStrongRules);
                } else if(recommendContext.getTrendUserType() == UserProfileUtils.TrendUserType.trend_weak){
                    result = new ArrayList<>(showDanceTrendBaseRules);
                    result.addAll(showDanceTrendWeakRules);
                }
                else{
                    result = new ArrayList<>(showDanceTrendBaseRules);
                }
                return result;
            }
            else{
                return this.showDanceRules;
            }
        }
        if (recommendContext.getRecommendRequest().getIhf() == Ihf
                .VFITPACK_FEED.id()) {
            return this.fitPackRules;
        }
        if (recommendContext.hasBucket("group_strategy-yes") &&
                UserProfileUtils.isFitnessUser(recommendContext.getUserItem(), null)) {
            return this.groupFeedRules;
        }
        List<RatioRule> result;
        List<RatioRule> baseRule = recommendContext.hasBucket("base_rule-base") ? feedBaseRules : feedExpRules;

        if (recommendContext.getUserType() == UserProfileUtils.UserType.new_bare) {
            result = new ArrayList<>(newBareUserRules);
            result.addAll(newAllUserRules);
        } else if (recommendContext.getUserType() == UserProfileUtils.UserType.new_interest) {
            result = new ArrayList<>(newInterestUserRules);
            result.addAll(newAllUserRules);
        } else if (recommendContext.getUserType() == UserProfileUtils.UserType.old_bare) {
            result = new ArrayList<>(oldBareUserRules);
            result.addAll(baseRule);
        } else {
            result = new ArrayList<>(oldInterestUserRules);
            result.addAll(baseRule);
        }
        return result;
    }

    public List<RatioRule> getWxAppBaseRules(VideoRecommenderContext recommendContext) {
        return wxAppBaseRules;
    }

    public List<RatioRule> getAfterRerankRules(VideoRecommenderContext recommendContext) {
        List<RatioRule> firstRules = getFeedRules(recommendContext);
        List<Integer> firstRuleIds = firstRules.stream().map(RatioRule::getId).collect(Collectors.toList());
        return afterRerankRules.stream().filter(ratioRule -> firstRuleIds.contains(ratioRule.getId())).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        Set<String> buckets = new HashSet<>();
        buckets.add("");
        VideoRecommenderContext recommendContext = new VideoRecommenderContext("123", Collections.emptySet(), buckets, new Items(), false);
        recommendContext.setPlaceInfo(Optional.empty());
        recommendContext.setRecommendRequest(new RecommendRequest());
        RatioRules.getInstance().getFeedRules(recommendContext);
    }
}
