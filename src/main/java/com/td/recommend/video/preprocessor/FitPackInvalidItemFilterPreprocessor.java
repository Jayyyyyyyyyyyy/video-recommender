package com.td.recommend.video.preprocessor;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.td.data.profile.item.VideoItem;
import com.td.featurestore.item.ItemsProcessor;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.datasource.TeachingResearchVidSet;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/10/13.
 */
public class FitPackInvalidItemFilterPreprocessor implements ItemsProcessor<PredictItems<DocItem>> {
    private static final Logger logger = LoggerFactory.getLogger(FitPackInvalidItemFilterPreprocessor.class);
    private HashMap<String, Long> filterReasonMap = new HashMap<>();
    private static final Set<Integer> bigVideo = ImmutableSet.of(101, 102, 103, 105, 106, 107, 301, 121);
    public static final Set<String> subcats = ImmutableSet.of("1007","1094","1010","1034");
    private static final Set<Integer> stageSet = ImmutableSet.of(6, 7, 8, 10);
    private VideoRecommenderContext recommendContext;

    public FitPackInvalidItemFilterPreprocessor(VideoRecommenderContext recommendContext) {
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
        taggedMetricRegistry.histogram("fitpack.invalidItem.filtercount").update(items.getSize() - predictItemList.size());

        PredictItems<DocItem> predictItems = new PredictItems<>();
        predictItems.setItems(predictItemList);
        Map<String, Long> after = getRetrieveTypeCountMap(predictItems);

        Map<String, Long> filteredMap = before.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() - after.getOrDefault(entry.getKey(), 0L)));
        logger.info("preprocess before:{}, after:{}, filtered:{}, reasons:{}, diu:{}", before, after, filteredMap, filterReasonMap, recommendContext.getUserItem().getId());
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
        if (!bigVideo.contains(staticDocumentData.getCtype())) {
            filterReasonMap.merge("ctype", 1L, Long::sum);
            return false;
        }

        String uid = recommendContext.getRecommendRequest().getUid();
        if (uid.equals(String.valueOf(staticDocumentData.getUid()))) {
            return true;//自产内容不过滤
        }
        ImmutableSet<Integer> validCstatus;
        if (recommendContext.hasBucket("core_firstcat-yes")) {
            validCstatus = ImmutableSet.of(0);
        } else {
            validCstatus = ImmutableSet.of(0, 7);
        }
        if (!validCstatus.contains(staticDocumentData.getCstatus())) {
            filterReasonMap.merge("cstatus", 1L, Long::sum);
            return false;
        }
        if (!stageSet.contains(staticDocumentData.getCstage())) {
            filterReasonMap.merge("cstage", 1L, Long::sum);
            return false;
        }
        if(!subcats.contains(DocProfileUtils.getSecondCat(item.getItem()))){
            filterReasonMap.merge("subcat", 1L, Long::sum);
            return false;
        }
        if (staticDocumentData.getDuration() < recommendContext.getRecommendRequest().getDuration()) {
            filterReasonMap.merge("duration", 1L, Long::sum);
            return false;
        }

        if (staticDocumentData.getVideo_reprint_flag() != 0) {
            filterReasonMap.merge("reprint", 1L, Long::sum);
            return false;
        }
        if (!isTeachingResearchRetrieve(item) && TeachingResearchVidSet.get().contains(staticDocumentData.getId())) {
            filterReasonMap.merge("research", 1L, Long::sum);
            return false;
        }

        return true;
    }

    boolean isTeachingResearchRetrieve(PredictItem<DocItem> item) {
        for (RetrieveKey retrieveKey : item.getRetrieveKeys()) {
            if (retrieveKey.getType().endsWith("_tr")) {
                return true;
            }
        }
        return false;
    }
}
