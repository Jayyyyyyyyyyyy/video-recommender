package com.td.recommend.video.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Created by admin on 2017/6/10.
 */
public class UserVideoConfig {
    private static UserVideoConfig instance = new UserVideoConfig();

    private Config rootConfig;
    private Config userNewsConfig;

    public static UserVideoConfig getInstance() {
        return instance;
    }

    private UserVideoConfig() {
        rootConfig = ConfigFactory.load();
        userNewsConfig = rootConfig.getConfig("user-video");
    }

    public Config getAppConfig() {
        return userNewsConfig;
    }

    public Config getRootConfig() {
        return rootConfig;
    }
}
