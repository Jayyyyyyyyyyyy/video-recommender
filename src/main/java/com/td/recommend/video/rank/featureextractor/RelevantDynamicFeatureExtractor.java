package com.td.recommend.video.rank.featureextractor;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.td.data.profile.TL2Entry;
import com.td.data.profile.TVariance;
import com.td.data.profile.item.ItemDocumentData;
import com.td.data.profile.item.VideoItem;
import com.td.featurestore.feature.IFeature;
import com.td.featurestore.feature.IFeatures;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.response.Tag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.api.vo.NewsDocBuilder;
import com.td.recommend.video.api.vo.VideoStaticFeatureBuilder;
import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import com.td.recommend.video.rank.featuredumper.bean.RelevantDynamicDumpInfo;
import com.td.recommend.video.rank.featuredumper.bean.VideoStaticFeature;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.FastDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Created by zjl on 2019/8/20.
 */
public class RelevantDynamicFeatureExtractor{
    private static final Logger LOG = LoggerFactory.getLogger(RelevantDynamicFeatureExtractor.class);
    private static final RelevantDynamicFeatureExtractor instance = new RelevantDynamicFeatureExtractor();

    public static RelevantDynamicFeatureExtractor getInstance() {
        return instance;
    }

    private static final String[] SHORT_TERM_USER_FEATURES = new String[]{
            "st_vcat_cs", "st_vcat_ck",
//            "st_vxcat_cs", "st_vxcat_ck",
            "st_vsubcat_cs", "st_vsubcat_ck",
//            "st_vxsubcat_cs", "st_vxsubcat_ck",
            "st_vtag_cs", "st_vtag_ck",
//            "st_vxtag_cs", "st_vxtag_ck",
//            "st_vdance_cs", "st_vdance_ck",
//            "st_vlen_cs", "st_vlen_ck",
//            "st_vheadt_cs", "st_vheadt_ck",
//            "st_vdifficulty_cs", "st_vdifficulty_ck",
//            "st_vteach_cs", "st_vteach_ck",
            "st_vauthor_uid_cs", "st_vauthor_uid_ck",
//            "st_vauthor_type_cs", "st_vauthor_type_ck",
//            "st_vauthor_level_cs", "st_vauthor_level_ck",
//            "st_vteam_cs", "st_vteam_ck",
//            "st_vteamrole_cs", "st_vteamrole_ck",
//            "st_vteacher_cs", "st_vteacher_ck",
//            "st_vgenre_cs", "st_vgenre_ck",
            "st_vmp3_cs", "st_vmp3_ck",
//            "st_vtitle_len_cs", "st_vtitle_len_ck",
//            "st_vauthor_in_title_cs", "st_vauthor_in_title_ck",
//            "st_vmusic_in_title_cs", "st_vmusic_in_title_ck",
//            "st_vdance_in_title_cs", "st_vdance_in_title_ck",
//            "st_vfactor_cs", "st_vfactor_ck",
//            "st_vexercise_body_ck", "st_vexercise_body_cs"
    };

    private static final String[] DOC_FEATURES = new String[]{
//            "stats",
            "dy_1day",
            "dy_15day",
            "dy_7day",
//            "classify_mp3",
//            "classify_uid",
//            "stat_feed",
            "dy_feed_1day",
            "dy_feed_7day",
            "dy_feed_15day",
//            "stat_rl",
            "dy_rl_1day",
            "dy_rl_7day",
            "dy_rl_15day",
    };
    private static final String[] SEARCH_FEATURES = new String[]{
            "sevmp3",
            "sevteacher"
    };

