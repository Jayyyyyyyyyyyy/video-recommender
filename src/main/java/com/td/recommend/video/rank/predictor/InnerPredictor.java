package com.td.recommend.video.rank.predictor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.td.data.profile.TVariance;
import com.td.featurestore.item.Items;
import com.td.rank.deepfm.felib.InteractionHistory;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import scala.collection.JavaConverters;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/8/3.
 */
public interface InnerPredictor {
    Optional<PredictResult> predict(PredictItems<DocItem> predictItems, Items queryItems, String predictId);

    default String dateFormat(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }

    default Map<String, TVariance> filterFeat(Map<String, TVariance> variance, String key) {
        if (variance != null && variance.size() > 0) {
            return variance.entrySet().stream().filter(x -> {
                TVariance variance1 = x.getValue();
                if (key.equals("cs")) {
                    return variance1.posCnt >= 3;
                }
                if (key.equals("ck")) {
                    return variance1.negCnt >= 3;
                }
                return false;
            }).collect(Collectors.toMap(
                    (e) -> (String) e.getKey(),
                    (e) -> e.getValue()));
        }
        return variance;
    }

    default double uclk(Map<String, TVariance> variance) {
        if (variance == null || variance.isEmpty()) {
            return 0.0;
        }
        return variance.values().stream().mapToDouble(TVariance::getPosCnt).sum();
    }

    default double uctr(Map<String, TVariance> variance) {
        if (variance == null || variance.isEmpty()) {
            return 0.0;
        }
        double click = 1.0;
        double display = 10.0;
        for (TVariance tVariance : variance.values()) {
            click += tVariance.getPosCnt();
            display += tVariance.getNegCnt();
        }
        return click / display;
    }

    default double udays(double activeTime) {
        if (activeTime <= 0) {
            return 0.0;
        }
        double now = System.currentTimeMillis();
        double diff = now - activeTime;
        double mill = 1000 * 60 * 60 * 24;
        return diff / mill;
    }

    default void swipes(JSONObject featureJson, VideoRecommenderContext recommendContext, Integer limit) {
        List<DynamicDumpInfo.Swipe> swipes = recommendContext.getSwipes();
        String startId = recommendContext.getRecommendRequest().getStartid();
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(swipes));
        com.td.rank.deepfm.felib.Sessions sessions = new com.td.rank.deepfm.felib.Sessions();
        sessions.load(jsonArray, startId, dateFormat("yyyy-MM-dd"));
        sessions.accFeatures(featureJson);

        InteractionHistory clickSeq = new InteractionHistory();
        List<DynamicDumpInfo.SeqItem> clickSeqData = recommendContext.getClickSeq();
        JSONArray clickSeqJson = JSON.parseArray(JSON.toJSONString(clickSeqData));
        clickSeq.load(clickSeqJson, 50);
        scala.collection.mutable.Map<String, Object> clickSeqLatestTimestamp = clickSeq.latestTimestamp();
        featureJson.put("clickSeqLatestTimestamp", JavaConverters.mapAsJavaMap(clickSeqLatestTimestamp));

        InteractionHistory playSeq = new InteractionHistory();
        List<DynamicDumpInfo.SeqItem> playSeqData = recommendContext.getPlaySeq();
        JSONArray playSeqJson = JSON.parseArray(JSON.toJSONString(playSeqData));
        playSeq.load(playSeqJson, 50);
        scala.collection.mutable.Map<String, Object> playSeqLatestTimestamp = playSeq.latestTimestamp();
        featureJson.put("playSeqLatestTimestamp", JavaConverters.mapAsJavaMap(playSeqLatestTimestamp));

        InteractionHistory downloadSeq = new InteractionHistory();
        List<DynamicDumpInfo.SeqItem> downloadSeqData = recommendContext.getDownloadSeq();
        JSONArray downloadSeqJson = JSON.parseArray(JSON.toJSONString(downloadSeqData));
        downloadSeq.load(downloadSeqJson, 50);
        scala.collection.mutable.Map<String, Object> downloadSeqLatestTimestamp = downloadSeq.latestTimestamp();
        featureJson.put("downloadSeqLatestTimestamp", JavaConverters.mapAsJavaMap(downloadSeqLatestTimestamp));

        InteractionHistory favSeq = new InteractionHistory();
        List<DynamicDumpInfo.SeqItem> favSeqData = recommendContext.getFavSeq();
        JSONArray favSeqJson = JSON.parseArray(JSON.toJSONString(favSeqData));
        favSeq.load(favSeqJson, 50);
        scala.collection.mutable.Map<String, Object> favSeqLatestTimestamp = favSeq.latestTimestamp();
        featureJson.put("favSeqLatestTimestamp", JavaConverters.mapAsJavaMap(favSeqLatestTimestamp));

        clickSeq.accSeqFeatures("click", 30, new int[]{1}, -1, featureJson);
        downloadSeq.accSeqFeatures("download", 15, new int[]{1}, -1, featureJson);
        favSeq.accSeqFeatures("fav", 15, new int[]{1}, -1, featureJson);
        playSeq.accPlayTimeFeatures(30, new int[]{1}, featureJson);
    }

    @Getter
    @Setter
    class SessionVideo {
        private String vid;
        private String author;
        private String mp3;
        private String cat;
        private String subcat;
        private Boolean impression;
        private Boolean click;

        public SessionVideo(DynamicDumpInfo.SwipeItem swipeItem) {
            vid = swipeItem.getVid();
            author = swipeItem.getVauthor();
            mp3 = swipeItem.getVmp3();
            cat = swipeItem.getVcat();
            subcat = swipeItem.getVsubcat();
            impression = swipeItem.getInview() > 0;
            click = swipeItem.getClick() > 0;
        }
    }

    @Getter
    @Setter
    class Session {
        private String token;
        private String startId;
        private List<SessionVideo> videos = new ArrayList<>();
        private Integer lastImpression = -1;

        public Session(DynamicDumpInfo.Swipe swipe, String specifiedStartId) {
            token = swipe.getToken();
            startId = swipe.getStartid();
            List<DynamicDumpInfo.SwipeItem> rawVideoArray = swipe.getVideos();
            if (rawVideoArray != null && !rawVideoArray.isEmpty()) {
                for (int i = 0; i < rawVideoArray.size(); i++) {
                    SessionVideo video = new SessionVideo(rawVideoArray.get(i));
                    if (video.getImpression()) {
                        lastImpression = i;
                    }
                    if (startId.equals(specifiedStartId) && lastImpression > 5) {
                        video.setImpression(true);
                    }
                    videos.add(video);
                }
            }
        }

        public Boolean isInactiveImpression() {
            return videos.stream().noneMatch(SessionVideo::getClick)
                    && videos.stream().filter(SessionVideo::getImpression).count() <= 2
                    && lastImpression <= 1;
        }
    }
}
