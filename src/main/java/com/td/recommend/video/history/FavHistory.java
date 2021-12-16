package com.td.recommend.video.history;

import com.td.recommend.FilterFavServer;
import com.td.recommend.FilterItem;
import com.td.recommend.FilterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FavHistory {
    private static final Logger LOG = LoggerFactory.getLogger(FavHistory.class);

    public static FavHistory instance = new FavHistory();
    private final FilterFavServer favServer;

    public static FavHistory getInstance() {
        return instance;
    }

    public FavHistory() {
        favServer = new FilterFavServer();
    }
    public List<String> faved(String diu) {
        FilterList loadedList = favServer.getLoadedList(diu);
        List<String> favList;
        if (loadedList == null) {
            favList = new ArrayList<>();
        } else {
            favList = loadedList.getList();
        }
        if (diu.startsWith("debug_")) {
            String realDiu = diu.replaceFirst("debug_", "");
            FilterList realLoadedList = favServer.getLoadedList(realDiu);
            if (realLoadedList != null) {
                favList.addAll(realLoadedList.getList());
            }
        }
        return favList;
    }

    public List<FilterItem> favSeq(String diu) {
        FilterList loadedList = favServer.getLoadedItems(diu);
        List<FilterItem> filterItems = Optional.ofNullable(loadedList).map(FilterList::getItems).orElse(new ArrayList<>());

        if (diu.startsWith("debug_")) {
            String realUserId = diu.replaceFirst("debug_", "");
            FilterList realLoadedList = favServer.getLoadedList(realUserId);
            List<FilterItem> realFilterItems = Optional.ofNullable(realLoadedList).map(FilterList::getItems).orElse(Collections.emptyList());
            filterItems.addAll(realFilterItems);
        }
        return filterItems;
    }

    public List<String> faved(String diu, int num) {
        List<String> favList = faved(diu);
        if (favList == null || favList.isEmpty() || favList.size() <= num) {
            return favList;
        }
        return favList.subList(0, num);
    }
}
