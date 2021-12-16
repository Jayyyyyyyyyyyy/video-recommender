package com.td.recommend.video.utils;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ChannelUtils {
    private static Map<String, List<String>> channel2CatNames;
    private static final Logger LOG = LoggerFactory.getLogger(ChannelUtils.class);

    static {
        load();
    }


    private static void load() {
        channel2CatNames = new HashMap<>();
        Config rootConfig = UserVideoConfig.getInstance().getRootConfig();


        if (rootConfig.hasPath("user-video-subcat")) {
            List<? extends Config> configs = rootConfig.getConfigList("user-video-subcat");
            for (Config config : configs) {
                String channelId = config.getString("id");
                List<String> catNames = config.getStringList("cat-name");
                channel2CatNames.put(channelId, catNames);
            }
        }
    }

    public static List<String> getCatNames(String channelId) {
        List<String> catNames = channel2CatNames.get(channelId);
        if (catNames == null) {
            LOG.warn("Get empty catName for channelId={}", channelId);
            return Collections.emptyList();
        }

        return catNames;
    }

    public static boolean isVideoSubChannel(String channelId) {
        return channel2CatNames.containsKey(channelId);
    }
}
