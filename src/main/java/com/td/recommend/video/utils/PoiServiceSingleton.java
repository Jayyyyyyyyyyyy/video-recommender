package com.td.recommend.video.utils;

import com.typesafe.config.Config;
import com.td.recommend.commons.poi.PoiService;

/**
 * Created by admin on 2017/12/8.
 */
public class PoiServiceSingleton {
    private static volatile PoiServiceSingleton instance = null;

    public static PoiServiceSingleton getInstance() {
        if (instance == null) {
            synchronized (PoiServiceSingleton.class) {
                if (instance == null) {
                    instance = new PoiServiceSingleton();
                }
            }
        }
        return instance;
    }

    private PoiService poiService;

    private PoiServiceSingleton() {
        Config rootConfig = UserVideoConfig.getInstance().getRootConfig();
        Config poiJedisConfig = rootConfig.getConfig("poi-jedis");
        poiService = new PoiService(poiJedisConfig);
    }

    public PoiService getPoiService() {
        return poiService;
    }
}
