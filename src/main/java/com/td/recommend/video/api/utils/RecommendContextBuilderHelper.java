package com.td.recommend.video.api.utils;

import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.bucket.core.BucketGetter;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.poi.PlaceInfo;
import com.td.recommend.commons.profile.ShortTermUserItemBuilder;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.userstore.data.RecentEvents;
import com.td.recommend.userstore.data.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by admin on 2017/12/26.
 */
public class RecommendContextBuilderHelper {
    private static final Logger log = LoggerFactory.getLogger(RecommendContextBuilderHelper.class);
    public static Optional<PlaceInfo> buildPlaceInfo(RecommendRequest recommendRequest) {
//        PoiService poiService = PoiServiceSingleton.getInstance().getPoiService();
//
//        return poiService.getPlaceInfo(recommendRequest.getLat(), recommendRequest.getLng());
        return Optional.empty();
    }


    public static Set<String> buildBuckets(String userId, RecommendRequest recommendRequest) {
        Set<String> buckets;
        if (recommendRequest.getBucket() != null && !recommendRequest.getBucket().isEmpty()) {
            buckets = new HashSet<>();
            buckets.add(recommendRequest.getBucket());
        } else {
            try {
                buckets = BucketGetter.get(userId);
            } catch (Exception e) {
                buckets = Collections.emptySet();
                log.error("build bucket failed, use empty.");
            }
        }

        if (buckets.isEmpty()) {//avoid Collections.emptySet()
            buckets = new HashSet<>();
        }

//        String alg = recommendRequest.getAlg();
//        if (RecommendRequest.GBDT.equals(alg)) {
//            buckets.add(BucketConstants.MODEL_GBDT);
//        } else {
//            buckets.add(BucketConstants.MODEL_FTRL);
//        }
        return buckets;
    }

    /**
     * TODO add read user view history
     * @param deviceId
     * @return use viewed doc ids
     */

    public static Optional<RecentEvents> buildRecentEvents(String deviceId) {
//        RecentEventDao recentEventDao = RecentEventDao.getInstance();
//        Optional<RecentEvents> recentEventOpt = recentEventDao.getRecentEvent(deviceId);
//        return recentEventOpt;
        return Optional.empty();
    }

    public static void buildRecentItem(Items queryItems, Optional<RecentEvents> recentEvents) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        if (recentEvents.isPresent()) {
            Timer.Context shortTermUserItemTime = taggedMetricRegistry.timer("uservideo.shortterm.latency").time();

            Optional<UserItem> userItemOpt = ShortTermUserItemBuilder.getInstance().build(recentEvents.get(), queryItems);
            if (userItemOpt.isPresent()) {
                queryItems.add(ItemKey.user_short_term, userItemOpt.get());
            }

            shortTermUserItemTime.stop();
        }
    }


    public static void main(String[] argv) {

        Set<String> buckets = BucketGetter.get("2292b4f6661e09fb");
        buckets.stream().forEach(
                bucket-> {
                    System.out.println(bucket);
                }
        );
    }
}
