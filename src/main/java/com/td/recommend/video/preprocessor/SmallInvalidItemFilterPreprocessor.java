package com.td.recommend.video.preprocessor;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.td.data.profile.item.VideoItem;
import com.td.featurestore.item.ItemsProcessor;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/10/13.
 */
public class SmallInvalidItemFilterPreprocessor implements ItemsProcessor<PredictItems<DocItem>> {
    private static final Logger logger = LoggerFactory.getLogger(SmallInvalidItemFilterPreprocessor.class);
    private HashMap<String, Long> filterReasonMap = new HashMap<>();
    private static final Set<Integer> smallVideo = ImmutableSet.of(105,106,107);
    private static final Set<Integer> stageSet = ImmutableSet.of(6, 7, 8, 10);
    private VideoRecommenderContext recommendContext;

    public SmallInvalidItemFilterPreprocessor(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public PredictItems<DocItem> process(PredictItems<DocItem> items) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        List<PredictItem<DocItem>> predictItemList = new ArrayList<>();
        Map<String, Long> before = getRetrieveTypeCountMap(items);

        for (PredictItem<DocItem> item : items) {
            if (isValid(item)) {
                predictItemList.add(item);
            }
        }

        taggedMetricRegistry.histogram("uservideo.invalidItem.filtercount").update(items.getSize() - predictItemList.size());

        PredictItems<DocItem> predictItems = new PredictItems<>();
        predictItems.setItems(predictItemList);

        Map<String, Long> after = getRetrieveTypeCountMap(predictItems);

        Map<String, Long> filteredMap = before.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() - after.getOrDefault(entry.getKey(), 0L)));
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            logger.info("preprocess before:{}, after:{}, filtered:{}, reasons:{}, diu:{}", before, after, filteredMap, filterReasonMap, recommendContext.getUserItem().getId());
        }
        return predictItems;
    }

    private Map<String, Long> getRetrieveTypeCountMap(PredictItems<DocItem> predictItems) {
        return predictItems.getItems().stream().
                flatMap(i -> i.getRetrieveKeys().stream().map(RetrieveKey::getType))
                .collect(Collectors.groupingBy(key -> key, Collectors.counting()));
    }

    private boolean isValid(PredictItem<DocItem> item) {
        VideoItem staticDocumentData;
        try {
            staticDocumentData = item.getItem().getNewsDocumentData().get().getStaticDocumentData().get();
        } catch (Exception e) {
            filterReasonMap.merge("docprofile", 1L, Long::sum);
            return false;
        }
        if (!smallVideo.contains(staticDocumentData.getCtype())) {
            filterReasonMap.merge("ctype", 1L, Long::sum);
            return false;
        }
        String uid = recommendContext.getRecommendRequest().getUid();
        if (uid.equals(String.valueOf(staticDocumentData.getUid()))) {
            return true;//自产内容不过滤
        }
        if (staticDocumentData.getCstatus() != 0) {
            filterReasonMap.merge("cstatus", 1L, Long::sum);
            return false;
        }
        if (!stageSet.contains(staticDocumentData.getCstage())) {
            filterReasonMap.merge("cstage", 1L, Long::sum);
            return false;
        }
        if (staticDocumentData.getDuration() < recommendContext.getRecommendRequest().getDuration()) {
            filterReasonMap.merge("duration", 1L, Long::sum);
            return false;
        }
        return true;
    }
}
