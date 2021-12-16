package com.td.recommend.video.api.servlet;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.td.data.profile.item.ItemDocumentData;
import com.td.data.profile.item.VideoItem;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.Item;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.FilterItem;
import com.td.recommend.commons.idgenerator.TokenIdGenerator;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.poi.PlaceInfo;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.request.RequestParamHelper;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.RecentEvents;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.api.utils.QueryItemBuilder;
import com.td.recommend.video.api.utils.RecommendContextBuilderHelper;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.history.*;
import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.RedisClientSingleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/12/8.
 */
public class VideoRecommendContextBuilder {
    private static VideoRecommendContextBuilder instance = new VideoRecommendContextBuilder();

    private static final String baseversion = "6.8.6.121622";
    private static JedisPool jedisPool = new JedisPool("10.10.205.57");
    private static final Set<Integer> trendTypeSet = ImmutableSet.of(501,502, 503);

    private static final Logger LOG = LoggerFactory.getLogger(VideoRecommendContextBuilder.class);

    public static VideoRecommendContextBuilder getInstance() {
        return instance;
    }

    public VideoRecommenderContext build(HttpServletRequest request) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Timer.Context buildTime = taggedMetricRegistry.timer("uservideo.requestbuild.latency").time();

        RecommendRequest recommendRequest = new RecommendRequest(request);
        String diu = recommendRequest.getDiu();
        String uid = recommendRequest.getUid();
        String vid = recommendRequest.getVid();
        int ihf = recommendRequest.getIhf();
        String appId = recommendRequest.getAppId();
        List<String> exposeList;

        Set<String> buckets = RecommendContextBuilderHelper.buildBuckets(diu, recommendRequest);
        if (Ihf.isRelevant(ihf)) {
            exposeList = RelevantExposeHistory.getInstance().getList(appId, diu);
        } else if (ihf == Ihf.VMIX_FOLLOW.id()) {
            exposeList = FollowExposeHistory.getInstance().getList(appId, uid);//关注流，需要登录，用uid
        } else if (ihf == Ihf.VSHOWDANCE_FEED.id()){
            exposeList = ExposeHistory.getInstance().getDays(appId, diu);//社区首页
        } else {
            exposeList = ExposeHistory.getInstance().getExposes(appId, diu);
        }
        List<String> oneDayExposes = ExposeHistory.getInstance().getOneDay(appId, diu);


        Set<String> used = new HashSet<>(exposeList);

        Items queryItems = QueryItemBuilder.INSTANCE.build(diu, recommendRequest, Optional.empty());
        boolean isDebug = RequestParamHelper.getBool(request, "debug", false);

        VideoRecommenderContext recommendContext = new VideoRecommenderContext(diu, used, buckets, queryItems, isDebug);
        recommendContext.setRecommendRequest(recommendRequest);
        String version = recommendRequest.getVersion();
        if (version.compareTo(baseversion) > 0) {
            String token = TokenIdGenerator.getInstance().generateNew("");
            recommendContext.setToken(token);
        }

        if (recommendRequest.getAppId().equals("t02")) {
            if ((ihf == Ihf.VBIG_RLVT.id() || ihf == Ihf.VSMALL_RLVT.id()) && buckets.contains("relevant_ext_wxapp-exp")) {
                recommendContext.setClicks(ClickHistory.getInstance().wxappClicked(diu));
                recommendContext.setPlayed(Collections.EMPTY_LIST);
            } else if (ihf == Ihf.VBIG_FEED.id() && buckets.contains("wxapp_implicit_recall-yes")) {
                recommendContext.setClicks(ClickHistory.getInstance().wxappClicked(diu));
                recommendContext.setPlayed(Collections.EMPTY_LIST);
            } else {
                recommendContext.setClicks(ClickHistory.getInstance().clicked(diu));
                recommendContext.setPlayed(parseClickedDurationHistory(diu));
            }
        } else {
            recommendContext.setClicks(ClickHistory.getInstance().clicked(diu));
            recommendContext.setPlayed(parseClickedDurationHistory(diu));
        }

        recommendContext.setTrendUserType(trendUsertypeByHistory(ClickHistory.getInstance().showDanceFeedClicked(diu)));

