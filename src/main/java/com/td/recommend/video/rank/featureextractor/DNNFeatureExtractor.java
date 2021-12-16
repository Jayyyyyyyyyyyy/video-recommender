package com.td.recommend.video.rank.featureextractor;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.td.rank.deepfm.felib.Buckets;
import com.td.rank.deepfm.felib.FeatureConfig;
import com.td.rank.deepfm.felib.Vocabulary;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author zhanghongtao
 */
public interface DNNFeatureExtractor {
    JSONObject extract(PredictItem<DocItem> predictItem, FeatureConfig featureConfig,
                       Vocabulary vocabulary, Buckets buckets, JSONObject docIrrelevantRawJSON, JSONObject docIrrelevantRawFeatures,
                       JSONObject docIrrelevantBucketizedFeatures, VideoRecommenderContext recommendContext);

    default void accLatestTimestampFeature(String prefix, String slot, String feat, Long requestTs, JSONObject latestTimestamp, JSONObject output) {
        String key = slot + feat;
        if (latestTimestamp != null && latestTimestamp.containsKey(key)) {
            Long latestTimes = latestTimestamp.getLong(key);
            long gap = DateUtil.betweenDay(new Date(latestTimes), new Date(requestTs), false);
            output.put(String.format("ud_%s_gap_%s", prefix, slot), gap);
        }
    }

    default void accLatestTimestampFeature(JSONObject featureJson, JSONObject docIrrelevantRawJSON) {
        String firstCatId = featureJson.getString("firstcat");
        if (StringUtils.isNotBlank(firstCatId)) {
            accLatestTimestampFeature("click", "cat", firstCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("clickSeqLatestTimestamp"), featureJson);
            accLatestTimestampFeature("download", "cat", firstCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("downloadSeqLatestTimestamp"), featureJson);
            accLatestTimestampFeature("fav", "cat", firstCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("favSeqLatestTimestamp"), featureJson);
        }

        String secondCatId = featureJson.getString("secondcat");
        if (StringUtils.isNotBlank(secondCatId)) {
            accLatestTimestampFeature("click", "subcat", secondCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("clickSeqLatestTimestamp"), featureJson);
            accLatestTimestampFeature("download", "subcat", secondCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("downloadSeqLatestTimestamp"), featureJson);
            accLatestTimestampFeature("fav", "subcat", secondCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("favSeqLatestTimestamp"), featureJson);
        }

        String mp3 = featureJson.getString("content_mp3");
        if (StringUtils.isNotBlank(mp3)) {
            accLatestTimestampFeature("click", "mp3", mp3, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("clickSeqLatestTimestamp"), featureJson);
            accLatestTimestampFeature("download", "mp3", mp3, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("downloadSeqLatestTimestamp"), featureJson);
            accLatestTimestampFeature("fav", "mp3", mp3, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("favSeqLatestTimestamp"), featureJson);
        }

        String uid = featureJson.getString("doc_uid");
        if (StringUtils.isNotBlank(uid)) {
            accLatestTimestampFeature("click", "author", Convert.toStr(uid), System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("clickSeqLatestTimestamp"), featureJson);
            accLatestTimestampFeature("download", "author", Convert.toStr(uid), System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("downloadSeqLatestTimestamp"), featureJson);
            accLatestTimestampFeature("fav", "author", Convert.toStr(uid), System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("favSeqLatestTimestamp"), featureJson);
        }
    }
}
