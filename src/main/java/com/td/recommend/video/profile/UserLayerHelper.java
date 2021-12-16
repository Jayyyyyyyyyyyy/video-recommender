package com.td.recommend.video.profile;

import com.td.recommend.commons.poi.PlaceInfo;
import com.td.recommend.commons.poi.PoiService;
import com.td.recommend.video.abtest.BucketConstants;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.LocationUtils;
import com.td.recommend.video.utils.PoiServiceSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by admin on 2017/10/31.
 */
public class UserLayerHelper {
    private static final Logger LOG = LoggerFactory.getLogger(UserLayerHelper.class);

    private static final Set<String> HIGH_END_CITIES = new HashSet<>();

    private static final Set<String> LAYER1_CITIES = new HashSet<>();

    static {
        HIGH_END_CITIES.addAll(Arrays.asList(
                "北京", "上海", "北京市", "上海市"/*, "广州", "广州市", "深圳", "深圳市"*/
//            "上海", "上海市"
        ));

        LAYER1_CITIES.addAll(Arrays.asList(
                "北京", "上海", "广州", "深圳", "天津", "北京市", "上海市", "广州市", "深圳市", "天津市"
        ));
    }


    public static boolean isLocationLayer1(VideoRecommenderContext recommendContext) {
        return isPlaceMatch(recommendContext, LAYER1_CITIES);
    }

    public static boolean isLocationHighEnd0(VideoRecommenderContext recommendContext) {
        if (recommendContext.hasBucket(BucketConstants.HIGHEND_EXCLUDE_EXP)) {
            return false;
        }

        return isPlaceMatch(recommendContext, HIGH_END_CITIES);
    }

    private static boolean isPlaceMatch(VideoRecommenderContext recommendContext, Set<String> cities) {
//        RecommendRequest recommendRequest = recommendContext.getRecommendRequest();
//        if (recommendRequest.isHighEnd()) {
//            return true;
//        }
//
//        //Optional<PoiService.PlaceInfo> placeInfoOpt = LocationUtils.getPlaceInfo(recommendContext, recommendRequest.getLat(), recommendRequest.getLng());
//        Optional<PlaceInfo> realTimePlaceInfoOpt = recommendContext.getPlaceInfo();
//        Optional<PlaceInfo> residencePlaceOpt = LocationUtils.getResidencePlace(recommendContext.getUserItem());
//
//        if (!realTimePlaceInfoOpt.isPresent() && !residencePlaceOpt.isPresent()) {
//            LOG.info("Unknown place for userId={}", recommendContext.getSubjectId());
//            return true;
//        }
//
//        boolean match = false;
//        if (realTimePlaceInfoOpt.isPresent()) {
//            match = isPlaceMath(realTimePlaceInfoOpt.get(), cities);
//        }
//
//        if (!match && residencePlaceOpt.isPresent()) {
//            match = isPlaceMath(residencePlaceOpt.get(), cities);
//        }
//
//        return match;
        return true;
    }

    public static boolean isPlaceMath(PlaceInfo placeInfo, Set<String> cities) {
        if (isAbroad(placeInfo.getProvince())) {
            return true;
        }

        if (cities.contains(placeInfo.getCity())) {
            return true;
        }

        return false;
    }

    public static boolean isAbroad(String province) {
        Set<String> provinces = LocationUtils.getProvinces();

        if (provinces.isEmpty()) {
            return false;
        }

        return !provinces.contains(province);
    }

    public static boolean isHighEndNight() {
        LocalDateTime localDateTime = LocalDateTime.now();
        int hour = localDateTime.getHour();
        if (hour > 22 || hour < 6) {
            return true;
        }
        return false;
    }

    public static boolean isLowEndNight() {
        LocalDateTime localDateTime = LocalDateTime.now();
        int hour = localDateTime.getHour();

        if (hour > 19 || hour < 6) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) {
        PoiService poiService = PoiServiceSingleton.getInstance().getPoiService();
        Optional<PlaceInfo> placeInfoOpt = poiService.getPlaceInfo(31.225753, 121.38871);

        if (placeInfoOpt.isPresent()) {
            PlaceInfo placeInfo = placeInfoOpt.get();
            System.out.println(placeInfo.getCity());
        }
    }
}