        recommendContext.setSwipes(extractSwipes(diu));
        recommendContext.setClickSeq(extractSeq(ClickHistory.getInstance().clickSeq(diu)));
        recommendContext.setDownloadSeq(extractSeq(DownloadHistory.getInstance().downloadSeq(diu)));
        recommendContext.setFavSeq(extractSeq(FavHistory.getInstance().favSeq(diu)));
        recommendContext.setPlaySeq(extractSeq(PlayHistory.getInstance().playSeq(diu)));
        recommendContext.setExposes(exposeList);
        recommendContext.setFav(FavHistory.getInstance().faved(diu, 20));
        recommendContext.setDownLoad(DownloadHistory.getInstance().downloaded(diu, 20));
        recommendContext.setOneDayExposes(oneDayExposes);
        recommendContext.setKeyWords(parseSearchHistory(diu));
        UserItem userItem = (UserItem) queryItems.get(ItemKey.user).get();
        Optional<IItem> teaserItem = queryItems.get(ItemKey.interest);//广告素材vid画像
        Optional<IItem> contextItem = queryItems.get(ItemKey.context);
        contextItem.ifPresent(iItem -> recommendContext.setTalentClusterLine(iItem.getTags().getOrDefault("talentcluster", "")));
        String activeCat = interestFirstCatByHistory(ClickHistory.getInstance().clicked(diu));//最近点击历史判定兴趣
        if (StringUtils.isBlank(activeCat)) {
            activeCat = teaserItem.map(teaser -> DocProfileUtils.getFirstCat((DocItem) teaser)).orElse("");//素材兴趣
        }
        recommendContext.setActiveCat(activeCat);
        recommendContext.setUserType(UserProfileUtils.getUserType(userItem, activeCat));//step1
        recommendContext.setUserInterest(UserProfileUtils.getUserInterest(userItem, activeCat));//step2

        long activeTime;
        try (Jedis redis = jedisPool.getResource()) {
            activeTime = Long.parseLong(redis.get("regitster_time_" + recommendContext.getRecommendRequest().getDiu()));
        } catch (Exception e) {
            activeTime = 0;
        }
        recommendContext.setActiveTime(activeTime);
        userItem.addTag("activeTime", String.valueOf(activeTime));

        Optional<PlaceInfo> placeInfo = RecommendContextBuilderHelper.buildPlaceInfo(recommendRequest);
        recommendContext.setPlaceInfo(placeInfo);

        Optional<RecentEvents> recentEvents = RecommendContextBuilderHelper.buildRecentEvents(diu);
        recommendContext.setRecentEvents(recentEvents);
        if (StringUtils.isNotBlank(vid)) {
            recommendContext.setRlvtdyns(extractRlvtdyns(queryItems));
        }
        RecommendContextBuilderHelper.buildRecentItem(queryItems, recentEvents);
        buildTime.stop();

