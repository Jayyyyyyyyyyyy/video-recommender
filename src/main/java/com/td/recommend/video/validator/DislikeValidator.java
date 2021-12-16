package com.td.recommend.video.validator;

import com.td.data.profile.item.ItemDocumentData;
import com.td.data.profile.item.VideoItem;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.core.validator.IValidator;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Created by admin on 2017/7/6.
 */
public class DislikeValidator implements IValidator<DocItem> {
    private Set<String> expDislikeTags;
    private Set<String> expDislikeSources;
    private Map<String, ImplicitDislikeItem> ipDislikeTags;
    private Map<String, ImplicitDislikeItem> ipDislikeSources;

    public DislikeValidator(VideoRecommenderContext recommendContext) {
        UserItem userItem = recommendContext.getUserItem();
        expDislikeTags = UserProfileUtils.getValueFeaturesMap(userItem, "ep_dlk_tags").keySet();
        expDislikeSources = UserProfileUtils.getValueFeaturesMap(userItem, "ep_dlk_sources").keySet();
        ipDislikeTags = getIpDislikeTags(userItem);
        ipDislikeSources = getIpDislikeSources(userItem);
    }


    @Override
    public boolean valid(DocItem docItem) {
        Optional<ItemDocumentData> docDataOpt = docItem.getNewsDocumentData();

        if (!docDataOpt.isPresent()) {
            return true;
        }

        ItemDocumentData ItemDocumentData = docDataOpt.get();
        Optional<VideoItem> staticDocDataOpt = ItemDocumentData.getStaticDocumentData();
        if (!staticDocDataOpt.isPresent()) {
            return true;
        }

        VideoItem staticDocumentData = staticDocDataOpt.get();
        if (isExplicitDislike(staticDocumentData)) {
            return false;
        }

        if (isImplicitDislike(staticDocumentData)) {
            return false;
        }

        return true;
    }


    private boolean isExplicitDislike(VideoItem staticDocumentData) {
//        Map<String, Double> tagsMap = staticDocumentData.getTagsMap();
//
//        Set<Map.Entry<String, Double>> entries = tagsMap.entrySet();
//        for (Map.Entry<String, Double> entry : entries) {
//            if(expDislikeTags.contains(entry.getKey())) {
//                return true;
//            }
//        }
//
//
//        String source = staticDocumentData.getFrom();
//        if (source != null && expDislikeSources.contains(source)) {
//            return true;
//        }

        return false;
    }

    private boolean isImplicitDislike(VideoItem staticDocumentData) {
//        Map<String, Double> tagsMap = staticDocumentData.getTagsMap();
//        long currentTimeMillis = System.currentTimeMillis();
//
//        for (Map.Entry<String, Double> entry : tagsMap.entrySet()) {
//            ImplicitDislikeItem dlkItem = ipDislikeTags.get(entry.getKey());
//            if (dlkItem == null) {
//                continue;
//            }
//
//            long timeInMills = currentTimeMillis - dlkItem.lastDlkTime;
//
//            if (dlkItem.dlkCount < 2 && timeInMills < ONE_DAY_IN_MILLS) {
//                return true;
//            } else if (dlkItem.dlkCount < 3 && timeInMills < 3 * ONE_DAY_IN_MILLS) {
//                return true;
//            } else if (dlkItem.dlkCount < 5 && timeInMills < 30 * ONE_DAY_IN_MILLS) {
//                return  true;
//            } else if (dlkItem.dlkCount >= 5) {
//                return true;
//            }
//        }
//
//        String from = staticDocumentData.getFrom();
//        ImplicitDislikeItem dlkItem = ipDislikeSources.get(from);
//        if (dlkItem != null) {
//            long timeInMills = currentTimeMillis - dlkItem.lastDlkTime;
//            if (dlkItem.dlkCount > 3 && timeInMills < 5 * ONE_DAY_IN_MILLS) {
//                return true;
//            } else if (dlkItem.dlkCount > 5 && timeInMills < 30 * ONE_DAY_IN_MILLS) {
//                return true;
//            } else if (dlkItem.dlkCount > 8){
//                return true;
//            }
//        }
        return false;
    }


    private static Map<String, ImplicitDislikeItem> getIpDislikeSources(UserItem userItem) {
        return getIpDislikeMap(userItem, "ip_dlk_sources");
    }

    private static Map<String, ImplicitDislikeItem> getIpDislikeTags(UserItem userItem) {
        return getIpDislikeMap(userItem, "ip_dlk_tags");
    }


    private static Map<String, ImplicitDislikeItem> getIpDislikeMap(UserItem userItem, String facetName) {
        Map<String, String> dlkSourceTimeMap = UserProfileUtils.getTagFeatureMap(userItem, facetName);
        Map<String, Double> dlkSourceValueMap = UserProfileUtils.getValueFeaturesMap(userItem, facetName);

        Map<String, ImplicitDislikeItem> ipDislikeSourcesMap = new HashMap<>();

        for (Map.Entry<String, String> entry : dlkSourceTimeMap.entrySet()) {
            long time = Long.parseLong(entry.getValue());
            Double count = dlkSourceValueMap.get(entry.getKey());
            if (count == null) {
                continue;
            }
            ImplicitDislikeItem implicitDislikeItem = new ImplicitDislikeItem();
            implicitDislikeItem.dlkCount = count;
            implicitDislikeItem.lastDlkTime = time;
            ipDislikeSourcesMap.put(entry.getKey(), implicitDislikeItem);
        }

        return ipDislikeSourcesMap;
    }

    static class ImplicitDislikeItem {
        double dlkCount;
        long lastDlkTime;
    }

    public static void main(String[] args) {
        UserItemDao userItemDao = UserVideoItemDataSource.getInstance().getUserItemDao();
        Optional<UserItem> userItemOpt = userItemDao.get("25d69205418bc5f8");
        if (userItemOpt.isPresent()) {
            Map<String, ImplicitDislikeItem> dlkSourceMap = getIpDislikeTags(userItemOpt.get());
            for (Map.Entry<String, ImplicitDislikeItem> entry : dlkSourceMap.entrySet()) {
                ImplicitDislikeItem value = entry.getValue();
                System.out.printf("%s\t%d\t%f\n", entry.getKey(), value.lastDlkTime, value.dlkCount);
            }
        }
    }

}
