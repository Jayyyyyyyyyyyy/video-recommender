package com.td.recommend.video.retriever.explorer;

import com.google.common.collect.ImmutableMap;
import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.TagExtendMap;
import com.td.recommend.video.profile.TagExtendDict;
import com.td.recommend.video.retriever.RetrieverType;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.*;

public class ExtendRetrieveKeyBuilder {
    public static final int defaultCount = 35;
    private static final Map<RetrieverType, RetrieverType> deriveMap = ImmutableMap.<RetrieverType, RetrieverType>builder()
            .put(RetrieverType.vmp3, RetrieverType.vmp3_ext)
            .put(RetrieverType.vauthor, RetrieverType.vauthor_ext)

            .put(RetrieverType.vxmp3, RetrieverType.vmp3_ext)
            .put(RetrieverType.vxauthor, RetrieverType.vauthor_ext)
            .build();

    public static Set<RetrieveKey> build(UserItem userItem, RetrieveKey originKey) {
        RetrieverType derivedType = deriveMap.get(RetrieverType.valueOf(originKey.getType()));
        if (derivedType == null) {
            return Collections.emptySet();
        }
        HashSet<RetrieveKey> resultSet = new HashSet<>();
        getSampledSimilarItems(originKey).forEach(key -> {
            if (shouldExtend(userItem, originKey.getType(), key)) {
                int maxCnt = (int) originKey.getAttribute("maxCnt").orElse(defaultCount);
                RetrieveKey derivedKey = new RetrieveKey();
                derivedKey.setType(derivedType.name());
                derivedKey.setAlias(derivedType.alias());
                derivedKey.setKey(key);
                derivedKey.addAttribute("maxCnt", Math.min(defaultCount, maxCnt));
                resultSet.add(derivedKey);
            }
        });

        return resultSet;
    }

    private static boolean shouldExtend(UserItem userItem, String type, String key) {
        type = type.replaceAll("vx", "v");
        String stFacet = "st_" + type.split("_")[0] + "_ck";
        if (type.contains("author")) {
            stFacet = "st_vauthor_uid_ck";
        }
        Map<String, TVariance> shortInterests = UserProfileUtils.getVarianceFeatureMap(userItem, stFacet);
        TVariance tVariance = shortInterests.get(key);
        if (tVariance == null) {
            return true;
        } else {
            return tVariance.getNegCnt() < 5;
        }

    }

    private static List<String> getSampledSimilarItems(RetrieveKey retrieveKey) {
        List<String> extendedTags;
        int maxExtendNum = 2;

        String targetKey;
        if (retrieveKey.getType().contains("mp3")) {
            targetKey = "vmp3_" + retrieveKey.getKey();
            List<String> extendedTagsAll = TagExtendMap.get(targetKey);
            extendedTags = extendedTagsAll.subList(0, Math.min(5, extendedTagsAll.size()));
        } else if (retrieveKey.getType().contains("author")) {
            targetKey = "vauthor_uid_" + retrieveKey.getKey();
            List<String> extendedTagsAll = TagExtendMap.get(targetKey);
            extendedTags = extendedTagsAll.subList(0, Math.min(5, extendedTagsAll.size()));
        } else {
            extendedTags = TagExtendDict.get(retrieveKey.getType().split("_")[0], retrieveKey.getKey());
        }

        BetaDistribution bd = new BetaDistribution(1, 3);
        List<String> result = new ArrayList<>();

        int extendNum = Math.min(maxExtendNum, extendedTags.size());
        for (int i = 0; i < extendNum; i++) {
            int sample = (int) (bd.sample() * extendedTags.size());
            result.add(extendedTags.get(sample));
        }
        return result;
    }
}
