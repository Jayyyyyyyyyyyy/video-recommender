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
import com.td.recommend.docstore.dao.DocItemDao;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.ReprintUids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/10/13.
 */
public class ShowDanceFeedInvalidItemFilterPreprocessor implements ItemsProcessor<PredictItems<DocItem>> {
    private static final Logger logger = LoggerFactory.getLogger(ShowDanceFeedInvalidItemFilterPreprocessor.class);
    private HashMap<String, Long> filterReasonMap = new HashMap<>();
    private static final Set<Integer> ctypeSet = ImmutableSet.of(103, 105,107, 501, 502, 503);
    private static final Set<Integer> stageSet = ImmutableSet.of(6, 7, 8, 9, 10);
    private VideoRecommenderContext recommendContext;

    public ShowDanceFeedInvalidItemFilterPreprocessor(VideoRecommenderContext recommendContext) {
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

        taggedMetricRegistry.histogram("showdance.invalidItem.filtercount").update(items.getSize() - predictItemList.size());

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

        String uid = recommendContext.getRecommendRequest().getUid();

        if (staticDocumentData.getCstatus() != 0) {
            filterReasonMap.merge("cstatus", 1L, Long::sum);
            return false;
        }
        if (!stageSet.contains(staticDocumentData.getCstage())) {
            filterReasonMap.merge("cstage", 1L, Long::sum);
            return false;
        }
        if (uid.equals(String.valueOf(staticDocumentData.getUid()))) {
            return true;//?????????????????????
        }
        if (DocProfileUtils.getCtype(item.getItem()) == 103 && !DocProfileUtils.getFirstCat(item.getItem()).equals("264")) {
            filterReasonMap.merge("dance", 1L, Long::sum);
            return false;
        }
        if(DocProfileUtils.getCtime(item.getItem()) < System.currentTimeMillis()-365*24*3600*1000L){
            filterReasonMap.merge("fresh", 1L, Long::sum);
            return false;
        }

        if (!ctypeSet.contains(DocProfileUtils.getCtype(item.getItem()))
                && item.getRetrieveKeys().stream().noneMatch(i -> i.getType().equals("vofalbum"))) {
            filterReasonMap.merge("ctype", 1L, Long::sum);
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

        if (ReprintUids.get().contains(DocProfileUtils.getUid(item.getItem()))) {
            filterReasonMap.merge("reprint-uid", 1L, Long::sum);
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        DocItem docItem = new DocItemDao().get("1500663144652").get();
        String firstCat = DocProfileUtils.getFirstCat(docItem);
        String secondCat = DocProfileUtils.getSecondCat(docItem);
        System.out.println(firstCat.equals("1006") && ImmutableSet.of("1009").contains(secondCat));
    }
}
