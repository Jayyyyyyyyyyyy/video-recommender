package com.td.recommend.video.retriever.keybuilder.utils;

import com.typesafe.config.Config;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.utils.UserVideoConfig;

import java.util.*;

/**
 * Created by admin on 2017/7/28.
 */
public class TopicRetrieveKeyBlackList {
    private Map<RetrieverType, Set<String>> topicTypeBlacklistMap = new EnumMap<>(RetrieverType.class);

    public static TopicRetrieveKeyBlackList INSTANCE = new TopicRetrieveKeyBlackList();

    public TopicRetrieveKeyBlackList() {
        List<? extends Config> topicTypeBlacklistConfig = UserVideoConfig.getInstance().
                getAppConfig().getConfigList("topic_blacklist");
        for (Config topicTypeConfig : topicTypeBlacklistConfig) {
            String type = topicTypeConfig.getString("type");
            List<String> blacklist = topicTypeConfig.getStringList("blacklist");
            topicTypeBlacklistMap.put(RetrieverType.valueOf(type), new HashSet<>(blacklist));
        }
    }

    public boolean contains(RetrieverType retrieveType, String topic) {
        Set<String> blacklist = topicTypeBlacklistMap.get(retrieveType);

        return blacklist != null && blacklist.contains(topic);
    }

    public static void main(String[] args) {
        for (Map.Entry<RetrieverType, Set<String>> entry : TopicRetrieveKeyBlackList.INSTANCE.topicTypeBlacklistMap.entrySet()) {
            System.out.println(entry.getKey() + "\t" + String.join(",", entry.getValue()));
        }
    }
}
