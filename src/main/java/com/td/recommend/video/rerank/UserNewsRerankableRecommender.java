package com.td.recommend.video.rerank;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.td.data.profile.TVariance;
import com.td.data.profile.item.ItemDocumentData;
import com.td.data.profile.item.VideoItem;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.rerank.RatioRule;
import com.td.recommend.commons.rerank.SlotRankResult;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.blender.BlendPolicy;
import com.td.recommend.core.blender.ItemIdValidator;
import com.td.recommend.core.recommender.Recommender;
import com.td.recommend.core.recommender.RecommenderBuilder;
import com.td.recommend.core.rerank.DefaultPredictItemSlotReranker;
import com.td.recommend.core.rerank.SecondTaggedItemSlotReranker;
import com.td.recommend.core.rerank.core.TaggedItemSlotReranker;
import com.td.recommend.core.validator.Validators;
import com.td.recommend.docstore.dao.DocItemDao;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.concurrent.ApplicationSharedExecutorService;
import com.td.recommend.video.recommender.*;
import com.td.recommend.video.rerank.predictor.FeedRerankPredictor;
import com.td.recommend.video.utils.ExtRelevantUtils;
import com.td.recommend.video.utils.TimeHelper;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.SupplierWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by admin on 2017/6/10.
 */
public class UserNewsRerankableRecommender {
    private static final Logger LOG = LoggerFactory.getLogger(BlendPolicy.class);
    private static final String METRICS_PREFIX = "uservideo";

    private static final long EXPIRE_DURATION_IN_MINUTES = 5;
    private static final long CACHE_MAX_SIZE = 100000;

    private static long timeOutInMs;
    //    private static final long timeoutInMsForPlugins = 100;
    private int slotNum = 32;
    private int ihf;
    private ExecutorService recommendExecutor;

    private static Cache<String, SlotRankResult> cache;

    private static TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();

    private VideoRecommenderContext recommendContext;

//    private List<RecommendSourcePlugin> recommendSourcePlugins;