    public RelevantDynamicDumpInfo extract(PredictItem<DocItem> predictItem, Items queryItems, Integer pos) {
        RelevantDynamicDumpInfo dumpInfo = new RelevantDynamicDumpInfo();
        UserItem userItem = UserProfileUtils.getUserItem(queryItems);
        if (0 == pos) {
            extractCurrentItemProfile(queryItems, dumpInfo);
            extractShortTermUserFeatures(userItem, dumpInfo);
            extractXFollowFeature(userItem, dumpInfo);
            extractSearchFeature(userItem, dumpInfo);

        }
        extractCurrentDocFeatures(queryItems, dumpInfo);
        extractDocFeatures(predictItem, dumpInfo);//视频动态特征
        extractDocRetrieveFeature(predictItem, dumpInfo);
        extractItemProfile(predictItem, dumpInfo);
        extractVideoTags(predictItem, dumpInfo);
        return dumpInfo;
    }
    private void extractItemProfile(PredictItem<DocItem> predictItem, RelevantDynamicDumpInfo dynamicDumpInfo) {
        Optional<VideoStaticFeature> itemProfile = new VideoStaticFeatureBuilder().getInstance().build(predictItem.getItem());
        if (itemProfile.isPresent()) {
            VideoStaticFeature videoStaticFeature = itemProfile.get();
            dynamicDumpInfo.setItemProfile(videoStaticFeature);
        }
    }
    private void extractCurrentItemProfile(Items queryItems, RelevantDynamicDumpInfo dynamicDumpInfo) {
        DocItem docItem = (DocItem)queryItems.get(ItemKey.doc).get();
        Optional<VideoStaticFeature> itemProfile = new VideoStaticFeatureBuilder().getInstance().build(docItem);
        if (itemProfile.isPresent()) {
            VideoStaticFeature videoStaticFeature = itemProfile.get();
            dynamicDumpInfo.setCurrentItemProfile(videoStaticFeature);
        }
    }
    private void extractVideoTags(PredictItem<DocItem> predictItem, RelevantDynamicDumpInfo dumpInfo) {
        dumpInfo.setUiTags(Collections.EMPTY_LIST);
        Optional<ItemDocumentData> itemDocumentData = predictItem.getItem().getNewsDocumentData();
        if (itemDocumentData.isPresent()) {
            ItemDocumentData ItemDocumentData = itemDocumentData.get();
            Optional<VideoItem> staticDocumentDataOpt = ItemDocumentData.getStaticDocumentData();
            if (staticDocumentDataOpt.isPresent()) {
                List<Tag> tags = NewsDocBuilder.getInstance().buildTags(predictItem, null);
                dumpInfo.setUiTags(tags);
            }
        }
    }

    private void extractSearchFeature(UserItem userItem, RelevantDynamicDumpInfo dumpInfo) {
        dumpInfo.setSearchTeacher(Collections.EMPTY_LIST);
        dumpInfo.setSearchMp3(Collections.EMPTY_LIST);
        try {
            TL2Entry searchTeacherEntry = userItem.getUserRawData().get()
                    .getFeaturesMap().get("sevteacher");
            if(searchTeacherEntry.getKeySize() > 0 && searchTeacherEntry.getKeySize() == searchTeacherEntry.getValueSize()){
                List<DynamicDumpInfo.KeySearchTime> sevteacherList = new ArrayList<>();
                for (int i = 0; i < searchTeacherEntry.getKeySize(); i++) {
                    sevteacherList.add(new DynamicDumpInfo.KeySearchTime(searchTeacherEntry.getKey().get(i),searchTeacherEntry.getValue().get(i).longValue()));
                }
                dumpInfo.setSearchTeacher(sevteacherList);
            }
        }catch (Exception e){
        }
        try {
            TL2Entry sevmp3Entry = userItem.getUserRawData().get()
                    .getFeaturesMap().get("sevmp3");
            if(sevmp3Entry.getKeySize() > 0 && sevmp3Entry.getKeySize() == sevmp3Entry.getValueSize()){
                List<DynamicDumpInfo.KeySearchTime> sevmp3List = new ArrayList<>();
                for (int i = 0; i < sevmp3Entry.getKeySize(); i++) {
                    sevmp3List.add(new DynamicDumpInfo.KeySearchTime(sevmp3Entry.getKey().get(i),sevmp3Entry.getValue().get(i).longValue()));
                }
                dumpInfo.setSearchMp3(sevmp3List);
            }
        }catch (Exception e){
        }

    }

    private void extractDocFeatures(PredictItem<DocItem> predictItem, RelevantDynamicDumpInfo dumpInfo) {
        DocItem docItem = predictItem.getItem();
        Map<String, Map<String, Double>> dynamicDocFeatures = new HashMap<>();
        for (String docFeature : DOC_FEATURES) {
            Map<String, Double> dynamicDocFeature = new HashMap<>();
            Optional<IFeatures> featuresOpt = docItem.getFeatures(docFeature);
            if (featuresOpt.isPresent()) {
                IFeatures features = featuresOpt.get();
                for (IFeature feature : features) {
                    dynamicDocFeature.put(feature.getName(), FastDouble.round(feature.getValue(), 4));
                }
                dynamicDocFeatures.put(docFeature, dynamicDocFeature);
            }
        }
        dumpInfo.setDynamicDocFeatures(dynamicDocFeatures);

    }
    private void extractCurrentDocFeatures(Items queryItems, RelevantDynamicDumpInfo dumpInfo) {
        DocItem docItem = (DocItem)queryItems.get(ItemKey.doc).get();
        dumpInfo.setCurrentDocId(docItem.getId());
    }

