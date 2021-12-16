package com.td.recommend.video.preprocessor;

import com.td.data.profile.TDocItem;
import com.td.featurestore.item.ItemsProcessor;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.feedback.LatentNegativeFeedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2017/10/13.
 */
public class LatentFeedbackPreprocessor implements ItemsProcessor<PredictItems<DocItem>> {
    private static final Logger logger = LoggerFactory.getLogger(LatentFeedbackPreprocessor.class);
    private VideoRecommenderContext recommendContext;
    private static final Set<String> latentRetrieveTypes = new HashSet<>();

    public LatentFeedbackPreprocessor(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    static {
        for (RetrieverType retrieverType : RetrieverType.values()) {
            if (retrieverType.name().equals(retrieverType.alias())) {
                latentRetrieveTypes.add(retrieverType.name());
            }
        }
        for (RetrieverType.GTop retrieverType : RetrieverType.GTop.values()) {
            latentRetrieveTypes.add(retrieverType.name());
        }
        for (RetrieverType.Top retrieverType : RetrieverType.Top.values()) {
            latentRetrieveTypes.add(retrieverType.name());
        }
    }

    @Override
    public PredictItems<DocItem> process(PredictItems<DocItem> items) {
        UserItem userItem = recommendContext.getUserItem();
        Map<String, Integer> maxCntMap;
        maxCntMap = LatentNegativeFeedback.compute(userItem, RetrieverType.vcat.name());

        Map<String, Integer> currentMap = new HashMap<>();
        Map<String, Integer> filteredMap = new HashMap<>();
        PredictItems<DocItem> predictItems = new PredictItems<>();
        double ctrLimit = 0.1;

        for (PredictItem<DocItem> item : items) {
            if (item.getRetrieveKeys().stream().map(RetrieveKey::getType).noneMatch(latentRetrieveTypes::contains)) {
                predictItems.add(item);
            } else {//隐式召回
                String cat;
                cat = DocProfileUtils.getFirstCat(item.getItem());
                Integer maxCnt = maxCntMap.getOrDefault(cat, LatentNegativeFeedback.maxCount);
                Integer currentCnt = currentMap.getOrDefault(cat, 0);

                if (currentCnt < maxCnt && isValidLantentCtr(item, ctrLimit)) {
                    currentMap.merge(cat, 1, Integer::sum);
                    predictItems.add(item);
                } else {
                    filteredMap.merge(cat, 1, Integer::sum);
                }
            }
        }
        logger.info("latent negative feedback filtered:{} currentMap:{} maxCnt:{}", filteredMap, currentMap, maxCntMap);
        return predictItems;
    }

    boolean isValidLantentCtr(PredictItem<DocItem> item, double ctrLimit) {
        if (item.getRetrieveKeys().size() != 1) {
            return true;
        }
        try {
            String type = item.getRetrieveKeys().get(0).getType();
            Map<String, TDocItem> dcCtrMap = item.getItem().getNewsDocumentData().get().getDynamicDocumentDataNew().get().getRawMap().get("recom").dcCtrMap;
            double ctr = dcCtrMap.get(type + "_*").ctr;
            double view = dcCtrMap.get(type + "_*").view;
            if (view < 1000) {
                return true;
            }
            if (ctr > ctrLimit) {
                return true;
            } else {
                String pos = item.getAdditionalItem().getTag("pos").orElse("-1");
                logger.info("latent valid ctr filtered id:{}, ctr:{} retrieve type:{}, pos:{}", item.getId(), ctr, type, pos);
                return false;
            }
        } catch (Exception e) {
            return true;
        }

    }

}
