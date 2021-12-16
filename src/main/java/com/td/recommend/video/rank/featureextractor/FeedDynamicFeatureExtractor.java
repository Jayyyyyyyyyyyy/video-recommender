package com.td.recommend.video.rank.featureextractor;

import com.td.data.profile.TDynamicDocDataNew;
import com.td.data.profile.TL2Entry;
import com.td.data.profile.TVariance;
import com.td.data.profile.item.ItemDocumentData;
import com.td.data.profile.user.TDocItem;
import com.td.featurestore.feature.IFeature;
import com.td.featurestore.feature.IFeatures;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.api.vo.NewsDocBuilder;
import com.td.recommend.video.api.vo.VideoStaticFeatureBuilder;
import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import com.td.recommend.video.rank.featuredumper.bean.VideoStaticFeature;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.FastDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by zjl on 2019/8/20.
 */
public class FeedDynamicFeatureExtractor {
    private static final FeedDynamicFeatureExtractor instance = new FeedDynamicFeatureExtractor();
    private static final Logger LOG = LoggerFactory.getLogger(FeedDynamicFeatureExtractor.class);

    public static FeedDynamicFeatureExtractor getInstance() {
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
            "st_vdifficulty_cs", "st_vdifficulty_ck",
            "st_vteach_cs", "st_vteach_ck",
            "st_vauthor_uid_cs", "st_vauthor_uid_ck",
//            "st_vauthor_type_cs", "st_vauthor_type_ck",
//            "st_vauthor_level_cs", "st_vauthor_level_ck",
//            "st_vteam_cs", "st_vteam_ck",
//            "st_vteamrole_cs", "st_vteamrole_ck",
            "st_vteacher_cs", "st_vteacher_ck",
            "st_vgenre_cs", "st_vgenre_ck",
            "st_vmp3_cs", "st_vmp3_ck",
//            "st_vtitle_len_cs", "st_vtitle_len_ck",
//            "st_vauthor_in_title_cs", "st_vauthor_in_title_ck",
//            "st_vmusic_in_title_cs", "st_vmusic_in_title_ck",
//            "st_vdance_in_title_cs", "st_vdance_in_title_ck",
//            "st_vfactor_cs", "st_vfactor_ck",
            "st_vexercise_body_ck", "st_vexercise_body_cs"
    };

    private static final String[] DOC_FEATURES = new String[]{
//            "stats",
            "dy_1day",
//            "dy_15day",
            "dy_7day",
//            "classify_mp3",
//            "classify_uid",
//            "stats_feed",
            "dy_feed_1day",
            "dy_feed_7day",
//            "dy_feed_15day",
//            "stats_rl",
//            "dy_rl_1day",
//            "dy_rl_7day",
//            "dy_rl_15day",
    };
    private static final String[] SEARCH_FEATURES = new String[]{
            "sevmp3",
            "sevteacher"
    };

    public DynamicDumpInfo extract(PredictItem<DocItem> predictItem, Items queryItems, Integer pos) {
        DynamicDumpInfo dynamicDumpInfo = new DynamicDumpInfo();
        UserItem userItem = UserProfileUtils.getUserItem(queryItems);
        try {
            if (0 == pos) {
                extractShortTermUserFeatures(userItem, dynamicDumpInfo);
                extractXFollowFesture(userItem, dynamicDumpInfo);
                extractSearchFeature(userItem, dynamicDumpInfo);
            }
            extractDocFeatures(predictItem, dynamicDumpInfo);//视频动态特征
            extractDocRetrieveFeature(predictItem, dynamicDumpInfo);
            extractItemProfile(predictItem, dynamicDumpInfo);
        } catch (Exception e) {
            LOG.error("Extract DynamicDumpInfo Exception", e);
        }
        return dynamicDumpInfo;
    }

    private void extractItemProfile(PredictItem<DocItem> predictItem, DynamicDumpInfo dynamicDumpInfo) {
        Optional<VideoStaticFeature> itemProfile = new VideoStaticFeatureBuilder().getInstance().build(predictItem.getItem());
        if (itemProfile.isPresent()) {
            VideoStaticFeature videoStaticFeature = itemProfile.get();
            dynamicDumpInfo.setItemProfile(videoStaticFeature);
        }
    }

