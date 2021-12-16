package com.td.recommend.video.history;

import com.td.recommend.FilterList;
import com.td.recommend.RelevantFilterServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by admin on 2017/6/22.
 */
public class RelevantExposeHistory {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantExposeHistory.class);

    public static RelevantExposeHistory instance = new RelevantExposeHistory();

    public static RelevantExposeHistory getInstance() {
        return instance;
    }

    private RelevantFilterServer filterServer;

    public RelevantExposeHistory() {
        filterServer = new RelevantFilterServer();
    }

    public Set<String> get(String appId, String userId) {
        FilterList loadedList = filterServer.getLoadedList(appId, userId);
        if (loadedList == null) {
            return Collections.emptySet();
        }
        List<String> list = loadedList.getList();
        if(list==null){
            return Collections.emptySet();
        }
        return new HashSet<>(list);
    }
    public List<String> getList(String appId, String userId) {
        FilterList loadedList = filterServer.getLoadedList(appId, userId);
        if (loadedList == null) {
            return Collections.emptyList();
        }
        List<String> list = loadedList.getList();
        if(list==null){
            return Collections.emptyList();
        }
        return list;
    }
    public static void main(String[] args) {
        System.out.println("ljk"+ RelevantExposeHistory.getInstance().get("t01", "86756403012549"));
    }
}
