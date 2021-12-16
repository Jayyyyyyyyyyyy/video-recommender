package com.td.recommend.video.retriever.keybuilder.trend;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Created by zhangx on 2021/08/23.
 * Modified by sunjian on 2021/09/09
 */
public class TrendRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public TrendRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String uid = recommendContext.getRecommendRequest().getUid();
        UserItem user = UserVideoItemDataSource.getInstance().getUserItemDao().getUserFollow(uid).get();
        Map<String, Double> featureMap = UserProfileUtils.getValueFeaturesMap(user, "tfollow");
        ArrayList<String> list = new ArrayList<>(featureMap.keySet());
        Collections.shuffle(list);
        int ihf = recommendContext.getRecommendRequest().getIhf();
        //只限app首页和社区首页的召回
        if(ihf==Ihf.VMIX_FEED.id() || ihf==Ihf.VSHOWDANCE_FEED.id()){
            for (String followedUid : list.subList(0, Math.min(10, list.size()))) {
                RetrieveKey retrieveKey = new RetrieveKey();
                retrieveKey.setIhf(String.valueOf(recommendContext.getRecommendRequest().getIhf()));
                retrieveKey.setType(RetrieverType.tfollow.name());
                retrieveKey.setAlias(RetrieverType.tfollow.alias());
                retrieveKey.setKey(followedUid);
                //只在社区首页生效
                if(ihf== Ihf.VSHOWDANCE_FEED.id()){
                    retrieveKey.setReason("你的关注");
                }
                if (ihf == Ihf.VSHOWDANCE_FEED.id()){
                    retrieveKeyContext.addRetrieveKey(retrieveKey);
                }

                RetrieveKey retrieveKeyEv = new RetrieveKey();
                retrieveKeyEv.setIhf(String.valueOf(recommendContext.getRecommendRequest().getIhf()));
                retrieveKeyEv.setType(RetrieverType.tfollow_ev.name());
                retrieveKeyEv.setAlias(RetrieverType.tfollow_ev.alias());
                retrieveKeyEv.setKey(followedUid);
                //只在社区首页生效
                if (ihf == Ihf.VSHOWDANCE_FEED.id()) {
                    retrieveKeyEv.setReason("你的关注");
                }
                //只限app首页和社区首页的召回
                retrieveKeyContext.addRetrieveKey(retrieveKeyEv);
            }
        }
        // 只限ihf=11时 召回
        if(ihf==Ihf.VSHOWDANCE_FEED.id()){
            //基于全网的热门内容召回  召回最近7天 按喜欢数倒排的动态
            RetrieveKey retrieveKeyHot7Day = new RetrieveKey();
            retrieveKeyHot7Day.setIhf(String.valueOf(recommendContext.getRecommendRequest().getIhf()));
            retrieveKeyHot7Day.setType(RetrieverType.t_hot_7day.name());
            retrieveKeyHot7Day.setAlias(RetrieverType.t_hot_7day.alias());
            retrieveKeyHot7Day.setKey("");
            retrieveKeyContext.addRetrieveKey(retrieveKeyHot7Day);
            //全网新动态召回（new_ev） 召回最近24小时新增动态，按发布时间倒排
            RetrieveKey retrieveKeyHot24Hour = new RetrieveKey();
            retrieveKeyHot24Hour.setIhf(String.valueOf(recommendContext.getRecommendRequest().getIhf()));
            retrieveKeyHot24Hour.setType(RetrieverType.t_hot_24hour.name());
            retrieveKeyHot24Hour.setAlias(RetrieverType.t_hot_24hour.alias());
            retrieveKeyHot24Hour.setKey("");
            retrieveKeyContext.addRetrieveKey(retrieveKeyHot24Hour);
            //trend_ev 召回
            RetrieveKey retrieveKeyEV = new RetrieveKey();
            retrieveKeyEV.setIhf(String.valueOf(recommendContext.getRecommendRequest().getIhf()));
            retrieveKeyEV.setType(RetrieverType.trend_ev.name());
            retrieveKeyEV.setAlias(RetrieverType.trend_ev.alias());
            retrieveKeyEV.setKey("");
            retrieveKeyContext.addRetrieveKey(retrieveKeyEV);
            //trandom
            RetrieveKey retrieveKeyTRandom = new RetrieveKey();
            retrieveKeyTRandom.setIhf(String.valueOf(recommendContext.getRecommendRequest().getIhf()));
            retrieveKeyTRandom.setType(RetrieverType.trandom.name());
            retrieveKeyTRandom.setAlias(RetrieverType.trandom.alias());
            retrieveKeyTRandom.setKey("");
            retrieveKeyContext.addRetrieveKey(retrieveKeyTRandom);
        }

    }

    public static void main(String[] args) {
        UserItem userItem = new UserItemDao().get("11688183").get();
        Map<String, String> featureMap = UserProfileUtils.getSValueFeaturesMap(userItem, "tfollow");
        for (Map.Entry<String, String> entry : featureMap.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

}
