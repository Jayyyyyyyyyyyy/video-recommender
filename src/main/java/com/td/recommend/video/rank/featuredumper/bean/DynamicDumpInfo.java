package com.td.recommend.video.rank.featuredumper.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.td.recommend.commons.response.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DynamicDumpInfo {
    Map<String, Map<String, Double>> dynamicDocFeatures;
    Map<String, Map<String, Double>> callCtrFeatures;
    Map<String, Map<String, UserStaticInfo>> stUserFeatures;
    Map<String, Double> retrieveFeatures;
    Map<String, Double> xFollowFeatures;
    List<String> clicked;
    String predictId;
    String docId;
    String userId;
    double predictScore;
    double rerankPredictScore;
    Map<String, Double> modelScore;
    Map<String, Double> rerankModelScore;
    long timestamp;
    long activeTime;
    String userInterest;
    String activeCat;
    String vprofile_info;
    List<Tag> uiTags;
    List<KeySearchTime> searchMp3;
    List<KeySearchTime> searchTeacher;
    List<String> bpr_u2u;
    VideoStaticFeature itemProfile;
    List<KeySearchTime> searchQuery;
    List<String> fav;
    List<String> download;
    String mtlBucket;
    List<VidDuration> played;
    List<Swipe> swipes;
    String startid;
    List<SeqItem> clickSeq;
    List<SeqItem> downloadSeq;
    List<SeqItem> favSeq;
    List<SeqItem> playSeq;
    Integer rankPos;
    Integer dumpUser;


    @Getter
    @Setter
    public static class UserStaticInfo {
        public double mean;
        public double posCnt;
        public double negCnt;
        public double variance;

        public UserStaticInfo(double mean, double posCnt, double negCnt, double variance) {
            this.mean = mean;
            this.posCnt = posCnt;
            this.negCnt = negCnt;
            this.variance = variance;
        }
    }

    @Getter
    @Setter
    public static class VideoInfo {
        public String vid;
        public String cat;
        public String subcat;
        public List<String> tag;
        public String author;
        public String mp3;
        public int rate;
        public int playtime;
        public String modules;
        public String dateStr;

        public VideoInfo(String vid, String cat, String subcat, List<String> tag, String author, String mp3, int rate, int playtime, String modules, String dateStr) {
            this.vid = vid;
            this.cat = cat;
            this.subcat = subcat;
            this.tag = tag;
            this.author = author;
            this.mp3 = mp3;
            this.rate = rate;
            this.playtime = playtime;
            this.modules = modules;
            this.dateStr = dateStr;
        }
    }

    @Getter
    @Setter
    public static class KeySearchTime {
        public String key;
        public long ts;


        public KeySearchTime(String key, long ts) {
            this.key = key;
            this.ts = ts;
        }
    }

    @Getter
    @Setter
    public static class WordTime {
        public String w;
        public long t;
    }

    @Getter
    @Setter
    @ToString
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SimUserDoc {
        public Integer status;
        public List<SimUser> data;
    }

    @Getter
    @Setter
    public static class SimUser {
        private String id;
        private double score;
    }

    @Getter
    @Setter
    public static class VidDuration {
        private String id;
        private double pt;
    }

    @Getter
    @Setter
    public static class SwipeItem {
        private String vid;
        private double inview;
        private double click;
        private String vcat;
        private String vsubcat;
        private String vauthor;
        private String vmp3;
        private double duration;
        private double download;
        private double fav;
    }

    @Getter
    @Setter
    public static class Swipe {
        private String startid;
        private List<SwipeItem> videos;
        private long time;
        private String token;
    }

    @Getter
    @Setter
    public static class SeqItem {
        private String vid;
        private String vcat;
        private String vsubcat;
        private String vauthor;
        private String vmp3;
        private List<String> vtag;
        private String mod;
        private long pt;
        private long time;
    }
}
