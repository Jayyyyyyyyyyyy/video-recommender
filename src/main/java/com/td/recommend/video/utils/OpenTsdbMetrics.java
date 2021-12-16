package com.td.recommend.video.utils;

import com.github.sps.metrics.OpenTsdbReporter;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.github.sps.metrics.opentsdb.OpenTsdb;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 2017/8/18.
 */
public class OpenTsdbMetrics {
    private static final Logger LOG = LoggerFactory.getLogger(OpenTsdbMetrics.class);
    private static volatile boolean isInit = false;

    public static void initMetrics() {
        try {
            if (!isInit) {
                synchronized (OpenTsdbMetrics.class) {
                    if (!isInit) {
                        isInit = true;
                    } else {
                        return;
                    }
                }
            } else {
                return;
            }

            Map<String, String> tags = new HashMap<>();
            tags.put("component", "video-recommender");
            String hostName;
            try {
                NetworkInterface eth0 = NetworkInterface.getByName("eth0");
                hostName = eth0.getInetAddresses().nextElement().getHostAddress();
            } catch (Exception e) {
                hostName = "127.0.0.1";
            }
            tags.put("host", hostName);

            Config opentsdb = ConfigFactory.load().getConfig("opentsdb");
            int connectTimeout = opentsdb.getInt("connect-timeout");
            int readTimeout = opentsdb.getInt("read-timeout");
            String openTsdbAddress = opentsdb.getString("address");
            OpenTsdb openTsdb = OpenTsdb.forService(openTsdbAddress)
                    .withGzipEnabled(true)
                    .withConnectTimeout(connectTimeout)
                    .withReadTimeout(readTimeout)
                    .create();
            TaggedMetricRegistry metricRegistry = new TaggedMetricRegistry();
            OpenTsdbReporter.forRegistry(metricRegistry)
                    .withTags(tags)
                    .withBatchSize(20)
                    .build(openTsdb)
                    .start(60L, TimeUnit.SECONDS);
            TaggedMetricRegisterSingleton.getInstance().init(metricRegistry);
        } catch (Exception e) {
            LOG.error("init metrics failed!", e);
        }
    }
}
