package com.td.recommend.video.service;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.data.profile.item.VideoItem;
import com.td.featurestore.feature.IFeature;
import com.td.featurestore.item.KeysItem;
import com.td.recommend.commons.date.TimeUtils;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.commons.response.NewsDoc;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.dao.DocItemDao;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.api.vo.NewsDocBuilder;
import com.td.recommend.video.rank.featuredumper.DynamicFeatureDumper;
import com.td.recommend.video.rank.featuredumper.RelevantDynamicFeatureDumper;
import com.td.recommend.video.rank.featuredumper.RelevantFeatureDumper;
import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import com.td.recommend.video.rank.featuredumper.bean.RelevantDynamicDumpInfo;
import com.td.recommend.video.rank.featureextractor.FeedDynamicFeatureExtractor;
import com.td.recommend.video.rank.featureextractor.RelevantDynamicFeatureExtractor;
import com.td.recommend.video.rank.featureextractor.RelevantGBDTFeatureExtractor;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.rerank.UserNewsRerankableRecommender;
import com.td.recommend.video.utils.RedisClientSingleton;
import com.td.recommend.video.utils.VectorFeatureHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/12/11.
 */
public class VideoRecommendService {
    private static final Logger LOG = LoggerFactory.getLogger(VideoRecommendService.class);
    private final VideoRecommenderContext recommendContext;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public VideoRecommendService(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    public List<NewsDoc> recommend() {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        List<TaggedItem<PredictItem<DocItem>>> resultItems;
        UserItem userItem = recommendContext.getUserItem();
        int num = recommendContext.getRecommendRequest().getNum();
        resultItems = getRerankResultItems(num);
        int ihf = recommendContext.getRecommendRequest().getIhf();

        if(recommendContext.getRecommendRequest().getAppId().equals("t01")) {
            RedisClientSingleton.general.pfadd(recommendContext.getUserType().name(), userItem.getId());
        } else {
            RedisClientSingleton.general.pfadd("others", userItem.getId());
        }

        List<NewsDoc> resultDocs = new ArrayList<>();

        int pos = 0;

        NewsDocBuilder newsDocBuilder = NewsDocBuilder.getInstance();
//        List<FeatureDumper.DumpInfo> dumpInfos = new ArrayList<>();
        List<DynamicDumpInfo> dynamicDumpInfos = new ArrayList<>();
//        List<RelevantFeatureDumper.DumpInfo> relevantDumpInfos = new ArrayList<>();
        List<RelevantDynamicDumpInfo> relevantDynamicDumpInfos = new ArrayList<>();
        //Optional<TDocUserProfile> tDocUserProfile = UserProfileHandler.getDocument(userItem.getId());
        Optional<DynamicDumpInfo.SimUserDoc> bprSimUer = Optional.empty();

        //强插
        List<TaggedItem<PredictItem<DocItem>>> resultItems_v2 = new ArrayList<>();
        Set<String> dedupSet = new HashSet<>();
        if(recommendContext.getRecommendRequest().getFp()==1 &&
                ihf==Ihf.VSHOWDANCE_FEED.id() &&
                StringUtils.isNotBlank(recommendContext.getRecommendRequest().getTrendUid())) { //第一次进去社区首页
            String trendvid = recommendContext.getTrendVidStrings();
            if(StringUtils.isBlank(trendvid)){
                trendvid = recommendContext.getRecommendRequest().getTrendVidString();
            }
            if (StringUtils.isNotBlank(trendvid)) {
                DocItem docItem = new DocItemDao().get(trendvid).get();
                //Optional<DocItem> docItemDao = new DocItemDao().get(trendvid).get();
                Optional<VideoItem> videoItem = docItem.getNewsDocumentData().get().getStaticDocumentData();
                if(Optional.ofNullable(videoItem).isPresent()){
                    TaggedItem<PredictItem<DocItem>> teaserTaggedItem = new TaggedItem<>(new PredictItem<>());
                    teaserTaggedItem.getItem().setRetrieveKeys(Collections.singletonList(new RetrieveKey().setKey("first_access_showdance").setType("teaser_showdance")));
                    teaserTaggedItem.getItem().setItem(docItem);
                    if (resultItems.size() > 0) {
                        teaserTaggedItem.getItem().setPredictId(resultItems.get(0).getItem().getPredictId());
                    }
                    resultItems_v2.add(0, teaserTaggedItem);
                    dedupSet.add(trendvid);
                    LOG.info("trend_recommend show_dance for first_access success: diu={}",
                            this.recommendContext.getRecommendRequest().getDiu());
                }
                else{
                    LOG.error("trend_recommend show_dance for first_access error, get vidItem error ihf={} diu={} trenduid={} requestTrendUid={} requestTrendVid={} ",
                            ihf,
                            this.recommendContext.getRecommendRequest().getDiu(),
                            this.recommendContext.getTrendUid(),
                            this.recommendContext.getRecommendRequest().getTrendUid(),
                            this.recommendContext.getRecommendRequest().getTrendVidString());
                }
            }
            else{
                LOG.error("trend_recommend show_dance for first_access error ihf={} diu={} trenduid={} requestTrendUid={} ",
                        ihf,
                        this.recommendContext.getRecommendRequest().getDiu(),
                        this.recommendContext.getTrendUid(),
                        this.recommendContext.getRecommendRequest().getTrendUid());
            }
        }
        for (TaggedItem<PredictItem<DocItem>> rankedItem : resultItems) {
            if (!dedupSet.contains(rankedItem.getItem().getId())) {

                resultItems_v2.add(rankedItem);
                dedupSet.add(rankedItem.getItem().getId());
                if (resultItems_v2.size() >= num) {
                    break;
                }
            }
        }


        LOG.info("resultItems.size():" + resultItems_v2.size());


        for (TaggedItem<PredictItem<DocItem>> taggedItem : resultItems_v2) {
            PredictItem<DocItem> predictItem = taggedItem.getItem();

            Optional<NewsDoc> newsDocOpt = newsDocBuilder.build(
                    taggedItem,
                    pos,
                    recommendContext);

            if (newsDocOpt.isPresent()) {
                NewsDoc newsDoc = newsDocOpt.get();
                resultDocs.add(newsDoc);
                if (isAppFeed(recommendContext)) {
                    FeedDynamicFeatureExtractor.getInstance().addDynamicDumpInfo(dynamicDumpInfos, predictItem, userItem, bprSimUer, pos, recommendContext);
                }
                if (resultDocs.size() < 10 && isAppRlvt(recommendContext)) {
//                    addRelevantDumpInfo(relevantDumpInfos, predictItem, userItem);
                    if (recommendContext.hasBucket("relevantRecDynamicFeatureDump-yes")) {
                        RelevantDynamicFeatureExtractor.getInstance().addRelevantDynamicDumpInfo(relevantDynamicDumpInfos, predictItem, userItem, bprSimUer, pos, recommendContext);
                    }
                }
                ++pos;
                if (resultDocs.size() >= num) {
                    break;
                }
            } else {
                String factors = predictItem.getRetrieveKeys().stream().map(RetrieveKey::getType).collect(Collectors.joining(","));
                LOG.warn("docId={} with factors={} not found", predictItem.getId(), factors);
            }
        }


        if (isAppFeed(recommendContext)) {
//            FeatureDumper.getInstance().asyncDumpAll(dumpInfos);
            DynamicFeatureDumper.getInstance().asyncDumpAll(dynamicDumpInfos);
        }
        if (isAppRlvt(recommendContext)) {
//            RelevantFeatureDumper.getInstance().asyncDumpAll(relevantDumpInfos);
            RelevantDynamicFeatureDumper.getInstance().asyncDumpAll(relevantDynamicDumpInfos);
        }
        if (resultDocs.size() < num) {
            taggedMetricRegistry.histogram("uservideo.resultsize.lessrate").update(100);
        } else {
            taggedMetricRegistry.histogram("uservideo.resultsize.lessrate").update(0);
        }

        if (resultDocs.size() < num) {
            LOG.info("userId={}, ihf={}, request num={}, result size={}", userItem.getId(), ihf, num, resultDocs.size());
        }

        resultDocsMetrics(taggedMetricRegistry, resultDocs);

        return resultDocs;
    }

    private Optional<DynamicDumpInfo.SimUserDoc> getSimUsers(VideoRecommenderContext recommendContext) {
        UserItem userItem = recommendContext.getUserItem();
        Optional<DynamicDumpInfo.SimUserDoc> bprSimUer;
        if (isAppFeed(recommendContext)) {
            bprSimUer = new VectorFeatureHandler().getBprSimUer(userItem.getId());
        } else {
            bprSimUer = Optional.empty();
        }
        return bprSimUer;
    }

    private void addRelevantDumpInfo(List<RelevantFeatureDumper.DumpInfo> dumpInfos, PredictItem<DocItem> predictItem, UserItem userItem) {
        List<IFeature> features = RelevantGBDTFeatureExtractor.getInstance().extract(predictItem, recommendContext.getQueryItems());
        RelevantFeatureDumper.DumpInfo dumpInfo = new RelevantFeatureDumper.DumpInfo(features, predictItem.getPredictId(), predictItem.getItem().getId(), recommendContext.getRecommendRequest().getVid(), userItem.getId(), predictItem.getPredictScore());
        dumpInfos.add(dumpInfo);
    }

    private void resultDocsMetrics(TaggedMetricRegistry taggedMetricRegistry, List<NewsDoc> resultDocs) {
        resultDocs.stream().flatMap(doc -> new TreeSet<>(Arrays.asList(StringUtils.split(doc.getAlgInfo().getOrDefault("vfactors", "empty"), ','))).stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((vfactor, count) -> {
                    taggedMetricRegistry.meter("uservideo.vfactor.view." + vfactor + ".size").mark(count);
                    taggedMetricRegistry.histogram("uservideo.vfactor." + vfactor + ".size").update(count);
                });

        long currentTimeMillis = System.currentTimeMillis();

        int[] daysCount = new int[9];
        Arrays.fill(daysCount, 0);

        for (NewsDoc resultDoc : resultDocs) {
            try {
                long pubTimestamp = resultDoc.getCtime();
                long days = (currentTimeMillis - pubTimestamp) / TimeUtils.ONE_DAY_IN_MILLS;
                daysCount[(int) (days % 9)] += 1;
            } catch (Exception e) {
                LOG.warn("transform createtime failed id {}", resultDoc.getId(), e);
            }
        }

        for (int i = 0; i < daysCount.length; ++i) {
            int count = daysCount[i];
            if (count != 0) {
                taggedMetricRegistry.meter("uservideo.resultdocs." + count + "days").mark();
            }
        }
    }


    private List<TaggedItem<PredictItem<DocItem>>> getRerankResultItems(int num) {
        UserNewsRerankableRecommender rerankableRecommender = new UserNewsRerankableRecommender(recommendContext);

        String queryId = buildQueryId(recommendContext);
        List<TaggedItem<PredictItem<DocItem>>> taggedItems = rerankableRecommender.recommend(queryId, num);

        return taggedItems;
    }

    private static String buildQueryId(VideoRecommenderContext context) {
        UserItem userItem = context.getUserItem();
        String userId = userItem.getId();
        RecommendRequest recommendRequest = context.getRecommendRequest();
        String cid = recommendRequest.getCid();
        int ihf = recommendRequest.getIhf();
        if (Ihf.isRelevant(ihf)) {
            String vid = context.getRecommendRequest().getVid();
            return cid + "_" + ihf + "_" + vid;
        }
        return cid + "_" + ihf + "_" + userId;
    }

    private List<PredictItem<DocItem>> getFilteredResult(Collection<KeysItem<String, PredictItem<DocItem>>> candidateList) {

        List<PredictItem<DocItem>> resultList = new ArrayList<>();
        Set<String> dedupSet = new HashSet<>();

        for (KeysItem<String, PredictItem<DocItem>> keysItem : candidateList) {
            String docId = keysItem.getItem().getId();
            if (!dedupSet.contains(docId)) {
                dedupSet.add(docId);
                resultList.add(keysItem.getItem());
            }
        }
        return resultList;
    }

    boolean isAppFeed(VideoRecommenderContext context) {
        int ihf = context.getRecommendRequest().getIhf();
        return context.getRecommendRequest().getAppId().equals("t01") && Ihf.isFeed(ihf);
    }

    boolean isAppRlvt(VideoRecommenderContext context) {
        int ihf = context.getRecommendRequest().getIhf();
        return context.getRecommendRequest().getAppId().equals("t01") && Ihf.isRelevant(ihf);
    }
}
