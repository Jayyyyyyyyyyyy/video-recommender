package com.td.recommend.video.retriever.explorer;

import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.HashMap;
import java.util.Map;

public class EnsureRetrieveKeyBuilder {
    private static final Map<RetrieverType, RetrieverType> ensureMap = ImmutableMap.<RetrieverType, RetrieverType>builder()
            .put(RetrieverType.vcat, RetrieverType.vcat_en)
            .put(RetrieverType.vtag, RetrieverType.vtag_en)
            .put(RetrieverType.vmp3, RetrieverType.vmp3_en)
            .put(RetrieverType.vsubcat, RetrieverType.vsubcat_en)
            .put(RetrieverType.vgenre, RetrieverType.vgenre_en)
            .put(RetrieverType.vauthor, RetrieverType.vauthor_en)
            .put(RetrieverType.vcat_st, RetrieverType.vcat_en)
            .put(RetrieverType.vtag_st, RetrieverType.vtag_en)
            .put(RetrieverType.vmp3_st, RetrieverType.vmp3_en)
            .put(RetrieverType.vsubcat_st, RetrieverType.vsubcat_en)
            .put(RetrieverType.vgenre_st, RetrieverType.vgenre_en)
            .put(RetrieverType.vauthor_st, RetrieverType.vauthor_en)
            .put(RetrieverType.vxcat, RetrieverType.vxcat_en)
            .put(RetrieverType.vxtag, RetrieverType.vxtag_en)
            .put(RetrieverType.vxsubcat, RetrieverType.vxsubcat_en)
            .put(RetrieverType.vxcat_st, RetrieverType.vxcat_en)
            .put(RetrieverType.vxtag_st, RetrieverType.vxtag_en)
            .put(RetrieverType.vxsubcat_st, RetrieverType.vxsubcat_en)
            .put(RetrieverType.vtagrank, RetrieverType.vtag_en)
            .put(RetrieverType.vcatrank, RetrieverType.vcat_en)
            .put(RetrieverType.vsubcatrank, RetrieverType.vsubcat_en)
            .put(RetrieverType.vmp3rank, RetrieverType.vmp3_en)
            .put(RetrieverType.vmp3rank_st, RetrieverType.vmp3_en)
            .put(RetrieverType.vgenrerank, RetrieverType.vgenre_en)
            .put(RetrieverType.vgenrerank_st, RetrieverType.vgenre_en)
            .put(RetrieverType.vcatrank_st, RetrieverType.vcat_en)
            .put(RetrieverType.vtagrank_st, RetrieverType.vtag_en)
            .put(RetrieverType.vsubcatrank_st, RetrieverType.vsubcat_en)
            .put(RetrieverType.vauthorrank_st, RetrieverType.vauthor_en)

            .build();
    public static void build(RetrieverType retrieverType, String interestKey, Integer blendNum,
                             Map<RetrieveKey, Map<String, Double>> keyFeaturesMap) {

        RetrieverType derivedType = ensureMap.get(retrieverType);

        if (derivedType != null && blendNum > 3) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(derivedType.name());
            retrieveKey.setAlias(derivedType.alias());
            retrieveKey.setKey(interestKey);
            retrieveKey.addAttribute("maxCnt", 5);
            keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());
        }
    }
}
