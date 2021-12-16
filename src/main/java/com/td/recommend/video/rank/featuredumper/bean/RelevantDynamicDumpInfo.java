package com.td.recommend.video.rank.featuredumper.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RelevantDynamicDumpInfo extends DynamicDumpInfo {
    Map<String, Map<String, Double>> currentDocDynamicDocFeatures;
    VideoStaticFeature currentItemProfile;
    String currentDocId;
    List<Swipe> feedSwipes;
    JSONObject probeDyns;
}
