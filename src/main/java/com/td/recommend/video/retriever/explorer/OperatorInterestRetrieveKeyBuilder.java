package com.td.recommend.video.retriever.explorer;

import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.HashMap;
import java.util.Map;

public class OperatorInterestRetrieveKeyBuilder {
    private static final Map<RetrieverType, RetrieverType> opeMap = ImmutableMap.<RetrieverType, RetrieverType>builder()
            .put(RetrieverType.vsubcat, RetrieverType.vsubcat_op)
            .put(RetrieverType.vphrase, RetrieverType.vphrase_op)
            .put(RetrieverType.vtag, RetrieverType.vtag_op)
            .build();

    public static void build(RetrieverType retrieverType, String interestKey, Integer blendNum,
                             Map<RetrieveKey, Map<String, Double>> keyFeaturesMap) {
        RetrieverType derivedType = opeMap.get(retrieverType);
        if (derivedType != null && blendNum > 0) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(derivedType.name());
            retrieveKey.setAlias(derivedType.alias());
            retrieveKey.setKey(interestKey);
            retrieveKey.addAttribute("maxCnt", 3);
            keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());

        }
    }
}
