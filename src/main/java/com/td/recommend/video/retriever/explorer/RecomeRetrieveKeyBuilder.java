package com.td.recommend.video.retriever.explorer;

import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.HashMap;
import java.util.Map;

public class RecomeRetrieveKeyBuilder {
    private static final Map<RetrieverType, RetrieverType> exploreMap = ImmutableMap.<RetrieverType, RetrieverType>builder() //映射
            .put(RetrieverType.vcat, RetrieverType.vcat_rc) // firstcat.tagid
            .put(RetrieverType.vtag, RetrieverType.vtag_rc) // content_tag.tagid
            .put(RetrieverType.vsubcat, RetrieverType.vsubcat_rc) //secondcat.tagid
            .put(RetrieverType.vphrase, RetrieverType.vphrase_rc) //content_phrase.tagid //
            .put(RetrieverType.vcat_st, RetrieverType.vcat_rc)
            .put(RetrieverType.vtag_st, RetrieverType.vtag_rc)
            .put(RetrieverType.vsubcat_st, RetrieverType.vsubcat_rc)
            .build();

    public static void build(RetrieverType retrieverType, String interestKey, Integer blendNum,
                             Map<RetrieveKey, Map<String, Double>> keyFeaturesMap) {
        RetrieverType derivedType = exploreMap.get(retrieverType);
        ;

        if (derivedType != null && blendNum > 0) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(derivedType.name()); // vcat_rc
            retrieveKey.setAlias(derivedType.alias());
            retrieveKey.setKey(interestKey); // 229
            keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());
        }
    }
}
