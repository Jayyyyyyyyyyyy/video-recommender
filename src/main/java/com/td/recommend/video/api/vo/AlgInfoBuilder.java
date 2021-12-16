package com.td.recommend.video.api.vo;

import com.google.common.collect.ImmutableList;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.rerank.RuleTag;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.commons.retriever.KeyTag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/7/11.
 */
public class AlgInfoBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(AlgInfoBuilder.class);

    public static Map<String, String> buildAlgInfo(TaggedItem<PredictItem<DocItem>> taggedItem, int pos, VideoRecommenderContext recommendContext) {
        List<String> vFactorList = new ArrayList<>();

        PredictItem<DocItem> predictItem = taggedItem.getItem();

        List<RetrieveKey> retrieveKeys = predictItem.getRetrieveKeys();

        for (RetrieveKey retrieveKey : retrieveKeys) {
            String vFactor = getVFactor(retrieveKey);
            vFactorList.add(vFactor);
        }

        String vFactors = String.join(",", vFactorList);
        String points = retrieveKeys.stream().map(
                retrieveKey -> {
                    String type = retrieveKey.getType();

                    String key = retrieveKey.getKey();
                    if (key == null) {
                        return type;
                    } else {
                        return key;
                    }
                }
        ).collect(Collectors.joining(","));


        String factors = retrieveKeys.stream()
                .map(RetrieveKey::getType)
                .collect(Collectors.joining(","));

        String reasons = retrieveKeys.stream().map(RetrieveKey::getReason).filter(r -> !r.isEmpty()).distinct().limit(1).collect(Collectors.joining(","));

        String rulestatus = String.valueOf(taggedItem.getStatus());
        String secondRuleStatus = String.valueOf(taggedItem.getSecondStatus());

        Map<String, String> algInfo = new HashMap<>();
        RecommendRequest recommendRequest = recommendContext.getRecommendRequest();
        String channel = recommendContext.getRecommendRequest().getChannel();

        int ihf = recommendRequest.getIhf();
        String mashtype;
        if (Ihf.isRelevant(ihf)) {
            mashtype = "relevant";
            String vid = recommendContext.getRecommendRequest().getVid();
            if (StringUtils.isNotBlank(vid)) {
                algInfo.put("currentDocId", vid);
            }
        } else if (Ihf.VMIX_FOLLOW.id() == ihf) {
            mashtype = "follow";
        } else {
            mashtype = "feed";
        }

        algInfo.put("mashtype", mashtype);
        algInfo.put("predictId", predictItem.getPredictId());
        algInfo.put("points", points);
        algInfo.put("factors", factors);
        algInfo.put("vfactors", vFactors);
        algInfo.put("reasons", reasons);
        algInfo.put("score", Double.toString(predictItem.getScore()));
        algInfo.put("predictScore", Double.toString(predictItem.getPredictScore()));
        algInfo.put("rerankScore", Double.toString(predictItem.getRerankScore()));
        algInfo.put("rerankPredictScore", Double.toString(predictItem.getRerankPredictScore()));
        algInfo.put("pos", String.valueOf(pos));
        algInfo.put("rulestatus", rulestatus);
        algInfo.put("secondRuleStatus", secondRuleStatus);
        algInfo.put("userType", recommendContext.getUserType().name());
        algInfo.put("userInterest", recommendContext.getUserInterest());
        algInfo.put("activeTime", String.valueOf(recommendContext.getActiveTime()));

        String hitRules = taggedItem.getHitRules().stream().map(rule -> rule.toString()).collect(Collectors.joining(","));
        algInfo.put("hitRules", hitRules);

        String secondHitRules = taggedItem.getSecondHitRules().stream().map(rule -> rule.toString()).collect(Collectors.joining(","));
        algInfo.put("secondHitRules", secondHitRules);

        String ruleTags = taggedItem.getTagSet().stream().map(tag -> "(" + tag.getId() + "," + tag.getName() + ")").collect(Collectors.joining(","));
        StringBuffer sb = new StringBuffer();
        sb.append(ruleTags);
        try {
            for (RuleTag ruletag : taggedItem.getParentRuleTagMap().keySet()) {
                RuleTag sonTag = taggedItem.getParentRuleTagMap().get(ruletag).get(0);
                String tagstr = "(" + sonTag.getId() + "," + sonTag.getName() + ")";
                sb.append(",");
                sb.append(tagstr);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        algInfo.put("ruleTags", sb.toString());

        algInfo.put("lastHitRule", String.valueOf(taggedItem.getLastHitRule()));
        algInfo.put("secondLastHitRule", String.valueOf(taggedItem.getSecondLastHitRule()));
        algInfo.put("initIndex", String.valueOf(taggedItem.getInitIndex()));
        algInfo.put("curIndex", String.valueOf(taggedItem.getCurIndex()));
        algInfo.put("secondInitIndex", String.valueOf(taggedItem.getSecondInitIndex()));
        algInfo.put("secondCurIndex", String.valueOf(taggedItem.getSecondCurIndex()));
        algInfo.put("posChain", StringUtils.join(ImmutableList.of(taggedItem.getInitIndex(), taggedItem.getCurIndex(), taggedItem.getSecondInitIndex(), taggedItem.getSecondCurIndex(), pos), ","));
        predictItem.getAttributes().forEach((k, v) -> algInfo.put(k, v.toString()));
        return algInfo;
    }

    private static String getVFactor(RetrieveKey retrieveKey) {
        String vfactor = retrieveKey.getType();

        if (retrieveKey.getTags().contains(KeyTag.recent.name())) {
            vfactor += "-" + KeyTag.recent.name();
        } else if (retrieveKey.getTags().contains(KeyTag.sixhour.name())) {
            vfactor += "-" + KeyTag.sixhour.name();
        }

        return vfactor;
    }
}