    private void extractSearchFeature(UserItem userItem, DynamicDumpInfo dynamicDumpInfo) {
        dynamicDumpInfo.setSearchTeacher(Collections.EMPTY_LIST);
        dynamicDumpInfo.setSearchMp3(Collections.EMPTY_LIST);
        try {
            TL2Entry searchTeacherEntry = userItem.getUserRawData().get()
                    .getFeaturesMap().get("sevteacher");
            if (searchTeacherEntry.getKeySize() > 0 && searchTeacherEntry.getKeySize() == searchTeacherEntry.getValueSize()) {
                List<DynamicDumpInfo.KeySearchTime> sevteacherList = new ArrayList<>();
                for (int i = 0; i < searchTeacherEntry.getKeySize(); i++) {
                    sevteacherList.add(new DynamicDumpInfo.KeySearchTime(searchTeacherEntry.getKey().get(i), searchTeacherEntry.getValue().get(i).longValue()));
                }
                dynamicDumpInfo.setSearchTeacher(sevteacherList);
            }
        } catch (Exception e) {
        }
        try {
            TL2Entry sevmp3Entry = userItem.getUserRawData().get()
                    .getFeaturesMap().get("sevmp3");
            if (sevmp3Entry.getKeySize() > 0 && sevmp3Entry.getKeySize() == sevmp3Entry.getValueSize()) {
                List<DynamicDumpInfo.KeySearchTime> sevmp3List = new ArrayList<>();
                for (int i = 0; i < sevmp3Entry.getKeySize(); i++) {
                    sevmp3List.add(new DynamicDumpInfo.KeySearchTime(sevmp3Entry.getKey().get(i), sevmp3Entry.getValue().get(i).longValue()));
                }
                dynamicDumpInfo.setSearchMp3(sevmp3List);
            }
        } catch (Exception e) {
        }

    }

    private void extractDocFeatures(PredictItem<DocItem> predictItem, DynamicDumpInfo dynamicDumpInfo) {
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
        dynamicDumpInfo.setDynamicDocFeatures(dynamicDocFeatures);
//        if (docItem.getNewsDocumentData().isPresent()) {
//            ItemDocumentData itemDocumentData = docItem.getNewsDocumentData().get();
//            Optional<TDynamicDocDataNew> dynamicDocumentDataNew = itemDocumentData.getDynamicDocumentDataNew();
//            if (dynamicDocumentDataNew.isPresent()) {
//                TDynamicDocDataNew tDynamicDocDataNew = dynamicDocumentDataNew.get();
//                try {
//                    Map<String, com.td.data.profile.TDocItem> callCtrM = tDynamicDocDataNew.getRawMap().get("recom").callCtrMap;
//                    Map<String, Map<String, Double>> callCtrFeatures = new HashMap<>();
//                    for (Map.Entry entry : callCtrM.entrySet()) {
//                        String featureName = entry.getKey().toString();
//                        if (featureName.endsWith("*")) {
//                            Map<String, Double> callCtrFeature = new HashMap<>();
//                            com.td.data.profile.TDocItem value = (com.td.data.profile.TDocItem) entry.getValue();
//                            callCtrFeature.put("ctr", FastDouble.round(value.ctr, 4));
//                            callCtrFeature.put("click", FastDouble.round(value.click, 4));
//                            callCtrFeature.put("view", FastDouble.round(value.view, 4));
//                            callCtrFeatures.put(featureName, callCtrFeature);
//                        }
//                    }
//                    dynamicDumpInfo.setCallCtrFeatures(callCtrFeatures);
//                } catch (Exception e) {
//                    LOG.info("extract doc dynamic feature failed id:{}", docItem.getId());
//                }
//            }
//        }

    }

    private void extractShortTermUserFeatures(UserItem userItem, DynamicDumpInfo dynamicDumpInfo) {
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
        dynamicDumpInfo.setStUserFeatures(stUserFeatures);
    }

    private void extractXFollowFesture(UserItem userItem, DynamicDumpInfo dynamicDumpInfo) {
        Map<String, Double> followUsers = UserProfileUtils.getValueFeaturesMap(userItem, "xfollow");
        Map<String, Double> xfollow = new HashMap<>();
        for (Map.Entry<String, Double> userFollow : followUsers.entrySet()) {
            xfollow.put(userFollow.getKey(), FastDouble.round(userFollow.getValue(), 4));
        }
        dynamicDumpInfo.setXFollowFeatures(xfollow);
    }

