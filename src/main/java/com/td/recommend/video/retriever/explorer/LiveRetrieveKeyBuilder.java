package com.td.recommend.video.retriever.explorer;

import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.HashMap;
import java.util.Map;

public class LiveRetrieveKeyBuilder {
    private static final Map<RetrieverType, RetrieverType> exploreMap = ImmutableMap.<RetrieverType, RetrieverType>builder()
            .put(RetrieverType.vsubcat, RetrieverType.vsubcat_live)
            .put(RetrieverType.vtag, RetrieverType.vtag_live)
            .build();

    public static void build(RetrieverType retrieverType, String interestKey, Integer blendNum,
                             Map<RetrieveKey, Map<String, Double>> keyFeaturesMap) {
        RetrieverType derivedType = exploreMap.get(retrieverType);
        ;

        if (derivedType != null && blendNum > 0) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(derivedType.name());
            retrieveKey.setAlias(derivedType.alias());
            retrieveKey.setKey(interestKey);
            retrieveKey.addAttribute("maxCnt", 5);
            keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());
        }
    }
}
