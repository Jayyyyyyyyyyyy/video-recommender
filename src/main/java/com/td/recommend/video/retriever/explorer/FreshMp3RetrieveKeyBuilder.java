package com.td.recommend.video.retriever.explorer;

import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.HashMap;
import java.util.Map;

public class FreshMp3RetrieveKeyBuilder {
    private static final Map<RetrieverType, RetrieverType> exploreMap = ImmutableMap.<RetrieverType, RetrieverType>builder()
            .put(RetrieverType.vcat, RetrieverType.vcat_f3)
            .put(RetrieverType.vtag, RetrieverType.vtag_f3)
            .put(RetrieverType.vsubcat, RetrieverType.vsubcat_f3)
            .put(RetrieverType.vxfollow, RetrieverType.vxfollow_f3)
            .build();

    public static void build(RetrieverType retrieverType, String interestKey, Integer blendNum,
                             Map<RetrieveKey, Map<String, Double>> keyFeaturesMap) {
        RetrieverType derivedType = exploreMap.get(retrieverType);

        if (derivedType != null && blendNum > 0) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(derivedType.name());
            retrieveKey.setAlias(derivedType.alias());
            retrieveKey.setKey(interestKey);
            retrieveKey.setReason("新歌新舞");
            keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());
        }
    }
}