    private void extractShortTermUserFeatures(UserItem userItem, RelevantDynamicDumpInfo dumpInfo) {
        Map<String, Map<String, DynamicDumpInfo.UserStaticInfo>> stUserFeatures = new HashMap<>();
        Map<String, Map<String, TVariance>> varianceMaps = UserProfileUtils.getVarianceMap(userItem);
        for (String userFeature : SHORT_TERM_USER_FEATURES) {
            if (varianceMaps.containsKey(userFeature)) {
                Map<String, TVariance> vMap = varianceMaps.get(userFeature);
                Map<String, DynamicDumpInfo.UserStaticInfo> userStaticInfoMaps = new HashMap<>();
                for (Map.Entry<String, TVariance> entry : vMap.entrySet()) {
                    TVariance tVariance = entry.getValue();
                    DynamicDumpInfo.UserStaticInfo userStaticInfo =
                            new DynamicDumpInfo.UserStaticInfo(
                                    FastDouble.round(tVariance.getMean(), 4),
                                    FastDouble.round(tVariance.getPosCnt(), 4),
                                    FastDouble.round(tVariance.getNegCnt(), 4),
                                    FastDouble.round(tVariance.getVariance(), 4)
                            );
                    userStaticInfoMaps.put(entry.getKey(), userStaticInfo);
                }
                stUserFeatures.put(userFeature, userStaticInfoMaps);
            }
        }
        dumpInfo.setStUserFeatures(stUserFeatures);
    }

    private void extractXFollowFeature(UserItem userItem, RelevantDynamicDumpInfo dumpInfo) {
        Map<String, Double> followUsers = UserProfileUtils.getValueFeaturesMap(userItem, "xfollow");
        Map<String, Double> xfollow = new HashMap<>();
        for (Map.Entry<String, Double> userFollow : followUsers.entrySet()) {
            xfollow.put(userFollow.getKey(), FastDouble.round(userFollow.getValue(), 4));
        }
        dumpInfo.setXFollowFeatures(xfollow);
    }

    private void extractDocRetrieveFeature(PredictItem<DocItem> predictItem, RelevantDynamicDumpInfo dumpInfo) {
        List<RetrieveKey> retrieveKeys = predictItem.getRetrieveKeys();
        DocItem item = predictItem.getItem();
        Map<String, Double> features = new HashMap<>();
        for (RetrieveKey retrieveKey : retrieveKeys) {
            features.put("d_retrieve_" + retrieveKey.getType(), 1.0);
            features.put("d_retrieve_" + retrieveKey.getType() + "_" + retrieveKey.getKey(), retrieveKey.getScore());
            for (String tag : retrieveKey.getTags()) {
                features.put("d_retrieve_" + retrieveKey.getType() + "_" + tag, 1.0);
            }
        }
        extractDocRetrieveStatFeature(item, retrieveKeys, "retrieveView", "d_rsview", features);
        extractDocRetrieveStatFeature(item, retrieveKeys, "retrieveClick", "d_rsclk", features);
        extractDocRetrieveStatFeature(item, retrieveKeys, "retrieveCtr", "d_rsctr", features);
        dumpInfo.setRetrieveFeatures(features);
    }


