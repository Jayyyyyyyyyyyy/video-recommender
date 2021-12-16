package com.td.recommend.video.utils;

import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import com.typesafe.config.Config;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


/**
 * Created by admin on 2017/12/8.
 */
public class VectorFeatureHandler {
    private static final Logger LOG = LoggerFactory.getLogger(VectorFeatureHandler.class);
    private static String u2uUrl;
    private static final int userNum = 20;
    static {
        Config bprServer = UserVideoConfig.getInstance().getRootConfig().getConfig("vector-server");
        u2uUrl = bprServer.getString("bpr.u2u.url");
    }
    public VectorFeatureHandler() {
    }
    public Optional<DynamicDumpInfo.SimUserDoc> getBprSimUer(String diu) {

        if (StringUtils.isBlank(diu)) {
            LOG.error("VectorFeatureHandler -> getBpr-u2u diu is null");
            return Optional.empty();
        } else {
            String requestUrl = u2uUrl + "key=" + diu + "&num=" + userNum;
            try {
                DynamicDumpInfo.SimUserDoc simUserDoc = HttpClientSingleton.getInstance().request(requestUrl, DynamicDumpInfo.SimUserDoc.class);
                if (simUserDoc != null) {
                    return Optional.of(simUserDoc);
                }
            } catch (Exception var2) {
                LOG.error("VectorFeatureHandler -> getBpr-u2u error diu {} , exception ", diu, var2);
            }
            return Optional.empty();
        }
    }
}
