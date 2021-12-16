package com.td.recommend.video.history;

import com.td.recommend.FilterDurationServer;
import com.td.recommend.FilterItem;
import com.td.recommend.FilterList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PlayHistory {
    private static final Logger LOG = LoggerFactory.getLogger(PlayHistory.class);

    public static PlayHistory instance = new PlayHistory();
    private final FilterDurationServer filterDurationServer;


    public static PlayHistory getInstance() {
        return instance;
    }

    public PlayHistory() {
        filterDurationServer = new FilterDurationServer();
    }

    public List<ImmutablePair<String, Double>> playDuration(String userId) {
        List<ImmutablePair<String, Double>> loadedList = filterDurationServer.getDurationPairs(userId);
        if(loadedList == null){
            return Collections.emptyList();
        }
        return loadedList;
    }
    public List<FilterItem> playSeq(String diu) {
        FilterList loadedList = filterDurationServer.getLoadedItems(diu);
        List<FilterItem> filterItems = Optional.ofNullable(loadedList).map(FilterList::getItems).orElse(new ArrayList<>());

        if (diu.startsWith("debug_")) {
            String realUserId = diu.replaceFirst("debug_", "");
            FilterList realLoadedList = filterDurationServer.getLoadedList(realUserId);
            List<FilterItem> realFilterItems = Optional.ofNullable(realLoadedList).map(FilterList::getItems).orElse(Collections.emptyList());
            filterItems.addAll(realFilterItems);
        }
        return filterItems;
    }
}