        return recommendContext;
    }

    private UserProfileUtils.TrendUserType trendUsertypeByHistory(List<String> showDanceClickHistory) {
        List<String> clicks = showDanceClickHistory.subList(0, Math.min(5, showDanceClickHistory.size()));
        if (clicks.size() >= 5) {
            List<Optional<DocItem>> items = UserVideoItemDataSource.getInstance().getCandidateDAO().parallelGet(clicks);
            long total = items.stream().filter(item ->{
                return item.filter(docItem -> trendTypeSet.contains(DocProfileUtils.getCtype(docItem))).isPresent();
            }).count();

            if (total >= 3) {
                return UserProfileUtils.TrendUserType.trend_strong;
            }
            else{
                return UserProfileUtils.TrendUserType.trend_weak;
            }
        }
        else{
            return UserProfileUtils.TrendUserType.trend_none;
        }
    }

    private List<DynamicDumpInfo.SeqItem> extractSeq(List<FilterItem> filterItems) {
        List<DynamicDumpInfo.SeqItem> seqItems = filterItems.subList(0, Math.min(50, filterItems.size())).stream().map(i -> {
            DynamicDumpInfo.SeqItem seqItem = new DynamicDumpInfo.SeqItem();
            seqItem.setVid(i.getVid());
            seqItem.setMod(i.getMod());
            seqItem.setPt(i.getDur() == null ? 0 : i.getDur().longValue());
            seqItem.setTime(i.getTime().longValue());
            return seqItem;
        }).collect(Collectors.toList());
        List<String> vids = seqItems.stream().map(DynamicDumpInfo.SeqItem::getVid).distinct().collect(Collectors.toList());
        List<Optional<DocItem>> optionals = UserVideoItemDataSource.getInstance().getCandidateDAO().parallelGet(vids);
        Map<String, DocItem> docItemMap = optionals.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toMap(Item::getId, i -> i));
        seqItems.forEach(i -> {
            DocItem docItem = docItemMap.get(i.getVid());
            i.setVcat(DocProfileUtils.getFirstCat(docItem));
            i.setVsubcat(DocProfileUtils.getSecondCat(docItem));
            i.setVauthor(DocProfileUtils.getUid(docItem));
            i.setVmp3(DocProfileUtils.getMp3(docItem));
            i.setVtag(DocProfileUtils.getTags(docItem));
        });
        return seqItems;
    }

    private static Map<String, Map<String, Map<String, Number>>> extractRlvtdyns(Items queryItems) {
        List<String> keys = extractVid(queryItems);
        List<String> rlvtdyns = RedisClientSingleton.rlvtdyns.mget(Convert.toStrArray(keys));
        Map<String, Map<String, Map<String, Number>>> dynsMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String[] split = StrUtil.split(key, "-");
            String type = split[1];
            String feat = rlvtdyns.get(i);
            if (StringUtils.isNotBlank(feat)) {
                dynsMap.put(type, JSON.parseObject(feat, Map.class));
            }
        }
        return dynsMap;
    }

    private static List<String> extractVid(Items queryItems) {
        List<String> rlvtdynsRedisKey = new ArrayList<>();
        DocItem docItem = (DocItem)queryItems.get(ItemKey.doc).get();
        Optional<ItemDocumentData> newsDocumentData = docItem.getNewsDocumentData();
        if (newsDocumentData.isPresent()) {
            ItemDocumentData itemDocumentData = newsDocumentData.get();
            Optional<VideoItem> staticDocumentData = itemDocumentData.getStaticDocumentData();
            if (staticDocumentData != null && staticDocumentData.isPresent()) {
                VideoItem videoItem = staticDocumentData.get();
                String vid = videoItem.getId();
                if (StringUtils.isNotBlank(vid)) {
                    rlvtdynsRedisKey.add("rlvtdyns2-vid-" + vid);
                }
                int uid = videoItem.getUid();
                if (uid > 0) {
                    rlvtdynsRedisKey.add("rlvtdyns2-author-" + uid);
                }
                String video_mp3_name = videoItem.getVideo_mp3_name();
                if (StringUtils.isNotBlank(video_mp3_name)) {
                    rlvtdynsRedisKey.add("rlvtdyns2-mp3-" + SecureUtil.md5(video_mp3_name));
                }
            }
        }
        return rlvtdynsRedisKey;
    }

    private static List<DynamicDumpInfo.WordTime> parseSearchHistory(String diu) {
        try {
            String queryHistory = RedisClientSingleton.query.get("sh:" + diu);
            if (queryHistory != null && queryHistory.length() > 0) {
                List<DynamicDumpInfo.WordTime> queryList = JSONObject.parseArray(queryHistory, DynamicDumpInfo.WordTime.class);
                List<DynamicDumpInfo.WordTime> topQuery;
                if (queryList.size() >= 10) {
                    topQuery = queryList.subList(0, 10);
                } else {
                    topQuery = queryList;
                }
                return topQuery;
            }
        } catch (Exception e) {
            LOG.error("parse search query failed");
        }
        return Collections.EMPTY_LIST;
    }

    private static List<DynamicDumpInfo.VidDuration> parseClickedDurationHistory(String userId) {
        try {
            List<ImmutablePair<String, Double>> list = PlayHistory.getInstance().playDuration(userId);
            if (list != null && list.size() > 0) {
                List<DynamicDumpInfo.VidDuration> historyList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    DynamicDumpInfo.VidDuration vidDuration = new DynamicDumpInfo.VidDuration();
                    vidDuration.setId(list.get(i).left);
                    vidDuration.setPt(list.get(i).right);
                    historyList.add(vidDuration);
                }
                return historyList;
            }
        } catch (Exception e) {
            LOG.error("parse click duration history failed");
        }
        return Collections.EMPTY_LIST;
    }

    private List<DynamicDumpInfo.Swipe> extractSwipes(String diu) {
        List<DynamicDumpInfo.Swipe> swipeList = Collections.emptyList();
        String swipes = RedisClientSingleton.swipe.get(diu);
        if (StringUtils.isNotBlank(swipes)) {
            swipeList = JSON.parseArray(swipes, DynamicDumpInfo.Swipe.class);
            List<String> vids = swipeList.stream().flatMap(i -> i.getVideos().stream())
                    .map(DynamicDumpInfo.SwipeItem::getVid)
                    .distinct()
                    .collect(Collectors.toList());
            List<Optional<DocItem>> optionals = UserVideoItemDataSource.getInstance().getCandidateDAO().parallelGet(vids);
            Map<String, DocItem> docItemMap = optionals.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toMap(Item::getId, i -> i));
            swipeList.forEach(swipe -> swipe.getVideos().forEach(i -> {
                DocItem docItem = docItemMap.get(i.getVid());
                i.setVcat(DocProfileUtils.getFirstCat(docItem));
                i.setVsubcat(DocProfileUtils.getSecondCat(docItem));
                i.setVauthor(DocProfileUtils.getUid(docItem));
                i.setVmp3(DocProfileUtils.getMp3(docItem));
            }));
        }
        return swipeList;
    }
    String interestFirstCatByHistory(List<String> clickHistory) {
        List<String> clicks = clickHistory.subList(0, Math.min(8, clickHistory.size()));
        if (clicks.size() > 3) {
            HashMap<String, Integer> catCount = new HashMap<>();
            float total = 0;
            List<Optional<DocItem>> items = UserVideoItemDataSource.getInstance().getCandidateDAO().parallelGet(clicks);
            for (Optional<DocItem> item : items) {
                if (item.isPresent()) {
                    catCount.merge(DocProfileUtils.getFirstCat(item.get()), 1, Integer::sum);
                    total += 1;
                }
            }
            if (total > 3) {
                Optional<Map.Entry<String, Integer>> maxInterest = catCount.entrySet().stream().max(Comparator.comparing(Map.Entry<String, Integer>::getValue));
                if (maxInterest.isPresent()) {
                    if (maxInterest.get().getValue() / total >= 0.75) {
                        return maxInterest.get().getKey();
                    }
                }
            }
        }
        return "";
    }
}
