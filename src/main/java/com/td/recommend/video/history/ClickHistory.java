package com.td.recommend.video.history;

import com.td.recommend.FilterClickServer;
import com.td.recommend.FilterItem;
import com.td.recommend.FilterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by admin on 2017/6/22.
 */
public class ClickHistory {
    private static final Logger LOG = LoggerFactory.getLogger(ClickHistory.class);

    public static ClickHistory instance = new ClickHistory();
    private final FilterClickServer clickServer;

    public static ClickHistory getInstance() {
        return instance;
    }

    public ClickHistory() {
        clickServer = new FilterClickServer();
    }

    public List<FilterItem> clickSeq(String userId) {
        FilterList loadedList = clickServer.getLoadedItems(userId);
        List<FilterItem> filterItems = Optional.ofNullable(loadedList).map(FilterList::getItems).orElse(new ArrayList<>());

        if (userId.startsWith("debug_")) {
            String realUserId = userId.replaceFirst("debug_", "");
            FilterList realLoadedList = clickServer.getLoadedItems(realUserId);
            List<FilterItem> realFilterItems = Optional.ofNullable(realLoadedList).map(FilterList::getItems).orElse(Collections.emptyList());
            filterItems.addAll(realFilterItems);
        }
        return filterItems;
    }

    public List<String> clicked(String userId) {
        FilterList loadedList = clickServer.getLoadedList(userId);
        List<String> clickList;
        if (loadedList == null) {
            clickList = new ArrayList<>();
        } else {
            clickList = loadedList.getList();
        }
        if (userId.startsWith("debug_")) {
            String realUserId = userId.replaceFirst("debug_", "");
            FilterList realLoadedList = clickServer.getLoadedList(realUserId);
            if (realLoadedList != null) {
                clickList.addAll(realLoadedList.getList());
            }
        }
        return clickList;
    }

    public List<FilterItem> showDanceFeedClickSeq(String userId){
        FilterList loadedList = clickServer.getCommunityFeedLoadedItems(userId);
        List<FilterItem> filterItems = Optional.ofNullable(loadedList).map(FilterList::getItems).orElse(new ArrayList<>());

        if (userId.startsWith("debug_")) {
            String realUserId = userId.replaceFirst("debug_", "");
            FilterList realLoadedList = clickServer.getCommunityFeedLoadedItems(realUserId);
            List<FilterItem> realFilterItems = Optional.ofNullable(realLoadedList).map(FilterList::getItems).orElse(Collections.emptyList());
            filterItems.addAll(realFilterItems);
        }
        return filterItems;
    }

    public List<String> showDanceFeedClicked(String userId){
        FilterList loadedList = clickServer.getCommunityFeedLoadedList(userId);
        List<String> clickList;
        if (loadedList == null) {
            clickList = new ArrayList<>();
        } else {
            clickList = loadedList.getList();
        }
        if (userId.startsWith("debug_")) {
            String realUserId = userId.replaceFirst("debug_", "");
            FilterList realLoadedList = clickServer.getCommunityFeedLoadedList(realUserId);
            if (realLoadedList != null) {
                clickList.addAll(realLoadedList.getList());
            }
        }
        return clickList;
    }

    public List<String> wxappClicked(String userId) {
        FilterList loadedList = clickServer.getWXLoadedList(userId);
        List<String> clickList;
        if (loadedList == null) {
            clickList = new ArrayList<>();
        } else {
            clickList = loadedList.getList();
        }
        return clickList;
    }

    public Set<String> watched(String userId, List<String> items) {
        List<String> watched = clickServer.filter(userId, items);
        if (watched == null) {
            watched = new ArrayList<>();
        }
        if (userId.startsWith("debug_")) {
            String realUserId = userId.replaceFirst("debug_", "");
            List<String> realWatched = clickServer.filter(realUserId, items);
            if (realWatched != null) {
                watched.addAll(realWatched);
            }
        }
        return new HashSet<>(watched);
    }

    public static void main(String[] args) {
        System.out.println("ljk" + ClickHistory.getInstance().clicked("863361021255189"));
        //Set<String> filter = ClickHistory.getInstance().watched("860529030531005", ImmutableList.of("1500653519491", "1500653519491"));
        //System.out.println(filter);

    }

}
