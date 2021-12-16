package com.td.recommend.video.utils;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.profile.UserProfileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UserUvMonitor {

    private final static String userUvLock = "user_uv_lock";

    private final static int period = 5;

    private static final TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance()
            .getTaggedMetricRegistry();
    private static final Logger LOG = LoggerFactory.getLogger(UserUvMonitor.class);

    public static void init() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton jedis = RedisClientSingleton.general;
                if (jedis.set(userUvLock, "lock", "nx", "ex", period * 60 - 5) != null) {
                    for (UserProfileUtils.UserType userType : UserProfileUtils.UserType.values()) {
                        taggedMetricRegistry.histogram("feed.uv." + userType.name()).update(jedis.pfCountAndDel(userType.name()));
                    }
                    taggedMetricRegistry.histogram("feed.uv.others").update(jedis.pfCountAndDel("others"));

                    for (CommonConstants.DynamicRuleUser dynamicRuleUser : CommonConstants.DynamicRuleUser.values()) {
                        taggedMetricRegistry.histogram("feed.uv." + dynamicRuleUser.name()).update(jedis.pfCountAndDel(dynamicRuleUser.name()));
                    }
                } else {
                    for (UserProfileUtils.UserType userType : UserProfileUtils.UserType.values()) {
                        taggedMetricRegistry.histogram("feed.uv." + userType.name()).update(0);
                    }
                    taggedMetricRegistry.histogram("feed.uv.others").update(0);
                    for (CommonConstants.DynamicRuleUser dynamicRuleUser : CommonConstants.DynamicRuleUser.values()) {
                        taggedMetricRegistry.histogram("feed.uv." + dynamicRuleUser.name()).update(0);
                    }
                }
            } catch (Exception e) {
                LOG.info("uv monitor failed:", e);
            }
        }, 0, period, TimeUnit.MINUTES);
    }
}