    static {
        cache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_DURATION_IN_MINUTES, TimeUnit.MINUTES)
                .maximumSize(CACHE_MAX_SIZE).build();
    }

    public UserNewsRerankableRecommender(
            VideoRecommenderContext recommendContext) {

        this.recommendContext = recommendContext;
        recommendExecutor = ApplicationSharedExecutorService.getInstance().getExecutorService();
        timeOutInMs = ConfigFactory.load().getConfig("server").getInt("timeout");
        ihf = this.recommendContext.getRecommendRequest().getIhf();
    }

    public List<TaggedItem<PredictItem<DocItem>>> recommend(String queryId, int num) {
//        int retries = 0;
//
//
//        List<TaggedItem<PredictItem<DocItem>>> resultItems = Collections.emptyList();
//        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
//
//        while (true) {
//            try {
//                if (retries >= 2) {
//                    LOG.warn("QueryId={} refresh retries={}, blend result size={}, less than threshold={}",
//                            queryId, retries, resultItems.size(), num);
//                    break;
//                }
//
//
//                Timer.Context time = taggedMetricRegistry.timer("uservideo.recommend.olduser.latency").time();
//                resultItems = getForOldUser(queryId, num);
//                time.stop();
//
//                if (resultItems.size() < Math.min(num, slotNum)) {
//                    cache.invalidate(queryId);
//                } else {
//                    break;
//                }
//            } finally {
//                ++retries;
//            }
//        }
//
//
//        return resultItems;
        List<TaggedItem<PredictItem<DocItem>>> forOldUser = getForOldUser(queryId, num);
        recommendContext.setRerankModel("skip");
        if (forOldUser.isEmpty()
                || !Ihf.isFeed(ihf)
                || !recommendContext.getRecommendRequest().getAppId().equals("t01")) {
            return forOldUser;
        }

        Double click = 0D;
        Map<String, Map<String, TVariance>> userVarianceMap = UserProfileUtils.getVarianceMap(recommendContext.getUserItem());
        if (!userVarianceMap.isEmpty()) {
            Map<String, TVariance> vcat_ck = userVarianceMap.get("vcat_ck");
            if (vcat_ck != null && !vcat_ck.isEmpty()) {
                click = vcat_ck.values().stream().map(TVariance::getPosCnt).reduce(0D, Double::sum);
            }
        }

        if (click <= 10 && (recommendContext.getUserType() != UserProfileUtils.UserType.old_bare && recommendContext.getUserType() != UserProfileUtils.UserType.old_interest)) {
            return forOldUser;
        }
        //社区首页及相关不走rerank逻辑
        if(!Ihf.isTrend(ihf)){
            CompletableFuture<List<TaggedItem<PredictItem<DocItem>>>> completableFuture = CompletableFuture.supplyAsync(SupplierWrapper.of(() -> {
                FeedRerankPredictor feedRerankPredictor = new FeedRerankPredictor(recommendContext);
                List<TaggedItem<PredictItem<DocItem>>> rerankResult = feedRerankPredictor.predict(forOldUser);
                RatioRules ratioRules = RatioRules.getInstance();
                List<RatioRule> afterRerankRules = ratioRules.getAfterRerankRules(recommendContext);
                if (!afterRerankRules.isEmpty()) {
                    TaggedItemSlotReranker<PredictItem<DocItem>> secondTaggedItemSlotReranker = new SecondTaggedItemSlotReranker<>(afterRerankRules, METRICS_PREFIX);
                    rerankResult = secondTaggedItemSlotReranker.rank(rerankResult, 12, rerankResult.size());
                    Map<Integer, TaggedItem<PredictItem<DocItem>>> taggedItemMap = Maps.newTreeMap();
                    rerankResult.forEach(predictItemTaggedItem -> {
                        int curIndex = predictItemTaggedItem.getCurIndex();
                        PredictItem<DocItem> predictItem = predictItemTaggedItem.getItem();
                        DocItem docItem = predictItem.getItem();
                        if (docItem != null) {
                            Optional<ItemDocumentData> itemDocumentData = docItem.getNewsDocumentData();
                            if (itemDocumentData.isPresent()) {
                                Optional<VideoItem> videoItem = itemDocumentData.get().getStaticDocumentData();
                                if (videoItem.isPresent()) {
                                    String createTime = videoItem.get().getCreatetime();
                                    if (StringUtils.isNotBlank(createTime)) {
                                        int videoAge = TimeHelper.caculateTotalTime(TimeHelper.dateFormat(""), createTime);
                                        if (videoAge <= 7) {
                                            taggedItemMap.put(curIndex, predictItemTaggedItem);
                                        }
                                    }
                                }
                            }
                        }
                    });
                    taggedItemMap.values().forEach(rerankResult::remove);
                    taggedItemMap.forEach(rerankResult::add);
                }
                return rerankResult;
            }), recommendExecutor);

            try {
                List<TaggedItem<PredictItem<DocItem>>> rerankResult = completableFuture.get(100, TimeUnit.MILLISECONDS);
                taggedMetricRegistry.histogram("uservideo.recommend.rerank.timeout.rate").update(0);
                return rerankResult;
            } catch (InterruptedException | ExecutionException e) {
                taggedMetricRegistry.histogram("uservideo.recommend.rerank.timeout.rate").update(100);
                LOG.error("recommend for queryId={} ihf={}, failed", queryId, ihf, e);
            } catch (TimeoutException e) {
                taggedMetricRegistry.histogram("uservideo.recommend.rerank.timeout.rate").update(100);
                LOG.error("recommend for query={} with timeoutInMs={} timeout,ihf={}", queryId, 50, ihf, e);
            }
        }

        return forOldUser;
    }

    private List<TaggedItem<PredictItem<DocItem>>> getForOldUser(String queryId, int num) {

        SlotRankResult slotRankResult = getSlotRankResult(queryId);

        List<TaggedItem<PredictItem<DocItem>>> rankedItems = slotRankResult.getRankedItems();
        Validators<PredictItem<DocItem>> validators = new Validators<>();
//        没有缓存结果，这个可以不过滤
//        List<String> exposes = recommendContext.getExposes();
//        List<String> recentExpose = exposes.subList(0, Math.min(exposes.size(), 50));
        List<String> recentExpose = Collections.emptyList();
        validators.add(new ItemIdValidator<>(new HashSet<>(recentExpose)));

        Set<String> dedupSet = new HashSet<>();

        List<TaggedItem<PredictItem<DocItem>>> taggedItems = new ArrayList<>();

        if (recommendContext.getRecommendRequest().getIsFirstAccess().equals("1")
                && (Ihf.isFeed(ihf) || ihf == Ihf.VMIX_CHANNEL.id())) { //feed流、频道页，广告vid放到第一位

            Optional<IItem> teaserItem = recommendContext.getQueryItems().get(ItemKey.interest);
            if (teaserItem.isPresent()) {
                TaggedItem<PredictItem<DocItem>> teaserTaggedItem = new TaggedItem<>(new PredictItem<>());
                teaserTaggedItem.getItem().setRetrieveKeys(Collections.singletonList(new RetrieveKey().setKey("first_access").setType("teaser")));
                teaserTaggedItem.getItem().setItem((DocItem) teaserItem.get());
                taggedItems.add(0, teaserTaggedItem);
                dedupSet.add(teaserItem.get().getId());
            }
        }
//        else if(recommendContext.getRecommendRequest().getFp()==1 &&
//                ihf==Ihf.VSHOWDANCE_FEED.id() &&
//                StringUtils.isNotBlank(recommendContext.getRecommendRequest().getTrendUid())){ //第一次进去社区首页
//            String trendvid = recommendContext.getTrendVidStrings();
//            if(StringUtils.isNotBlank(trendvid)){
//                DocItem docItem = new DocItemDao().get(trendvid).get();
//                TaggedItem<PredictItem<DocItem>> teaserTaggedItem = new TaggedItem<>(new PredictItem<>());
//                teaserTaggedItem.getItem().setRetrieveKeys(Collections.singletonList(new RetrieveKey().setKey("first_access_showdance").setType("teaser_showdance")));
//                teaserTaggedItem.getItem().setItem(docItem);
//                //rankedItems.get(0).getItem().getPredictId();
//                if(rankedItems.size()>0){
//                    teaserTaggedItem.getItem().setPredictId(rankedItems.get(0).getItem().getPredictId());
//                }
//                taggedItems.add(0, teaserTaggedItem);
//                dedupSet.add(trendvid);
//                LOG.info("trend_recommend show_dance for first_access success: diu={}",
//                        this.recommendContext.getRecommendRequest().getDiu());
//            }
//            else{
//                LOG.error("trend_recommend show_dance for first_access error query={} ihf={} diu={} trenduid={} requestTrendUid={} ",
//                        queryId, ihf,
//                        this.recommendContext.getRecommendRequest().getDiu(),
//                        this.recommendContext.getTrendUid(),
//                        this.recommendContext.getRecommendRequest().getTrendUid());
//            }
//        }

        for (TaggedItem<PredictItem<DocItem>> rankedItem : rankedItems) {
            if (validators.valid(rankedItem.getItem()) && !dedupSet.contains(rankedItem.getItem().getId())) {

                taggedItems.add(rankedItem);
                dedupSet.add(rankedItem.getItem().getId());
                if (taggedItems.size() >= num) {
                    break;
                }
            }
        }

        return taggedItems;
    }

    private List<TaggedItem<PredictItem<DocItem>>> tag(List<PredictItem<DocItem>> predictItems, PredictItemTagger predictItemTagger, int slotNum) {
        List<TaggedItem<PredictItem<DocItem>>> taggedItems = new ArrayList<>();
        int count = 0;
        for (PredictItem<DocItem> predictItem : predictItems) {
            TaggedItem taggedItem = predictItemTagger.tag(predictItem);
            taggedItem.setCurIndex(count);
            taggedItem.setInitIndex(count);
            taggedItems.add(taggedItem);
            if (++count >= slotNum) {
                break;
            }
        }

        return taggedItems;
    }

    private SlotRankResult getSlotRankResult(String queryId) {
        if (!recommendContext.isDebug()
                && !Ihf.isRelevant(ihf)
                && recommendContext.hasBucket("feedcache-yes")) {

            SlotRankResult cachedResult = cache.getIfPresent(queryId);
            if (cachedResult != null) {
                return cachedResult;
            }
        }

        CompletableFuture<SlotRankResult> completableFuture = CompletableFuture.supplyAsync(SupplierWrapper.of(() -> {
            try {
                RecommenderBuilder<DocItem> recommenderBuilder;
                RatioRules ratioRules = RatioRules.getInstance();
                List<RatioRule> rules = Collections.emptyList();

                if (ExtRelevantUtils.isExtRelevant(recommendContext, ihf) && recommendContext.hasBucket("relevant_ext-exp")) {
                    recommenderBuilder = new ExtRelevantRecommenderBuilder(this.recommendContext);
                    rules = ratioRules.getRelevantRules(recommendContext);
                } else if (ExtRelevantUtils.isWxAppExtRelevant(recommendContext, ihf) && recommendContext.hasBucket("relevant_ext_wxapp-exp")) {
                    recommenderBuilder = new WxAppRelevantRecommenderBuilder(this.recommendContext);
                    rules = ratioRules.getRelevantRules(recommendContext);
                } else if (Ihf.isRelevant(ihf)) {
                    recommenderBuilder = new RelevantRecommenderBuilder(this.recommendContext);
                    rules = ratioRules.getRelevantRules(recommendContext);
                } else if (ihf == Ihf.VMIX_CHANNEL.id()) {
                    recommenderBuilder = new ChannelRecommenderBuilder(this.recommendContext);
                    slotNum = 100;
                    rules = ratioRules.getChannelRules(recommendContext);
                } else if (ihf == Ihf.VMIX_FOLLOW.id()) {
                    recommenderBuilder = new FollowRecommenderBuilder(this.recommendContext);
                    rules = ratioRules.getFollowRules(recommendContext);
                } else if (ihf == Ihf.VNOT_PLAY.id()) {
                    recommenderBuilder = new NotPlayRecommenderBuilder(this.recommendContext);
                } else {
                    recommenderBuilder = new VideoRecommenderBuilder(this.recommendContext);
                    rules = ratioRules.getFeedRules(recommendContext);
                    slotNum = 36;
                }

                Recommender<DocItem> recommender = recommenderBuilder.build();
                PredictItems<DocItem> predictItems = recommender.recommend();
//                if (recommendContext.hasBucket("feed_ev-exp")) {
//                    evSwap(predictItems);
//                }
//                DefaultSlotReranker<DocItem> slotReranker = new DefaultSlotReranker<>(new PredictItemTagger(recommendContext), rules, METRICS_PREFIX);
                DefaultPredictItemSlotReranker<DocItem> slotReranker = new DefaultPredictItemSlotReranker<>(new PredictItemTagger(recommendContext), rules, METRICS_PREFIX);

                long startRerankTime = System.currentTimeMillis();
                List<TaggedItem<PredictItem<DocItem>>> rankedItems = slotReranker.rank(predictItems.getItems(), slotNum, predictItems.getSize());
//                if (recommendContext.getRecommendRequest().getAppId().equals("t02")) {
//                    rankedItems = tag(predictItems.getItems(), new PredictItemTagger(recommendContext), slotNum);
//                } else {
//                    rankedItems = slotReranker.rank(predictItems.getItems(), slotNum, predictItems.getSize());
//                }

                LOG.info("rerank use time:{}", System.currentTimeMillis() - startRerankTime);
                List<TaggedItem<PredictItem<DocItem>>> taggedItems = rankedItems;
                if (rankedItems.size() > slotNum) {
                    taggedItems = new ArrayList<>(rankedItems.subList(0, slotNum));//jvm GC: subList contains parent, new ArrayList avoid cache all, original will be gc
                }

                addTaggedItemsMetrics(taggedItems);

                SlotRankResult slotRankResult = new SlotRankResult(taggedItems);

//                PredictItemsSimplify.simplify(predictItems);


                if (!recommendContext.isDebug()
                        && !Ihf.isRelevant(ihf)
                        && slotRankResult.getRankedItems().size() >= slotNum
                        && recommendContext.hasBucket("feedcache-yes")) {
                    cache.put(queryId, slotRankResult);
                }
                recommendContext.setModel(predictItems.getModelName());

                return slotRankResult;
            } catch (Exception e) {
                LOG.error("recommend with queryId={} failed!", queryId, e);
                return SlotRankResult.empty();
            }
        }), recommendExecutor);


        try {
            SlotRankResult slotRankResult = completableFuture.get(timeOutInMs, TimeUnit.MILLISECONDS);
            taggedMetricRegistry.histogram("uservideo.recommendraw.timeout.rate").update(0);
            return slotRankResult;
        } catch (InterruptedException | ExecutionException e) {
            taggedMetricRegistry.histogram("uservideo.recommendraw.timeout.rate").update(100);
            LOG.error("recommend for queryId={} ihf={}, failed", queryId, ihf, e);
        } catch (TimeoutException e) {
            taggedMetricRegistry.histogram("uservideo.recommendraw.timeout.rate").update(100);
            LOG.error("recommend for query={} with timeoutInMs={} timeout,ihf={}", queryId, timeOutInMs, ihf);
        }

        return SlotRankResult.empty();
    }

    private void addTaggedItemsMetrics(List<TaggedItem<PredictItem<DocItem>>> taggedItems) {
        Map<String, MutableInt> categoryCountMap = new HashMap<>();

        int totalCount = 0;
        for (TaggedItem<PredictItem<DocItem>> taggedItem : taggedItems) {
            PredictItem<DocItem> predictItem = taggedItem.getItem();
            Set<String> allCategories = DocProfileUtils.getAllCategories(predictItem.getItem());
            for (String category : allCategories) {
                MutableInt counter = categoryCountMap.get(category);
                if (counter == null) {
                    counter = new MutableInt(0);
                    categoryCountMap.put(category, counter);
                }
                counter.increment();
                ++totalCount;
            }
        }

        double entropy = 0.0;

        for (Map.Entry<String, MutableInt> entry : categoryCountMap.entrySet()) {
            int count = entry.getValue().intValue();
//            taggedMetricRegistry.histogram("uservideo.taggedItems." + entry.getKey() + ".count").update(count);
            double prob = (count * 1.0 / totalCount);
            entropy += -prob * (Math.log(prob) / Math.log(2));
        }

        int perplexity = (int) Math.pow(2, entropy);
        taggedMetricRegistry.histogram("uservideo.taggedItems.perplexity").update(perplexity);
        taggedMetricRegistry.histogram("uservideo.taggedItems.catnum").update(categoryCountMap.size());
    }

    private void evSwap(PredictItems<DocItem> predictItems) {
        ImmutableSet<String> subcats = ImmutableSet.of("1007", "1009", "1010", "1034", "1094", "1053");
        int target = -1, candidate = -1;
        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            String secondCat = DocProfileUtils.getSecondCat(items.get(i).getItem());
            boolean hasEv = items.get(i).getRetrieveKeys().stream().anyMatch(k -> k.getType().endsWith("_ev"));
            if (target < 0 && !subcats.contains(secondCat) && hasEv) {
                target = i;
            }
            if (candidate < 0 && subcats.contains(secondCat) && hasEv) {
                candidate = i;
            }
            if (target == -1 && candidate >= 0) {
                break;
            }
            if (target >= 0 && candidate > 0) {
                PredictItem<DocItem> tmp = items.get(target);
                items.set(target, items.get(candidate));
                items.set(candidate, tmp);
                double tmpScore = items.get(target).getScore();
                items.get(target).setScore(items.get(candidate).getScore());
                items.get(candidate).setScore(tmpScore);
                break;
            }
        }
    }


    public static void main(String[] args) {
        double a = Integer.MIN_VALUE;
        System.out.println(Integer.MIN_VALUE == a);
    }
}
