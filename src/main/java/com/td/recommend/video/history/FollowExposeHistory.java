package com.td.recommend.video.history;

import com.td.recommend.FollowFilterServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/6/22.
 */
public class FollowExposeHistory {
    private static final Logger LOG = LoggerFactory.getLogger(FollowExposeHistory.class);

    public static FollowExposeHistory instance = new FollowExposeHistory();

    public static FollowExposeHistory getInstance() {
        return instance;
    }

    private FollowFilterServer filterServer;

    public FollowExposeHistory() {
        filterServer = new FollowFilterServer();
    }

    public List<String> getList(String appId, String userId) {
        List<String> loadedList = filterServer.getFilterListOrderNew(appId, userId);
        if (loadedList == null) {
            return Collections.emptyList();
        }
        return loadedList;
    }

    public static void main(String[] args) {
        System.out.println("ljk" + FollowExposeHistory.getInstance().getList("t01", "12325728"));
    }
}
