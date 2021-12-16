package com.td.recommend.video.retriever.keybuilder.follow;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import com.td.recommend.video.utils.RedisClientSingleton;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/12/2.
 */
public class FollowRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private final VideoRecommenderContext recommendContext;
    private final Set<String> followFreshVideoUids;
    private final Map<String, Double> unfollows, followExtend;

    public FollowRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
        followFreshVideoUids = RedisClientSingleton.general.smembers("follow_fresh_video_uids");//24小时内有发布新视频的uid
        unfollows = UserProfileUtils.getValueFeaturesMap(recommendContext.getUserItem(), "unfollow_st");//取消关注
        followExtend = UserProfileUtils.getValueFeaturesMap(recommendContext.getUserItem(), "vfollow_ext");//用户经常观看的uid
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vfollow, "vfollow");//关注了uid
        addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vfollow_st, "vfollow_st");//关注了uid实时
    }

    private void addInterestRetrieveKeys(RetrieveKeyContext retrieveKeyContext,
                                         RetrieverType retrieverType,
                                         String facets) {
        Map<String, Double> follows = UserProfileUtils.getValueFeaturesMap(recommendContext.getUserItem(), facets);
        List<String> strongFollows = follows.entrySet().stream()
                .filter(i -> i.getValue() > unfollows.getOrDefault(i.getKey(), 0d)) //根据时间戳干掉取关的
                .filter(i -> followExtend.containsKey(i.getKey()))
                .filter(i -> followFreshVideoUids.contains(i.getKey()))
                .sorted(Comparator.comparing(i -> -followExtend.get(i.getKey())))//亲密度排序
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> weakFollows = follows.entrySet().stream()
                .filter(i -> i.getValue() > unfollows.getOrDefault(i.getKey(), 0d)) //根据时间戳干掉取关的
                .filter(i -> !followExtend.containsKey(i.getKey()))
                .filter(i -> followFreshVideoUids.contains(i.getKey()))
                .sorted(Comparator.comparing(Map.Entry<String, Double>::getValue).reversed())//关注时间
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        strongFollows.subList(0, Math.min(100, strongFollows.size())).forEach(k -> {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setKey(k);
            retrieveKey.setType(RetrieverType.vfollow_ext.name());
            retrieveKey.setAlias(RetrieverType.vfollow_ext.alias());
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        });

        Collections.shuffle(weakFollows);
        weakFollows.subList(0, Math.min(100, weakFollows.size())).forEach(k -> {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setKey(k);
            retrieveKey.setType(retrieverType.name());
            retrieveKey.setAlias(retrieverType.alias());
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        });
    }

    public static void main(String[] args) {
        String uid = "12173868";
        Optional<UserItem> userItem = new UserItemDao().getUserFollow(uid);
        Map<String, Double> follows = UserProfileUtils.getValueFeaturesMap(userItem.get(), "vfollow");
        Set<String> followFreshVideoUids = RedisClientSingleton.general.smembers("follow_fresh_video_uids");//24小时内有发布新视频的uid
        Map<String, Double> followExtend = UserProfileUtils.getValueFeaturesMap(userItem.get(), "vfollow_ext");//用户经常观看的uid
        Map<String, Double> unfollows = UserProfileUtils.getValueFeaturesMap(userItem.get(), "unfollow_st");//取消关注

        List<String> strongFollows = follows.entrySet().stream()
                .filter(i -> i.getValue() > unfollows.getOrDefault(i.getKey(), 0d)) //根据时间戳干掉取关的
                .filter(i -> followExtend.containsKey(i.getKey()))
                .filter(i -> followFreshVideoUids.contains(i.getKey()))
                .sorted(Comparator.comparing(i -> -followExtend.get(i.getKey())))//亲密度排序
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        System.out.println(strongFollows);
    }

}