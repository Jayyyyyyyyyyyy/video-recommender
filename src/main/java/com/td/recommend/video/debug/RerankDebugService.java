package com.td.recommend.video.debug;


import com.td.data.profile.common.KeyConstants;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.rerank.RatioRule;
import com.td.recommend.commons.rerank.RuleTag;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.recommender.Recommender;
import com.td.recommend.core.recommender.RecommenderBuilder;
import com.td.recommend.core.rerank.DefaultPredictItemSlotReranker;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.*;
import com.td.recommend.video.rerank.PredictItemTagger;
import com.td.recommend.video.rerank.RatioRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class RerankDebugService {
    private static Logger LOG = LoggerFactory.getLogger(RerankDebugService.class);


    private static final int slotNum = 32;

    private static final int VIDEO_RECOMMEND_NUM = 150;
    private static final int NEWS_RECOMMEND_NUM = 400;

    private static final String RANK_HOST_POSTFIX = "rankHost";

    private static RerankDebugService instance = new RerankDebugService();

    public static RerankDebugService getInstance() {
        return instance;
    }

    public RerankDebugService() {
    }

    private List<TaggedItem<PredictItem<DocItem>>> tag(PredictItems<DocItem> predictItems, PredictItemTagger itemTagger) {
        List<TaggedItem<PredictItem<DocItem>>> taggedItems = new ArrayList<>();
        for (PredictItem<DocItem> predictItem : predictItems) {
            TaggedItem taggedItem = itemTagger.tag(predictItem);
            taggedItems.add(taggedItem);
        }

        return taggedItems;
    }

    private void retrieveStartCount(Map<String, Integer> retrieveCountMap, List<RetrieveKey> retrieveKeyList) {
        for (RetrieveKey key : retrieveKeyList) {
            String type = key.getType();
            if (!retrieveCountMap.containsKey(type)) {
                retrieveCountMap.put(type, 0);
            }
            Integer count = retrieveCountMap.get(type);
            count = count + 1;
            retrieveCountMap.put(type, count);
        }

    }

    private void startSubcatCount(Map<String, Integer> subcatCountMap, String subcat) {
        if (!subcatCountMap.containsKey(subcat)) {
            subcatCountMap.put(subcat, 0);
        }
        Integer count = subcatCountMap.get(subcat);
        count = count + 1;
        subcatCountMap.put(subcat, count);
    }

    private void startSubcatRetrieveCount(Map<String, Map<String, Integer>> subcatCountMap, String subcat, List<RetrieveKey> retrieveKeyList) {
        if (!subcatCountMap.containsKey(subcat)) {
            subcatCountMap.put(subcat, new HashMap<>());
        }
        Map<String, Integer> retrieveMap = subcatCountMap.get(subcat);
        for (RetrieveKey key : retrieveKeyList) {
            String type = key.getType();
            if (!retrieveMap.containsKey(type)) {
                retrieveMap.put(type, 0);
            }
            Integer count = retrieveMap.get(type);
            count = count + 1;
            retrieveMap.put(type, count);
        }
        subcatCountMap.put(subcat, retrieveMap);
    }


    private String getSubcat(TaggedItem<PredictItem<DocItem>> taggedItem) {
        String cat = KeyConstants.secondcat;
        String id;
        try {
            id = taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(cat).get()
                    .getId();
        } catch (Exception e) {
            id = "0";
        }
        return id;
    }

    public RerankDebugInfo recommend(VideoRecommenderContext recommendContext) {

        try {

            PredictItemTagger itemTagger = new PredictItemTagger(recommendContext);

            int realSlotNum = slotNum;
            RecommenderBuilder<DocItem> recommenderBuilder;
            RatioRules ratioRules = RatioRules.getInstance();
            List<RatioRule> rules = Collections.emptyList();
            int ihf = recommendContext.getRecommendRequest().getIhf();
            if (Ihf.isRelevant(ihf)) {
                recommenderBuilder = new RelevantRecommenderBuilder(recommendContext);
                rules = ratioRules.getRelevantRules(recommendContext);
            } else if (ihf == Ihf.VMIX_FOLLOW.id()) {
                recommenderBuilder = new FollowRecommenderBuilder(recommendContext);
                rules = ratioRules.getFollowRules(recommendContext);
            } else if (ihf == Ihf.VMIX_CHANNEL.id()) {
                recommenderBuilder = new ChannelRecommenderBuilder(recommendContext);
                realSlotNum = 100;
            } else if (ihf == Ihf.VNOT_PLAY.id()) {
                recommenderBuilder = new NotPlayRecommenderBuilder(recommendContext);
            } else {
                recommenderBuilder = new VideoRecommenderBuilder(recommendContext);
                rules = ratioRules.getFeedRules(recommendContext);
                realSlotNum = 36;

            }
            Recommender<DocItem> recommender = recommenderBuilder.build();
            PredictItems<DocItem> predictItems = recommender.recommend();


            List<TaggedItem<PredictItem<DocItem>>> taggedItems = tag(predictItems, itemTagger);
            List<RerankDebugItem> rerankBeforeDebugItems = new ArrayList<>();
            List<String> docIds = taggedItems.stream().map(item -> item.getItem().getId()).collect(Collectors.toList());

            Map<String, Integer> docInitIndexMap = new HashMap<>();
            Map<String, Integer> retrieveCountMap = new HashMap<>();
            Map<String, Integer> subcatCountMap = new HashMap<>();
            Map<String, Map<String, Integer>> subcatRetrieveCountMap = new HashMap<>();
            for (int i = 0; i < taggedItems.size(); i++) {
                TaggedItem<PredictItem<DocItem>> taggedItem = taggedItems.get(i);
                PredictItem<DocItem> predictItem = taggedItem.getItem();
                RerankDebugItem rerankDebugItem = new RerankDebugItem();
                rerankDebugItem.setId(predictItem.getId());
                rerankDebugItem.setScore(predictItem.getScore());
                rerankDebugItem.setPredictScore(predictItem.getPredictScore());
                rerankDebugItem.setPos(i);
                rerankDebugItem.setRetrieveKeyList(predictItem.getRetrieveKeys());
                String title = predictItem.getItem().getNewsDocumentData().get().getStaticDocumentData().get().getTitle();
                rerankDebugItem.setTitle(title);

                Set<RuleTag> tagSet = taggedItem.getTagSet();
                List<String> tags = new ArrayList<>();
                for (RuleTag ruleTag : tagSet) {
                    tags.add("(" + ruleTag.getId() + "," + ruleTag.getName() + ")");
                }
                StringBuffer sb = new StringBuffer();
                sb.append(String.join(",", tags));
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
                rerankDebugItem.setTags(sb.toString());
                rerankBeforeDebugItems.add(rerankDebugItem);
                docInitIndexMap.put(predictItem.getId(), i);
                retrieveStartCount(retrieveCountMap, predictItem.getRetrieveKeys());
                String subcat = getSubcat(taggedItem);
                startSubcatCount(subcatCountMap, subcat);
                startSubcatRetrieveCount(subcatRetrieveCountMap, subcat, predictItem.getRetrieveKeys());

            }

//
//            RatioRules ratioRules = RatioRules.getInstance();
//            List<RatioRule> rules ;
            String ruletype = recommendContext.getRecommendRequest().getRuleType();
//            if (ruletype.equals(BucketConstants.RULETYPE_EXP)) {
//                realSlotNum = 24;
//                rules = ratioRules.getRatioRules(recommendContext);
//            } else {
//                rules = ratioRules.getOldUserRules(recommendContext);
//            }

//                DefaultSlotReranker<DocItem> slotReranker = new DefaultSlotReranker<>(new PredictItemTagger(recommendContext), rules, METRICS_PREFIX);
            DefaultPredictItemSlotReranker<DocItem> slotReranker = new DefaultPredictItemSlotReranker<>(new PredictItemTagger(recommendContext), rules, "video-recommend-debug");

            long startRerankTime = System.currentTimeMillis();
            List<TaggedItem<PredictItem<DocItem>>> rankedItems = slotReranker.rank(predictItems.getItems(), realSlotNum, predictItems.getSize());

            LOG.info("rerank use time:{}", System.currentTimeMillis() - startRerankTime);
            List<TaggedItem<PredictItem<DocItem>>> taggedItemsAfterRerank = rankedItems;
            if (rankedItems.size() > realSlotNum) {
                taggedItemsAfterRerank = new ArrayList<>(rankedItems.subList(0, realSlotNum));//jvm GC: subList contains parent, new ArrayList avoid cache all, original will be gc
            }
            List<RerankDebugItem> rerankAfterDebugItems = new ArrayList<>();
            for (int i = 0; i < taggedItemsAfterRerank.size(); i++) {
                TaggedItem<PredictItem<DocItem>> taggedItem = taggedItemsAfterRerank.get(i);
                PredictItem<DocItem> predictItem = taggedItem.getItem();
                RerankDebugItem rerankDebugItem = new RerankDebugItem();
                rerankDebugItem.setId(predictItem.getId());
                rerankDebugItem.setScore(predictItem.getScore());
                rerankDebugItem.setPredictScore(predictItem.getPredictScore());
                rerankDebugItem.setPos(i);
                rerankDebugItem.setRetrieveKeyList(predictItem.getRetrieveKeys());

                String title = predictItem.getItem().getNewsDocumentData().get().getStaticDocumentData().get().getTitle();
                rerankDebugItem.setTitle(title);

                rerankDebugItem.setStatus(taggedItem.getStatus());
                rerankDebugItem.setInitPos(docInitIndexMap.get(predictItem.getId()));
                Set<Integer> hitRules = taggedItem.getHitRules();

                List<String> hits = new ArrayList<>();
                for (Integer hitRule : hitRules) {
                    hits.add(hitRule.toString());
                }

                rerankDebugItem.setHitRules(String.join(",", hits));
                rerankDebugItem.setLastHitRule(taggedItem.getLastHitRule());

                Set<RuleTag> tagSet = taggedItem.getTagSet();
                List<String> tags = new ArrayList<>();
                for (RuleTag ruleTag : tagSet) {
                    tags.add("(" + ruleTag.getId() + "," + ruleTag.getName() + ")");
                }
                StringBuffer sb = new StringBuffer();
                sb.append(String.join(",", tags));
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
                rerankDebugItem.setTags(sb.toString());

                rerankAfterDebugItems.add(rerankDebugItem);
            }
            RerankDebugInfo rerankDebugInfo = new RerankDebugInfo();
            rerankDebugInfo.setRerankBeforeItems(rerankBeforeDebugItems);
            rerankDebugInfo.setRerankAfterItems(rerankAfterDebugItems);
            rerankDebugInfo.setRetrieveCount(retrieveCountMap);
            rerankDebugInfo.setSubcatCount(subcatCountMap);
            rerankDebugInfo.setSubcatRetrieveCount(subcatRetrieveCountMap);

            return rerankDebugInfo;
        } catch (Exception ex) {
            LOG.error("RerankDebugService recommend failed:{}", ex);
            return RerankDebugInfo.empty();
        }

    }

}