    private void extractDocRetrieveStatFeature(DocItem item, List<RetrieveKey> retrieveKeys, String facet,
                                               String namespace, Map<String, Double> features) {
        double min = Double.MAX_VALUE;
        double max = Double.NEGATIVE_INFINITY;
        String maxType = null;
        String minType = null;

        double avg = 0.0;

        int count = 0;

        List<Double> matchedFeatureValues = new ArrayList<>();
        for (RetrieveKey retrieveKey : retrieveKeys) {
            String statKey = retrieveKey.getType() + "_" + retrieveKey.getKey();
            Optional<IFeatures> featuresOpt = item.getFeatures(facet);
            if (!featuresOpt.isPresent()) {
                continue;
            }

            IFeatures iFeatures = featuresOpt.get();
            Optional<IFeature> iFeatureOpt = iFeatures.get(statKey);
            if (iFeatureOpt.isPresent()) {
                IFeature feature = iFeatureOpt.get();
                double value = feature.getValue();
                matchedFeatureValues.add(value);
                if (min > value) {
                    min = value;
                    minType = retrieveKey.getType();
                }

                if (max < value) {
                    max = value;
                    maxType = retrieveKey.getType();
                }

                avg += value;
                ++count;
            }
        }


        if (count != 0) {
            avg /= count;

            features.put(namespace + "max", max);
            features.put(namespace + "min", min);
            features.put(namespace + "avg", avg);
            if (maxType != null) {
                features.put(namespace + "max" + maxType, 1.0);
            }
            if (minType != null) {
                features.put(namespace + "min" + minType, 1.0);
            }

            double std = 0;
            if (count > 1) {
                for (Double value : matchedFeatureValues) {
                    std += (value - avg) * (value - avg);
                }
                std = Math.sqrt(1.0 / count * std);
            }

            features.put(namespace + "std", FastDouble.round(std, 4));
        }

    }
    public void addRelevantDynamicDumpInfo(List<RelevantDynamicDumpInfo> relevantDynamicDumpInfos,
                                           PredictItem<DocItem> predictItem,
                                           UserItem userItem,
                                           Optional<DynamicDumpInfo.SimUserDoc> simUserDoc,
                                   Integer pos,
                                   VideoRecommenderContext recommendContext) {
        Items queryItems = recommendContext.getQueryItems();
        Map<String, Map<String, Map<String, Number>>> rlvtdyns = recommendContext.getRlvtdyns();
        RelevantDynamicDumpInfo relevantDynamicDumpInfo = extract(predictItem, queryItems, pos);
        relevantDynamicDumpInfo.setDumpUser(0);
        if (0 == pos) {
            String vprofile_info = "";
            try {
                vprofile_info = userItem.getUserRawData().get()
                        .getFeaturesMap()
                        .get("vprofile_info")
                        .getSvalue()
                        .get(0);
            } catch (Exception ignore) {
            }

            relevantDynamicDumpInfo.setVprofile_info(vprofile_info);

            List<String> clickedList = recommendContext.getClicks();
            if (clickedList.size() > 49) {
                relevantDynamicDumpInfo.setClicked(clickedList.subList(0, 50));
            } else {
                relevantDynamicDumpInfo.setClicked(clickedList);
            }
            relevantDynamicDumpInfo.setFav(recommendContext.getFav());
            relevantDynamicDumpInfo.setDownload(recommendContext.getDownLoad());
            List<DynamicDumpInfo.WordTime> keyWords = recommendContext.getKeyWords();
            relevantDynamicDumpInfo.setSearchQuery(classTypeTransfer(keyWords));
            relevantDynamicDumpInfo.setFeedSwipes(recommendContext.getSwipes());
            relevantDynamicDumpInfo.setClickSeq(recommendContext.getClickSeq());
            relevantDynamicDumpInfo.setDownloadSeq(recommendContext.getDownloadSeq());
            relevantDynamicDumpInfo.setFavSeq(recommendContext.getFavSeq());
            relevantDynamicDumpInfo.setPlaySeq(recommendContext.getPlaySeq());
            relevantDynamicDumpInfo.setDumpUser(1);
        }
        relevantDynamicDumpInfo.setUserId(userItem.getId());
        relevantDynamicDumpInfo.setPredictId(predictItem.getPredictId());
        relevantDynamicDumpInfo.setTimestamp(System.currentTimeMillis());
        relevantDynamicDumpInfo.setRankPos(pos);
        relevantDynamicDumpInfo.setDocId(predictItem.getItem().getId());
        relevantDynamicDumpInfo.setPredictScore(predictItem.getPredictScore());
        JSONObject probeDyns = extractProbeDyns(predictItem.getItem(), rlvtdyns);
        if (probeDyns != null) {
            relevantDynamicDumpInfo.setProbeDyns(probeDyns);
        }
        relevantDynamicDumpInfos.add(relevantDynamicDumpInfo);
    }

    public static JSONObject extractProbeDyns(DocItem item, Map<String, Map<String, Map<String, Number>>> rlvtdyns) {
        if (rlvtdyns == null || rlvtdyns.isEmpty()) {
            return null;
        }
        String vid = item.getId();
        JSONObject probeDyns = new JSONObject();
        rlvtdyns.forEach((type, value) -> {
            if (value != null) {
                Map<String, Number> dynsItem = value.get("vid_" + vid);
                probeDyns.put(type + "-vid", dynsItem);
            }
        });
        return probeDyns;
    }

    public List<DynamicDumpInfo.KeySearchTime> classTypeTransfer(List<DynamicDumpInfo.WordTime> list) {
        List<DynamicDumpInfo.KeySearchTime> newList = new ArrayList<>();
        if (null == list || list.isEmpty()) {
            return newList;
        }
        for (int i = 0; i < list.size(); i++) {
            DynamicDumpInfo.WordTime wt = list.get(i);
            DynamicDumpInfo.KeySearchTime keySearchTime = new DynamicDumpInfo.KeySearchTime(wt.getW(), wt.getT());
            newList.add(keySearchTime);
        }
        return newList;
    }
}