    private void extractDocRetrieveFeature(PredictItem<DocItem> predictItem, DynamicDumpInfo dynamicDumpInfo) {
        List<RetrieveKey> retrieveKeys = predictItem.getRetrieveKeys();
        DocItem item = predictItem.getItem();
        Map<String, Double> features = new HashMap<>();
        for (RetrieveKey retrieveKey : retrieveKeys) {
            features.put("d_retrieve_" + retrieveKey.getType(), 1.0);
            //score is the pos in this retrieve queue
            features.put("d_retrieve_" + retrieveKey.getType() + "_" + retrieveKey.getKey(), retrieveKey.getScore());
            for (String tag : retrieveKey.getTags()) {
                features.put("d_retrieve_" + retrieveKey.getType() + "_" + tag, 1.0);
            }
        }
        extractDocRetrieveStatFeature(item, retrieveKeys, "retrieveView", "d_rsview", features);
        extractDocRetrieveStatFeature(item, retrieveKeys, "retrieveClick", "d_rsclk", features);
        extractDocRetrieveStatFeature(item, retrieveKeys, "retrieveCtr", "d_rsctr", features);
        dynamicDumpInfo.setRetrieveFeatures(features);
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

    public void addDynamicDumpInfo(List<DynamicDumpInfo> dynamicDumpInfos,
                                   PredictItem<DocItem> predictItem,
                                   UserItem userItem,
                                   Optional<DynamicDumpInfo.SimUserDoc> simUserDoc,
                                   Integer pos,
                                   VideoRecommenderContext recommendContext) {
        Items queryItems = recommendContext.getQueryItems();
        DynamicDumpInfo dynamicDumpInfo = extract(predictItem, queryItems, pos);
        String vprofile_info = "";

        try {
            vprofile_info = userItem.getUserRawData().get()
                    .getFeaturesMap()
                    .get("vprofile_info")
                    .getSvalue()
                    .get(0);
        } catch (Exception ignore) {
        }
        dynamicDumpInfo.setDumpUser(0);
        if (0 == pos) {
            dynamicDumpInfo.setVprofile_info(vprofile_info);
            dynamicDumpInfo.setActiveTime(recommendContext.getActiveTime());
            dynamicDumpInfo.setActiveCat(recommendContext.getActiveCat());
            dynamicDumpInfo.setUserInterest(recommendContext.getUserInterest());
            List<String> clickedList = recommendContext.getClicks();
            if (clickedList.size() > 50) {
                dynamicDumpInfo.setClicked(clickedList.subList(0, 49));
            } else {
                dynamicDumpInfo.setClicked(clickedList);
            }
            List<DynamicDumpInfo.WordTime> keyWords = recommendContext.getKeyWords();
            dynamicDumpInfo.setSearchQuery(classTypeTransfer(keyWords));
            dynamicDumpInfo.setFav(recommendContext.getFav());
            dynamicDumpInfo.setFavSeq(recommendContext.getFavSeq());
            dynamicDumpInfo.setDownload(recommendContext.getDownLoad());
            dynamicDumpInfo.setDownloadSeq(recommendContext.getDownloadSeq());
            List<DynamicDumpInfo.VidDuration> played = recommendContext.getPlayed();
            dynamicDumpInfo.setPlayed(played.subList(0, Math.min(30, played.size())));
            dynamicDumpInfo.setSwipes(recommendContext.getSwipes());
            dynamicDumpInfo.setClickSeq(recommendContext.getClickSeq());
            dynamicDumpInfo.setPlaySeq(recommendContext.getPlaySeq());
            dynamicDumpInfo.setDumpUser(1);
        }
        dynamicDumpInfo.setStartid(recommendContext.getRecommendRequest().getStartid());
        dynamicDumpInfo.setPredictId(predictItem.getPredictId());
        dynamicDumpInfo.setDocId(predictItem.getItem().getId());
        dynamicDumpInfo.setUserId(userItem.getId());
        dynamicDumpInfo.setPredictScore(predictItem.getPredictScore());
        dynamicDumpInfo.setModelScore(predictItem.getModelScore());
        dynamicDumpInfo.setTimestamp(System.currentTimeMillis());
        dynamicDumpInfo.setUiTags(NewsDocBuilder.getInstance().buildTags(predictItem, recommendContext));
        dynamicDumpInfo.setRankPos(pos);
        dynamicDumpInfos.add(dynamicDumpInfo);
    }

    public List<DynamicDumpInfo.VideoInfo> transformTDocItem(List<TDocItem> tDocItems) {
        List<DynamicDumpInfo.VideoInfo> videoInfos = new ArrayList<>();
        for (TDocItem docItem : tDocItems) {
            if (docItem != null && docItem.getVid().trim().length() > 0) {
                String tag = docItem.getTag();
                List<String> tagList = new ArrayList<>();
                if (tag.length() > 0) {
                    String[] tagArr = tag.split(",");
                    tagList = Arrays.asList(tagArr);
                }
                int rate = -1;
                int playTime = -1;
                try {
                    rate = Integer.valueOf(docItem.getRate());
                } catch (Exception e) {

                }
                try {
                    playTime = Integer.valueOf(docItem.getPlaytime());
                } catch (Exception e) {

                }
                DynamicDumpInfo.VideoInfo videoInfo = new DynamicDumpInfo.VideoInfo(docItem.getVid(), docItem.getCat(), docItem.getSubcat(), tagList,
                        docItem.getAuthor(), docItem.getMp3(), rate, playTime, docItem.getModules(), docItem.getDateStr());
                videoInfos.add(videoInfo);
            }
        }
        return videoInfos;
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
