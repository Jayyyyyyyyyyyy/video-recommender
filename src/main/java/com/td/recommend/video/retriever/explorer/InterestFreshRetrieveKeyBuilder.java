package com.td.recommend.video.retriever.explorer;

import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.HashMap;
import java.util.Map;

public class InterestFreshRetrieveKeyBuilder {
    private static final Map<RetrieverType, RetrieverType> freshMap = ImmutableMap.<RetrieverType, RetrieverType>builder()
            .put(RetrieverType.vcat, RetrieverType.vcat_fr)
            .put(RetrieverType.vtag, RetrieverType.vtag_fr)
            .put(RetrieverType.vmp3, RetrieverType.vmp3_fr)
            .put(RetrieverType.vbody, RetrieverType.vbody_fr)
            .put(RetrieverType.vsubcat, RetrieverType.vsubcat_fr)
            .put(RetrieverType.vgenre, RetrieverType.vgenre_fr)
            .put(RetrieverType.vauthor, RetrieverType.vauthor_fr)
            .put(RetrieverType.vxcat, RetrieverType.vxcat_fr)
            .put(RetrieverType.vxsubcat, RetrieverType.vxsubcat_fr)
            .put(RetrieverType.vxtag, RetrieverType.vxtag_fr)
            .put(RetrieverType.vtagrank, RetrieverType.vtag_fr)
            .put(RetrieverType.vcatrank, RetrieverType.vcat_fr)
            .put(RetrieverType.vsubcatrank, RetrieverType.vsubcat_fr)
            .put(RetrieverType.vmp3rank, RetrieverType.vmp3_fr)
            .put(RetrieverType.vgenrerank, RetrieverType.vgenre_fr)
            .build();

    public static void build(VideoRecommenderContext context, RetrieverType retrieverType, String interestKey, Integer blendNum,
                             Map<RetrieveKey, Map<String, Double>> keyFeaturesMap) {
        RetrieverType derivedType = freshMap.get(retrieverType);
        int ihf = context.getRecommendRequest().getIhf();
        if (derivedType != null && blendNum > 3) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(derivedType.name());
            retrieveKey.setIhf(String.valueOf(ihf));
            retrieveKey.setAlias(derivedType.alias());
            retrieveKey.setKey(interestKey);
            retrieveKey.addAttribute("maxCnt", 10);
            keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());
        }
    }
}
