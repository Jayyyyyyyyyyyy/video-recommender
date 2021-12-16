package com.td.recommend.video.utils;

import org.apache.commons.lang3.StringUtils;

public class AppUtils {
    public static boolean isAppStorePreview(String appStore) {
        return StringUtils.equalsIgnoreCase(appStore, "preview");
    }
}
