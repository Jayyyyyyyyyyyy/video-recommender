package com.td.recommend.video.debug;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Getter
@Setter
public class RerankDebugInfo {
    private List<RerankDebugItem> rerankBeforeItems;
    private List<RerankDebugItem> rerankAfterItems;
    private Map<String,Integer> retrieveCount;
    private Map<String, Integer> subcatCount;
    private Map<String,Map<String,Integer>> subcatRetrieveCount;

    private static final RerankDebugInfo EMPTY = new RerankDebugInfo();
    static {
        EMPTY.setRerankBeforeItems(Collections.emptyList());
        EMPTY.setRerankAfterItems(Collections.emptyList());
    }

    public static RerankDebugInfo empty() {
        return EMPTY;
    }
}
