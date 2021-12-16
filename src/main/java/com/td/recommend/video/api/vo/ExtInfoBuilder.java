package com.td.recommend.video.api.vo;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/11/13.
 */
public class ExtInfoBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ExtInfoBuilder.class);

    public static Map<String, Object> build(VideoRecommenderContext recommendContext, long latency) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();

        Map<String, Object> extMap = new HashMap<>();
        extMap.put("token", recommendContext.getToken());
        extMap.put("latency", latency);
        String hostName;
        try {
            NetworkInterface eth0 = NetworkInterface.getByName("eth0");
            hostName = eth0.getInetAddresses().nextElement().getHostAddress();
        } catch (Exception e) {
            hostName = "127.0.0.1";
        }
        extMap.put("host", hostName);
        extMap.put("model",recommendContext.getModel());
        extMap.put("rerankModel",recommendContext.getRerankModel());
        if(recommendContext.getRecommendRequest().getIhf()== Ihf.VMIX_FEED.id()
                && recommendContext.getRecommendRequest().getFp()==1){
            extMap.put("trendUid", recommendContext.getTrendUid());
            extMap.put("trendVidString", recommendContext.getTrendVidStrings());
            extMap.put("trendRecall", recommendContext.getTrendRecall());
            extMap.put("trendUidInfo", recommendContext.getTrendUidInfo());
        }
        //extMap.put("talentcluster", recommendContext.getTalentclusterline());
        RecommendRequest recommendRequest = recommendContext.getRecommendRequest();
//        UserItem userItem = recommendContext.getUserItem();
//
//        Histogram lessActiveUserHistogram = taggedMetricRegistry.histogram("usernews.user.lessactive.ratio");
//        if (UserProfileHelper.isLessActiveUser(userItem)) {
//            lessActiveUserHistogram.update(100);
//            extMap.put("lessactive", 1);
//        } else {
//            lessActiveUserHistogram.update(0);
//        }
//
//        Histogram dayUserHistogram = taggedMetricRegistry.histogram("usernews.user.3days.ratio");
//        if (UserProfileHelper.isNewUserInDays(userItem, 3)) {
//            LOG.info("userId={} 3 days news user", userItem.getId());
//            dayUserHistogram.update(100);
//        } else {
//            dayUserHistogram.update(0);
//        }
//
//        Histogram highEndHistogram = taggedMetricRegistry.histogram("usernews.user.highend.ratio");
//
//        if (UserLayerHelper.isLocationHighEnd0(recommendContext)) {
//            highEndHistogram.update(100);
//        } else {
//            highEndHistogram.update(0);
//        }
//
//        int categoryCount = UserProfileUtils.getCategoryCount(userItem);
//        taggedMetricRegistry.histogram("usernews.user.category.count").update(categoryCount);
//
//        Optional<PlaceInfo> placeInfoOpt = recommendContext.getPlaceInfo();
//        if (placeInfoOpt.isPresent()) {
//            PlaceInfo placeInfo = placeInfoOpt.get();
//            extMap.put("city", placeInfo.getCity());
//        } else {
//            extMap.put("city", "unknown");
//        }

        String netModel = recommendRequest.getNetType();
        extMap.put("net", netModel);

        extMap.put("ts", System.currentTimeMillis());
        return extMap;
    }

    public static void main(String[] args) {
        String hostName="asdf";
        try {
            hostName = NetworkInterface.getByName("en0").getInetAddresses().nextElement().getHostName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(hostName);
    }

}
