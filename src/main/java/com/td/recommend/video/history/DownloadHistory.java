package com.td.recommend.video.history;

import com.td.recommend.FilterDownloadServer;
import com.td.recommend.FilterItem;
import com.td.recommend.FilterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DownloadHistory {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadHistory.class);

    public static DownloadHistory instance = new DownloadHistory();
    private final FilterDownloadServer downloadServer;

    public static DownloadHistory getInstance() {
        return instance;
    }

    public DownloadHistory() {
        downloadServer = new FilterDownloadServer();
    }

    public List<FilterItem> downloadSeq(String diu) {
        FilterList loadedList = downloadServer.getLoadedItems(diu);
        List<FilterItem> filterItems = Optional.ofNullable(loadedList).map(FilterList::getItems).orElse(new ArrayList<>());

        if (diu.startsWith("debug_")) {
            String realUserId = diu.replaceFirst("debug_", "");
            FilterList realLoadedList = downloadServer.getLoadedList(realUserId);
            List<FilterItem> realFilterItems = Optional.ofNullable(realLoadedList).map(FilterList::getItems).orElse(Collections.emptyList());
            filterItems.addAll(realFilterItems);
        }
        return filterItems;
    }
    public List<String> downloaded(String diu) {
        FilterList loadedList = downloadServer.getLoadedList(diu);
        List<String> downloadList;
        if (loadedList == null) {
            downloadList = new ArrayList<>();
        } else {
            downloadList = loadedList.getList();
        }
        if (diu.startsWith("debug_")) {
            String realDiu = diu.replaceFirst("debug_", "");
            FilterList realLoadedList = downloadServer.getLoadedList(realDiu);
            if (realLoadedList != null) {
                downloadList.addAll(realLoadedList.getList());
            }
        }
        return downloadList;
    }

    public List<String> downloaded(String diu, int num) {
        List<String> downloadList = downloaded(diu);
        if (downloadList == null || downloadList.isEmpty() || downloadList.size() <= num) {
            return downloadList;
        }
        return downloadList.subList(0, num - 1);
    }
}
