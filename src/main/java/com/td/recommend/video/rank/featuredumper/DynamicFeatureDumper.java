package com.td.recommend.video.rank.featuredumper;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.td.recommend.commons.json.JsonUtils;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import com.td.recommend.video.utils.KafkaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static com.codahale.metrics.Timer.Context;

/**
 * Created by admin on 2017/6/23.
 */
public class DynamicFeatureDumper {
    private static final Logger LOG = LoggerFactory.getLogger("FeatureDumper");
    private static final String tab = "\t";
    private static final ExecutorService dumpExecutor= new ThreadPoolExecutor(8, 15,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(5000),
                new ThreadFactoryBuilder().setNameFormat("feature-dump-%d").build());

    private static DynamicFeatureDumper instance =  new DynamicFeatureDumper();

    private KafkaClient kafkaClient;
    private static final String TOPIC = "video_dynamic_features";

    public static DynamicFeatureDumper getInstance() {
        return instance;
    }

    private DynamicFeatureDumper() {
        kafkaClient = KafkaClient.getInstance();
    }

    public void asyncDumpAll(List<DynamicDumpInfo> dynamicDumpInfos) {
        CompletableFuture.runAsync(() -> dumpAll(dynamicDumpInfos), dumpExecutor);
    }
    public void asyncDump(DynamicDumpInfo dynamicDumpInfo){
        CompletableFuture.runAsync(() -> dump(dynamicDumpInfo), dumpExecutor);
    }

    public void dumpAll(List<DynamicDumpInfo> dynamicDumpInfos) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Context featureDumpTime = taggedMetricRegistry.timer("uservideo.featuredump.latency").time();

        int dumpTopK = Math.max(250, dynamicDumpInfos.size());
        dynamicDumpInfos.sort(Comparator.comparing(DynamicDumpInfo::getPredictScore).reversed());

        int index = 0;
        for (DynamicDumpInfo dynamicDumpInfo : dynamicDumpInfos) {
            dump(dynamicDumpInfo);

            if (index > dumpTopK) {
                break;
            }
            ++index;
        }

        featureDumpTime.stop();
    }

    public void dump(DynamicDumpInfo dynamicDumpInfo) {
        Optional<String> dumpJsonOpt = JsonUtils.toJson(dynamicDumpInfo);
        if (dumpJsonOpt.isPresent()) {
            String predictId = dynamicDumpInfo.getPredictId() != null? dynamicDumpInfo.getPredictId() : "";
            String userId = dynamicDumpInfo.getUserId() != null? dynamicDumpInfo.getUserId() : "";
            String docId = dynamicDumpInfo.getDocId() != null? dynamicDumpInfo.getDocId() : "";
            String dumpJsonStr = predictId.concat(tab).concat(userId).concat(tab).concat(docId).concat(tab).concat(dumpJsonOpt.get());
            kafkaClient.send(TOPIC, userId, dumpJsonStr);
        }
    }
//    @Getter
//    @Setter
//    public static class DumpInfo {
//        Map<String, Map<String, Double>> dynamicDocFeatures;
//        Map<String, Map<String, UserStaticInfo>> stUserFeatures;
//        Map<String, Double> retrieveFeatures;
//        Map<String, Double> xFollowFeatures;
//        List<String> clicked;
//        List<String> skiped;
//        String predictId;
//        String docId;
//        String userId;
//        double predictScore;
//        long timestamp;
//        String vprofile_info;
//        List<Tag> uiTags;
//        List<KeySearchTime> searchMp3;
//        List<KeySearchTime> searchTeacher;
//        List<String> bpr_u2u;
//    }
//    @Getter
//    @Setter
//    public static class UserStaticInfo {
//        public double mean;
//        public double posCnt;
//        public double negCnt;
//        public double variance;
//
//        public UserStaticInfo(double mean,double posCnt,double negCnt,double variance){
//            this.mean = mean;
//            this.posCnt = posCnt;
//            this.negCnt = negCnt;
//            this.variance = variance;
//        }
//    }
//
//    @Getter
//    @Setter
//    public static class VideoInfo {
//        public String vid;
//        public String cat;
//        public String subcat;
//        public List<String> tag;
//        public String author;
//        public String mp3;
//        public int rate;
//        public int playtime;
//        public String modules;
//        public String dateStr;
//
//        public VideoInfo(String vid,String cat,String subcat,List<String> tag,String author,String mp3,int rate,int playtime,String modules,String dateStr){
//            this.vid = vid;
//            this.cat = cat;
//            this.subcat = subcat;
//            this.tag = tag;
//            this.author = author;
//            this.mp3 = mp3;
//            this.rate = rate;
//            this.playtime = playtime;
//            this.modules = modules;
//            this.dateStr = dateStr;
//        }
//    }
//    @Getter
//    @Setter
//    public static class KeySearchTime {
//        public String key;
//        public long ts;
//
//
//        public KeySearchTime(String key,long ts){
//            this.key = key;
//            this.ts = ts;
//        }
//    }
//    @Getter
//    @Setter
//    @ToString
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    public static class SimUserDoc {
//        public Integer status;
//        public List<SimUser> data;
//    }
//    @Getter
//    @Setter
//    public static class SimUser{
//        private String id;
//        private double score;
//    }

}
