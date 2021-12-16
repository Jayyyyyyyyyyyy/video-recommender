package com.td.recommend.video.recommender;

import com.alibaba.fastjson.JSONObject;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.idgenerator.TokenIdGenerator;
import com.td.recommend.commons.poi.PlaceInfo;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.rerank.RatioRule;
import com.td.recommend.core.recommender.RecommendContext;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.RecentEvents;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by admin on 2017/12/8.
 */
@Getter
@Setter
public class VideoRecommenderContext extends RecommendContext<DocItem> {
    private String token;
    private Optional<PlaceInfo> placeInfo;
    private Optional<RecentEvents> recentEvents;
    private String model = "";
    private String rerankModel = "";
    private List<DynamicDumpInfo.WordTime> keyWords;
    private List<String> fav;
    private List<String> downLoad;
    private long activeTime;
    private String userInterest;
    private String activeCat;
    private UserProfileUtils.UserType userType;
    private List<RatioRule> dynamicRules = new ArrayList<>();
    private List<DynamicDumpInfo.VidDuration> played;
    private Map<String, String> recReasonMap;
    private List<DynamicDumpInfo.Swipe> swipes;
    private List<DynamicDumpInfo.SeqItem> clickSeq;
    private List<DynamicDumpInfo.SeqItem> downloadSeq;
    private List<DynamicDumpInfo.SeqItem> favSeq;
    private List<DynamicDumpInfo.SeqItem> playSeq;
    private Map<String, Map<String, Map<String, Number>>> rlvtdyns;
    private String trendUid; // used by trend with RecommendRequest's ihf in (1,11) and fp=1
    private String trendVidStrings; // used by trend with RecommendRequest's ihf=1 and fp=1
    private String trendRecall; // used by trend with RecommendRequest's ihf=1 and fp=1
    private String trendUidInfo; // used by trend with RecommendRequest's ihf=1 and fp=1
    private String talentClusterLine;
    private UserProfileUtils.TrendUserType trendUserType;

    public VideoRecommenderContext(String userId, Set<String> used, Set<String> buckets, Items queryItems, boolean debug) {
        super(userId, used, buckets, queryItems, debug);
        UserItem userItem = getUserItem();
        this.token = TokenIdGenerator.getInstance().generate(userItem.getId());
    }

    public UserItem getUserItem() {
        Items queryItems = this.getQueryItems();
        Optional<IItem> itemOpt = queryItems.get(ItemKey.user);
        if (!itemOpt.isPresent()) {
            if (recommendRequest != null) {
                return new UserItem(recommendRequest.getDiu());
            } else {
                return new UserItem("123");
            }
        }
        IItem item = itemOpt.get();
        return (UserItem) item;
    }

    public void addDynamicRule(RatioRule rule) {
        dynamicRules.add(rule);
    }

    public void addDynamicRules(List<RatioRule> rules) {
        dynamicRules.addAll(rules);
    }

    public boolean hasBucket(String bucket) {
        return this.getBuckets().contains(bucket);
    }
}
