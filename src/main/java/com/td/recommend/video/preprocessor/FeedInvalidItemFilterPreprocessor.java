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
import com.td.recommend.video.datasource.TeachingResearchVidSet;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.ReprintUids;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/10/13.
 */
public class FeedInvalidItemFilterPreprocessor implements ItemsProcessor<PredictItems<DocItem>> {
    private static final Logger logger = LoggerFactory.getLogger(FeedInvalidItemFilterPreprocessor.class);
    private HashMap<String, Long> filterReasonMap = new HashMap<>();
    private static final Set<Integer> mixVideo = ImmutableSet.of(101, 102, 103, 105, 106, 107, 301, 121, 401);
    private static final Set<Integer> smallVideo = ImmutableSet.of(105, 106, 107);
    private static final Set<Integer> stageSet = ImmutableSet.of(6, 7, 8, 10);
    private VideoRecommenderContext recommendContext;
    List<PredictItem<DocItem>> teachItemList = new ArrayList<>();
    List<PredictItem<DocItem>> titleItemList = new ArrayList<>();
    List<PredictItem<DocItem>> opItemList = new ArrayList<>();

    public FeedInvalidItemFilterPreprocessor(VideoRecommenderContext recommendContext) {
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
        if (teachItemList.size() > 0) {
            predictItemList.add(teachItemList.get(ThreadLocalRandom.current().nextInt(teachItemList.size())));
        }
        if (titleItemList.size() > 0) {
            predictItemList.add(titleItemList.get(ThreadLocalRandom.current().nextInt(titleItemList.size())));
        }
        if (opItemList.size() > 0) {
            Collections.shuffle(opItemList);
            predictItemList.addAll(opItemList.subList(0, Math.min(opItemList.size(), 2)));
        }
        taggedMetricRegistry.histogram("uservideo.invalidItem.filtercount").update(items.getSize() - predictItemList.size());

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
        int ihf = recommendContext.getRecommendRequest().getIhf();
        VideoItem staticDocumentData;
        try {
            staticDocumentData = item.getItem().getNewsDocumentData().get().getStaticDocumentData().get();
        } catch (Exception e) {
            filterReasonMap.merge("docprofile", 1L, Long::sum);
            return false;
        }

        String uid = recommendContext.getRecommendRequest().getUid();

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
        if (uid.equals(String.valueOf(staticDocumentData.getUid()))) {
            return true;//自产内容不过滤
        }
        if (!mixVideo.contains(staticDocumentData.getCtype())) {
            filterReasonMap.merge("ctype", 1L, Long::sum);
            return false;
        }
        if (smallVideo.contains(staticDocumentData.getCtype()) && staticDocumentData.getIs_distribute() == 0) {
            filterReasonMap.merge("distribute", 1L, Long::sum);
            return false;
        }
        if(StringUtils.isBlank(staticDocumentData.getTitle().trim())){
            filterReasonMap.merge("title", 1L, Long::sum);
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

        if (!isTeachingResearchRetrieve(item) && TeachingResearchVidSet.get().contains(staticDocumentData.getId())) {
            filterReasonMap.merge("research", 1L, Long::sum);
            return false;
        }

        if (isBadCover(item)) {
            filterReasonMap.merge("cover", 1L, Long::sum);
            return false;
        }
//        if (isOldNonTeaching(item)) {
//            filterReasonMap.merge("old-non-teaching", 1L, Long::sum);
//            return false;
//        }
        if (isOldPractice(item) && recommendContext.hasBucket("teaching_filter-yes")) {
            return false;
        }
        if (item.getRetrieveKeys().stream().anyMatch(i -> i.getKey().startsWith("vbert"))
                && DocProfileUtils.getTalentStar(item.getItem()) < 4) {
            filterReasonMap.merge("bert<4star", 1L, Long::sum);
            return false;
        }
        if (item.getRetrieveKeys().stream().anyMatch(i -> i.getKey().contains("search"))
                && DocProfileUtils.getTalentStar(item.getItem()) < 4
                && recommendContext.hasBucket("realtimesearch-exp")) {
            return false;
        }
        // 这几个放在最后
        if (hasContentResearchRetrieve(item)) {
            teachItemList.add(item);
            filterReasonMap.merge("content_tr", 1L, Long::sum);
            return false;
        }
        if (hasTitleResearchRetrieve(item)) {
            titleItemList.add(item);
            filterReasonMap.merge("title_tr", 1L, Long::sum);
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

    boolean hasContentResearchRetrieve(PredictItem<DocItem> item) {
        for (RetrieveKey retrieveKey : item.getRetrieveKeys()) {
            if (retrieveKey.getType().equals("vsubcat_tr")) {
                return true;
            }
        }
        return false;
    }

    boolean hasTitleResearchRetrieve(PredictItem<DocItem> item) {
        for (RetrieveKey retrieveKey : item.getRetrieveKeys()) {
            if (retrieveKey.getType().equals("vtitle_tr")) {
                return true;
            }
        }
        return false;
    }


    boolean isBadCover(PredictItem<DocItem> item) {
        if (DocProfileUtils.getVideoQuality2Split(item.getItem()) == 1
                && DocProfileUtils.getTalentStar(item.getItem()) <= 3
                && DocProfileUtils.getVideoQuality(item.getItem()) < 300) {
            return true;
        }
        return false;
    }

    boolean isOldPractice(PredictItem<DocItem> item) {
        long oldTime = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L;
        if (DocProfileUtils.getCtime(item.getItem()) < oldTime
                && DocProfileUtils.getFirstCat(item.getItem()).equals("264")
                && DocProfileUtils.getContentTeach(item.getItem()).equals("日常习作")) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        DocItem docItem = new DocItemDao().get("1500663144652").get();
        String firstCat = DocProfileUtils.getFirstCat(docItem);
        String secondCat = DocProfileUtils.getSecondCat(docItem);
        System.out.println(firstCat.equals("1006") && ImmutableSet.of("1009").contains(secondCat));
    }
}
