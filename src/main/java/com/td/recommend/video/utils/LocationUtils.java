package com.td.recommend.video.utils;

import com.td.recommend.commons.poi.PlaceInfo;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by admin on 2017/9/4.
 */
public class LocationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(LocationUtils.class);

    private static final Set<String> FILTER_CITIES = new HashSet<>(Arrays.asList(
        "北京", "上海")
    );

//    private static final Set<String> FILTER_PROVINCES = Collections.emptySet();

//            new HashSet<>(Arrays.asList(
//       "江苏"
//    ));
    private static Map<String, String> provinceMap = new HashMap<>();
    private static Set<String> provinces = new HashSet<>();

    static {
        loadProvinceMap();
        provinces = new HashSet<>(provinceMap.values());
    }

    public static void loadProvinceMap() {
        String dictFileName = "provinces.txt";
        ClassLoader classLoader = LocationUtils.class.getClassLoader();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(dictFileName), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                if (fields.length >= 2) {
                    String province = fields[0].trim();
                    String provinceAbbr = fields[1].trim();
                    provinceMap.put(province, provinceAbbr);
                }
            }
        } catch (IOException e) {
            LOG.error("Read province info from file={} failed!", dictFileName, e);
        }
    }


    public static Set<String> getProvinces() {
        return provinces;
    }

    public static Optional<PlaceInfo> getPlaceInfo(VideoRecommenderContext recommendContext, double lat, double lng) {
//        double lat, double lng
        Optional<PlaceInfo> placeInfoOpt = recommendContext.getPlaceInfo();
        if (placeInfoOpt.isPresent()) {
            return placeInfoOpt;
        }


        UserItem userItem = recommendContext.getUserItem();
        return getResidencePlace(userItem);
    }


    public static Optional<PlaceInfo> getResidencePlace(UserItem userItem) {
        Map<String, String> appLbs = UserProfileUtils.getSValueFeaturesMap(userItem, "app_lbs");
        String city = appLbs.get("city");
        String province = appLbs.get("province");


        if (city != null && province != null) {
            PlaceInfo placeInfo = new PlaceInfo();
            placeInfo.setCity(city);
            String provinceAbbr = provinceMap.get(province);
            if (provinceAbbr == null) {
                placeInfo.setProvince(province);
            } else {
                placeInfo.setProvince(provinceAbbr);
            }

            return Optional.of(placeInfo);
        }

        return Optional.empty();
    }

}
