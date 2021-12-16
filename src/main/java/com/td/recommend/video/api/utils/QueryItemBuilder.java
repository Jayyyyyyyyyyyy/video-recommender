package com.td.recommend.video.api.utils;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.Maps;
import com.td.data.profile.TUserItem;
import com.td.featurestore.datasource.ItemDAO;
import com.td.featurestore.datasource.ItemDataSource;
import com.td.featurestore.feature.Feature;
import com.td.featurestore.feature.IFeatures;
import com.td.featurestore.feature.KeySortedFeatures;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.Item;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.utils.TalentUids;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/12/26.
 */
public class QueryItemBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(QueryItemBuilder.class);

    public static final QueryItemBuilder INSTANCE = new QueryItemBuilder();

    public Items build(String userId, RecommendRequest recommendRequest, Optional<TUserItem> userItemOpt) {
        long startTime = System.currentTimeMillis();
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        String appId = recommendRequest.getAppId();
        Timer.Context time = taggedMetricRegistry.timer(appId + ".uservideo.buildQueryItems.latency").time();

        UserItem userItem;
        if (userItemOpt.isPresent()) {
            userItem = buildUserItem(userId, userItemOpt.get());
        } else {
            userItem = buildUserItem(userId, recommendRequest);
        }

        Items items = new Items();
        userItem.addTag("userId", userId);
        items.add(ItemKey.user, userItem);

        String relevantVid = recommendRequest.getVid();//相关推荐vid
        if (StringUtils.isNotBlank(relevantVid)) {
            DocItem docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(relevantVid).orElse(new DocItem(relevantVid));
            items.add(ItemKey.doc, docItem);
        }
        String teaserVid = recommendRequest.getSecondCatVid();//渠道素材vid
        if (StringUtils.isNotBlank(teaserVid)) {
            DocItem docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(teaserVid).orElse(new DocItem(teaserVid));
            items.add(ItemKey.interest, docItem);
        }

        Item contextItem = new Item(userId);

        String os = recommendRequest.getOs();
        if (!StringUtils.isEmpty(os)) {
            IFeatures features = new KeySortedFeatures("os");
            features.add(new Feature(os, 1.0));
            contextItem.addFeatures("os", features);
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        String hourSegment = hourSegment(localDateTime.getHour());
        IFeatures hourSegFeatures = new KeySortedFeatures("hourseg");
        hourSegFeatures.add(new Feature(hourSegment, 1.0));
        contextItem.addFeatures("hourseg", hourSegFeatures);


        IFeatures hourFeatures = new KeySortedFeatures("hour");
        hourFeatures.add(new Feature(String.valueOf(localDateTime.getHour()), 1.0));
        contextItem.add("hour", hourFeatures);

        IFeatures dayOfWeekFeatures = new KeySortedFeatures("dayOfWeek");
        dayOfWeekFeatures.add(new Feature(String.valueOf(localDateTime.getDayOfWeek()), 1.0));
        contextItem.add("dayOfWeek", dayOfWeekFeatures);

        if (recommendRequest.getDevice() != null) {
            IFeatures deviceNameFeatures = new KeySortedFeatures("device");
            deviceNameFeatures.add(new Feature(recommendRequest.getDevice(), 1.0));
            contextItem.add("device", deviceNameFeatures);
        }
        Map<String, String> contextMap = Maps.newHashMap();
        if (StringUtils.isNotBlank(recommendRequest.getProvince())) {
            try {
                contextMap.put("province", URLDecoder.decode(recommendRequest.getProvince(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOG.error("province decode error", e);
            }
        }
        if (StringUtils.isNotBlank(recommendRequest.getCity())) {
            try {
                contextMap.put("city", URLDecoder.decode(recommendRequest.getCity(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOG.error("city decode error", e);
            }
        }
        if (StringUtils.isNotBlank(recommendRequest.getManufacture())) {
            contextMap.put("manufacture", recommendRequest.getManufacture());
        }
        if (StringUtils.isNotBlank(recommendRequest.getNetType())) {
            contextMap.put("nettype", recommendRequest.getNetType());
        }
        if (StringUtils.isNotBlank(recommendRequest.getModel())) {
            contextMap.put("model", recommendRequest.getModel());
        }
        if (StringUtils.isNotBlank(recommendRequest.getDevice())) {
            contextMap.put("device", recommendRequest.getDevice());
        }
        if (recommendRequest.getScreenHeight() > 0) {
            contextMap.put("height", String.valueOf(recommendRequest.getScreenHeight()));
        }
        if (recommendRequest.getScreenWidth() > 0) {
            contextMap.put("width", String.valueOf(recommendRequest.getScreenWidth()));
        }
        //获取用户的cluster
        String clusterRes = getTalentCluster(userItem, recommendRequest.getUid());
        if(StringUtils.isNotBlank(clusterRes)){
            contextMap.put("talentcluster", clusterRes);
        }
        long timestamp = System.currentTimeMillis();
        contextMap.put("timestamp", String.valueOf(timestamp));
        contextItem.setTags(contextMap);
        items.add(ItemKey.context, contextItem);
        LOG.info("build query item latency={}", System.currentTimeMillis() - startTime);
        time.stop();

        return items;
    }

    private String  getTalentCluster(UserItem userItem, String uid){

        List<String> xfollowUids = new ArrayList<>(UserProfileUtils.getValueFeaturesMap(userItem, "xfollow").keySet());

        List<String> st_uid_cs = new ArrayList<>(UserProfileUtils.getVarianceFeatureMap(userItem, "st_vauthor_uid_cs").keySet());

        List<String> uid_cs = new ArrayList<>(UserProfileUtils.getVarianceFeatureMap(userItem, "vauthor_uid_cs").keySet());

        List<String> st_uid_ck = UserProfileUtils.getVarianceFeatureMap(userItem, "st_vauthor_uid_ck").entrySet().stream().
                filter(entry -> entry.getValue().getMean()>0.0 && entry.getValue().getVariance()<0.9).
                map(Map.Entry::getKey).collect(Collectors.toList());

        List<String> uid_ck = UserProfileUtils.getVarianceFeatureMap(userItem, "vauthor_uid_ck").entrySet().stream().
                filter(entry -> entry.getValue().getMean()>0.0 && entry.getValue().getVariance()<0.9 ).
                map(Map.Entry::getKey).collect(Collectors.toList());

        Map<String, List<String>> key2Uids = new LinkedHashMap<>();
        key2Uids.put("xfollow", xfollowUids);
        key2Uids.put("st_vauthor_uid_cs", st_uid_cs);
        key2Uids.put("vauthor_uid_cs", uid_cs);
        key2Uids.put("st_vauthor_uid_ck", st_uid_ck);
        key2Uids.put("vauthor_uid_ck", uid_ck);

        Map<String, Set<String>> my_cluster = new HashMap<>();

        boolean hasCluster = false;
        for(Map.Entry<String,List<String>> entry: key2Uids.entrySet()){
            if(!hasCluster){
                hasCluster = createCluster(entry.getValue(), my_cluster, 3);
            }
        }
        return my_cluster.entrySet().stream().
                map(entry -> entry.getKey() + ":" + entry.getValue().size()).
                collect(Collectors.joining(","));
    }

    public static boolean createCluster(List<String> intputUids,
                                        Map<String, Set<String>> clusterMap,
                                        int clusterLimit){
        intputUids.stream().
                filter(uid -> TalentUids.getTalent2Cluster_v2().containsKey(uid)).
                forEach( csuid -> {
                    String cluster = TalentUids.getTalent2Cluster_v2().getOrDefault(csuid, "");
                    if(StringUtils.isNotBlank(cluster)){
                        clusterMap.computeIfAbsent(cluster,k -> new HashSet<>()).add(csuid);
                    }
                });
        return clusterMap.entrySet().stream().anyMatch(entry -> entry.getValue().size()>=clusterLimit);
    }

    private String hourSegment(int hour) {
        int hourSegment = -1;

        if (hour >= 0 && hour < 6) {
            hourSegment = 0;
        } else if (hour >= 6 && hour < 9) {
            hourSegment = 1;
        } else if (hour >= 9 && hour < 11) {
            hourSegment = 2;
        } else if (hour >= 11 && hour < 13) {
            hourSegment = 3;
        } else if (hour >= 13 && hour < 15) {
            hourSegment = 4;
        } else if (hour >= 15 && hour < 17) {
            hourSegment = 5;
        } else if (hour >= 17 && hour < 19) {
            hourSegment = 6;
        } else if (hour >= 19 && hour < 21) {
            hourSegment = 7;
        } else if (hour >= 21 && hour < 24) {
            hourSegment = 8;
        }
        return String.valueOf(hourSegment);
    }

    private UserItem buildUserItem(String userId, TUserItem userItem) {
        return new UserItem(userId, userItem);
    }

    private UserItem buildUserItem(String userId, RecommendRequest recommendRequest) {
        ItemDataSource<DocItem> dataSource;

        dataSource = UserVideoItemDataSource.getInstance();

        Map<String, ItemDAO<? extends IItem>> queryDAOs = dataSource.getQueryDAOs();

        UserItemDao itemDAO = (UserItemDao) queryDAOs.get(ItemKey.user.name());


        Optional<UserItem> userItemOpt = Optional.empty();
        int ihf = recommendRequest.getIhf();

        if (recommendRequest.getAppId().equals("t02")) {
            Set<String> buckets = RecommendContextBuilderHelper.buildBuckets(userId, recommendRequest);
            if (buckets.contains("wxapp_long_profile-yes")) {
                userItemOpt = itemDAO.getWxApp(userId);
            } else {
                userItemOpt = itemDAO.get(userId);
            }
        } else if (ihf == Ihf.VBIG_RLVT.id() || ihf == Ihf.VSMALL_RLVT.id()) {
            userItemOpt = itemDAO.newGet(userId);
        } else if (ihf == Ihf.VMIX_FOLLOW.id()) {
            userItemOpt = itemDAO.getUserFollow(recommendRequest.getUid());//需要登录，用uid
        } else {
            userItemOpt = itemDAO.get(userId);
        }

        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Histogram histogram = taggedMetricRegistry.histogram(recommendRequest.getAppId() + ".uservideo.userprofile.emptyrate");

        UserItem userItem = null;
        if (userItemOpt.isPresent()) {
            histogram.update(0);
            userItem = userItemOpt.get();
        } else {
            histogram.update(100);
            userItem = new UserItem(userId);
            LOG.error("Get profile with userId={} failed!", userId);
        }

        return userItem;
    }
}
