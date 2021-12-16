package com.td.recommend.video.profile;

import com.td.data.profile.TVariance;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.userstore.data.UserRawData;
import com.td.recommend.video.utils.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by admin on 2017/6/17.
 */
public class UserProfileHelper {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileHelper.class);

    public static Map<String, Double> getValueFeaturesMap(UserItem userItem, String facet) {

        Optional<UserRawData> userRawDataOpt = userItem.getUserRawData();
        if (!userRawDataOpt.isPresent()) {
            LOG.error("user raw data is empty for userId={}", userItem.getId());
            return Collections.emptyMap();
        }

        UserRawData userRawData = userRawDataOpt.get();
        Map<String, Map<String, Double>> l2Map = userRawData.getL2Map();
        if (l2Map == null || l2Map.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> featuresMap = l2Map.get(facet);
        if (featuresMap == null) {
            return Collections.emptyMap();
        }

        return featuresMap;
    }

    public static Map<String, TVariance> getVarianceFeatureMap(UserItem userItem, String facet) {
        Optional<UserRawData> userRawDataOpt = userItem.getUserRawData();
        if (!userRawDataOpt.isPresent()) {
//            LOG.error("user raw data is empty for userId={}", userItem.getId());
            return Collections.emptyMap();
        }

        UserRawData userRawData = userRawDataOpt.get();
        Map<String, Map<String, TVariance>> l2VarianceMap = userRawData.getL2VarianceMap();
        if (l2VarianceMap == null || l2VarianceMap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, TVariance> varianceFeaturesMap = l2VarianceMap.get(facet);
        if (varianceFeaturesMap == null) {
            return Collections.emptyMap();
        }

        return varianceFeaturesMap;
    }

    public static Map<String, Map<String, TVariance>> getVarianceMap(UserItem userItem) {
        Optional<UserRawData> userRawDataOpt = userItem.getUserRawData();
        if (!userRawDataOpt.isPresent()) {
            return Collections.emptyMap();
        }

        UserRawData userRawData = userRawDataOpt.get();
        Map<String, Map<String, TVariance>> l2VarianceMap = userRawData.getL2VarianceMap();
        if (l2VarianceMap == null || l2VarianceMap.isEmpty()) {
            return Collections.emptyMap();
        }

        return l2VarianceMap;
    }

    public static Map<String, Map<String, Double>> getValueMap(UserItem userItem) {
        Optional<UserRawData> userRawDataOpt = userItem.getUserRawData();
        if (!userRawDataOpt.isPresent()) {
            return Collections.emptyMap();
        }

        UserRawData userRawData = userRawDataOpt.get();
        Map<String, Map<String, Double>> l2Map = userRawData.getL2Map();
        if (l2Map == null || l2Map.isEmpty()) {
            return Collections.emptyMap();
        }

        return l2Map;
    }

    public static Map<String, String> getSValueFeaturesMap(UserItem userItem, String facet) {

        Optional<UserRawData> userRawDataOpt = userItem.getUserRawData();
        if (!userRawDataOpt.isPresent()) {
            return Collections.emptyMap();
        }

        UserRawData userRawData = userRawDataOpt.get();
        Map<String, Map<String, String>> l2StringMap = userRawData.getL2StringMap();
        if (l2StringMap == null || l2StringMap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> featuresMap = l2StringMap.get(facet);
        if (featuresMap == null) {
            return Collections.emptyMap();
        }

        return featuresMap;
    }

    public static Map<String, String> getTagFeatureMap(UserItem userItem, String facet) {
        Optional<UserRawData> userRawDataOpt = userItem.getUserRawData();
        if (!userRawDataOpt.isPresent()) {
            return Collections.emptyMap();
        }

        UserRawData userRawData = userRawDataOpt.get();
        Map<String, Map<String, String>> tagsMap = userRawData.getTagsMap();
        if (tagsMap == null || tagsMap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> featuresMap = tagsMap.get(facet);
        if (featuresMap == null) {
            return Collections.emptyMap();
        }

        return featuresMap;
    }

    public static UserItem getUserItem(Items queryItems) {
        Optional<IItem> itemOpt = queryItems.get(ItemKey.user);
        UserItem userItem;
        if (itemOpt.isPresent()) {
            userItem = (UserItem) itemOpt.get();
        } else {
            userItem = new UserItem("123");
            LOG.error("Get userItem from queryItems null");
        }

        return userItem;
    }

    private static final Set<String> PROFESSIONAL_CITIES = new HashSet<>();

    static {
        PROFESSIONAL_CITIES.addAll(Arrays.asList(
                "北京", "上海", "北京市", "上海市"
        ));
    }


    public static boolean shouldBobo(UserItem userItem) {
        Map<String, TVariance> featureMap = UserProfileUtils.getVarianceFeatureMap(userItem, "vinner_source_ck");
        TVariance variance = featureMap.get(CommonConstants.BOBO_KEY);
        if (variance == null || variance.getPosCnt() <= 5) {
            return true;
        }

        return false;
    }

}
