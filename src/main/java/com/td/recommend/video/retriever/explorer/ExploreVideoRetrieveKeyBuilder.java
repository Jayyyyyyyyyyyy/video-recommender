package com.td.recommend.video.retriever.explorer;

import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.HashMap;
import java.util.Map;

public class ExploreVideoRetrieveKeyBuilder {
    private static final Map<RetrieverType, RetrieverType> exploreMap = ImmutableMap.<RetrieverType, RetrieverType>builder()
            .put(RetrieverType.vcat, RetrieverType.vcat_ev)
            .put(RetrieverType.vtag, RetrieverType.vtag_ev)
            .put(RetrieverType.vmp3, RetrieverType.vmp3_ev)
            .put(RetrieverType.vsubcat, RetrieverType.vsubcat_ev)
            .put(RetrieverType.vgenre, RetrieverType.vgenre_ev)
            .put(RetrieverType.vauthor, RetrieverType.vauthor_ev)
            .put(RetrieverType.vbody, RetrieverType.vbody_ev)
            .put(RetrieverType.vcat_st, RetrieverType.vcat_ev)
            .put(RetrieverType.vtag_st, RetrieverType.vtag_ev)
            .put(RetrieverType.vmp3_st, RetrieverType.vmp3_ev)
            .put(RetrieverType.vsubcat_st, RetrieverType.vsubcat_ev)
            .put(RetrieverType.vgenre_st, RetrieverType.vgenre_ev)
            .put(RetrieverType.vauthor_st, RetrieverType.vauthor_ev)
            .put(RetrieverType.vbody_st, RetrieverType.vbody_ev)
            .put(RetrieverType.vxcat, RetrieverType.vxcat_ev)
            .put(RetrieverType.vxtag, RetrieverType.vxtag_ev)
            .put(RetrieverType.vxsubcat, RetrieverType.vxsubcat_ev)
            .put(RetrieverType.vxcat_st, RetrieverType.vxcat_ev)
            .put(RetrieverType.vxtag_st, RetrieverType.vxtag_ev)
            .put(RetrieverType.vxsubcat_st, RetrieverType.vxsubcat_ev)

            .put(RetrieverType.svcat, RetrieverType.svcat_ev)
            .put(RetrieverType.svtag, RetrieverType.svtag_ev)
            .put(RetrieverType.svmp3, RetrieverType.svmp3_ev)
            .put(RetrieverType.svsubcat, RetrieverType.svsubcat_ev)
            .put(RetrieverType.svgenre, RetrieverType.svgenre_ev)
            .put(RetrieverType.svauthor, RetrieverType.svauthor_ev)
            .put(RetrieverType.svcat_st, RetrieverType.svcat_ev)
            .put(RetrieverType.svtag_st, RetrieverType.svtag_ev)
            .put(RetrieverType.svmp3_st, RetrieverType.svmp3_ev)
            .put(RetrieverType.svsubcat_st, RetrieverType.svsubcat_ev)
            .put(RetrieverType.svgenre_st, RetrieverType.svgenre_ev)
            .put(RetrieverType.svauthor_st, RetrieverType.svauthor_ev)
            .put(RetrieverType.vtagrank, RetrieverType.vtag_ev)
            .put(RetrieverType.vcatrank, RetrieverType.vcat_ev)
            .put(RetrieverType.vsubcatrank, RetrieverType.vsubcat_ev)
            .put(RetrieverType.vmp3rank, RetrieverType.vmp3_ev)
            .put(RetrieverType.vmp3rank_st, RetrieverType.vmp3_ev)
            .put(RetrieverType.vgenrerank, RetrieverType.vgenre_ev)
            .put(RetrieverType.vgenrerank_st, RetrieverType.vgenre_ev)
            .put(RetrieverType.vcatrank_st, RetrieverType.vcat_ev)
            .put(RetrieverType.vtagrank_st, RetrieverType.vtag_ev)
            .put(RetrieverType.vsubcatrank_st, RetrieverType.vsubcat_ev)
            .put(RetrieverType.vauthorrank_st, RetrieverType.vauthor_ev)
            .build();

    public static void build(VideoRecommenderContext context, RetrieverType retrieverType, String interestKey, Integer blendNum,
                             Map<RetrieveKey, Map<String, Double>> keyFeaturesMap) {
        RetrieverType derivedType = exploreMap.get(retrieverType);
        int ihf = context.getRecommendRequest().getIhf();

        if (derivedType != null && blendNum > 3) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(derivedType.name());
            retrieveKey.setIhf(String.valueOf(ihf));
            retrieveKey.setAlias(derivedType.alias());
            retrieveKey.setKey(interestKey);
            retrieveKey.addAttribute("maxCnt", 5);
            keyFeaturesMap.computeIfAbsent(retrieveKey, k -> new HashMap<>());
        }
    }
}
