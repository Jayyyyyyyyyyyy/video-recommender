package com.td.recommend.video.rank.featureextractor;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.td.data.profile.TDocItem;
import com.td.data.profile.TDynamicDocDataNew;
import com.td.data.profile.TDynamicDocRaw;
import com.td.data.profile.item.ItemDocumentData;
import com.td.data.profile.item.KeyItem;
import com.td.data.profile.item.VideoItem;
import com.td.featurestore.feature.IFeature;
import com.td.featurestore.feature.IFeatures;
import com.td.rank.deepfm.felib.Buckets;
import com.td.rank.deepfm.felib.FeatureConfig;
import com.td.rank.deepfm.felib.FieldNavigatorModule;
import com.td.rank.deepfm.felib.Vocabulary;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.response.Tag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.api.vo.NewsDocBuilder;
import com.td.recommend.video.rank.monitor.DNNFeatureMonitor;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class FeedDNNFeatureExtractor2 implements DNNFeatureExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(FeedDNNFeatureExtractor2.class);
    private static final FeedDNNFeatureExtractor2 instance = new FeedDNNFeatureExtractor2();

    public static FeedDNNFeatureExtractor2 getInstance() {
        return instance;
    }

    private static final String[] DOC_FEATURES = new String[]{
            "dy_1day",
            "dy_7day",
            "dy_feed_1day",
            "dy_feed_7day"
    };

    @Override
    public JSONObject extract(PredictItem<DocItem> predictItem, FeatureConfig featureConfig, Vocabulary vocabulary, Buckets buckets, JSONObject docIrrelevantRawJSON, JSONObject docIrrelevantRawFeatures, JSONObject docIrrelevantBucketizedFeatures, VideoRecommenderContext recommendContext) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        JSONObject featureJson = new JSONObject();
        DocItem docItem = predictItem.getItem();
        if (docItem != null) {
            Optional<ItemDocumentData> itemDocumentDataOptional = docItem.getNewsDocumentData();
            if (itemDocumentDataOptional.isPresent()) {
                ItemDocumentData itemDocumentData = itemDocumentDataOptional.get();
                Optional<VideoItem> videoItemOptional = itemDocumentData.getStaticDocumentData();
                if (videoItemOptional.isPresent()) {
                    VideoItem videoItem = videoItemOptional.get();
                    String createTime = videoItem.getCreatetime();
                    if (StringUtils.isNotBlank(createTime)) {
                        featureJson.put("video_age", caculateTotalTime(dateFormat(""), createTime));
                    }
                    Optional<KeyItem> fistcatOptional = videoItem.getKeyItemByName("firstcat");
                    fistcatOptional.ifPresent(keyItem -> {
                        String firstCatId = keyItem.getId();
                        featureJson.put("firstcat", firstCatId);
                        accLatestTimestampFeature("click", "cat", firstCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("clickSeqLatestTimestamp"), featureJson);
                        accLatestTimestampFeature("download", "cat", firstCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("downloadSeqLatestTimestamp"), featureJson);
                        accLatestTimestampFeature("fav", "cat", firstCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("favSeqLatestTimestamp"), featureJson);
                    });

                    Optional<KeyItem> secondcatOptional = videoItem.getKeyItemByName("secondcat");
                    secondcatOptional.ifPresent(keyItem -> {
                        String secondCatId = keyItem.getId();
                        featureJson.put("secondcat", secondCatId);
                        accLatestTimestampFeature("click", "subcat", secondCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("clickSeqLatestTimestamp"), featureJson);
                        accLatestTimestampFeature("download", "subcat", secondCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("downloadSeqLatestTimestamp"), featureJson);
                        accLatestTimestampFeature("fav", "subcat", secondCatId, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("favSeqLatestTimestamp"), featureJson);
                    });

                    featureJson.put("content_tag", videoItem.getContent_tag());

                    Optional<KeyItem> content_mp3Optional = videoItem.getKeyItemByName("content_mp3");
                    content_mp3Optional.ifPresent(keyItem -> {
                        String mp3 = keyItem.getName();
                        featureJson.put("content_mp3", mp3);
                        accLatestTimestampFeature("click", "mp3", mp3, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("clickSeqLatestTimestamp"), featureJson);
                        accLatestTimestampFeature("download", "mp3", mp3, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("downloadSeqLatestTimestamp"), featureJson);
                        accLatestTimestampFeature("fav", "mp3", mp3, System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("favSeqLatestTimestamp"), featureJson);
                    });

                    int uid = videoItem.getUid();
                    if (uid > 0) {
                        accLatestTimestampFeature("click", "author", Convert.toStr(uid), System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("clickSeqLatestTimestamp"), featureJson);
                        accLatestTimestampFeature("download", "author", Convert.toStr(uid), System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("downloadSeqLatestTimestamp"), featureJson);
                        accLatestTimestampFeature("fav", "author", Convert.toStr(uid), System.currentTimeMillis(), docIrrelevantRawJSON.getJSONObject("favSeqLatestTimestamp"), featureJson);
                        featureJson.put("doc_uid", uid);
                    }

                    Optional<KeyItem> content_genreOptional = videoItem.getKeyItemByName("content_genre");
                    content_genreOptional.ifPresent(keyItem -> featureJson.put("content_genre", keyItem.getId()));

                    featureJson.put("title_len", videoItem.getTitle_len());

                    Optional<KeyItem> content_teachOptional = videoItem.getKeyItemByName("content_teach");
                    content_teachOptional.ifPresent(keyItem -> featureJson.put("content_teach", keyItem.getId()));

                    featureJson.put("duration", videoItem.getDuration());
                }

                Optional<TDynamicDocDataNew> dynamicDocumentDataNew = itemDocumentData.getDynamicDocumentDataNew();
                if (dynamicDocumentDataNew.isPresent()) {
                    TDynamicDocDataNew tDynamicDocDataNew = dynamicDocumentDataNew.get();
                    TDynamicDocRaw recom = tDynamicDocDataNew.getRawMap().get("recom");
                    if (recom != null) {
                        Map<String, TDocItem> callCtrMap = recom.callCtrMap;
                        if (callCtrMap != null && !callCtrMap.isEmpty()) {
                            List<RetrieveKey> retrieveKeys = predictItem.getRetrieveKeys();
                            if (retrieveKeys != null && !retrieveKeys.isEmpty()) {
                                List<TDocItem> hitList = new ArrayList<>();
                                retrieveKeys.forEach(retrieveKey -> {
                                    TDocItem tDocItem = callCtrMap.get(retrieveKey.getType() + "_*");
                                    if (tDocItem != null) {
                                        hitList.add(tDocItem);
                                    }
                                });
                                if (!hitList.isEmpty()) {
                                    Optional<Double> maxOptional = hitList.stream().filter(tDocItem -> tDocItem.getView() >= 10).map(TDocItem::getCtr).max(Double::compareTo);
                                    if (maxOptional.isPresent()) {
                                        featureJson.put("recall_ctr", maxOptional.get());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            featureJson.put("docId", docItem.getId());
            extractDocFeatures(docItem, featureJson);
        }
        List<RetrieveKey> retrieveKeys = predictItem.getRetrieveKeys();
        if (retrieveKeys != null && retrieveKeys.size() > 0) {
            List<String> types = Lists.newArrayList();
            Map<String, Double> recallQueuePosMap = Maps.newHashMap();
            for (RetrieveKey retrieveKey : retrieveKeys) {
                String type = retrieveKey.getType();
                double score = retrieveKey.getScore();
                Double pos = recallQueuePosMap.get(type);
                if (pos == null) {
                    recallQueuePosMap.put(type, score);
                } else {
                    if (score < pos) {
                        recallQueuePosMap.put(type, score);
                    }
                }
                types.add(type);

            }
            featureJson.put("recall_queue", types);
            featureJson.put("recall_queue_num", recallQueuePosMap.size());//group 去重之后个数
            featureJson.put("recall_key_num", retrieveKeys.size());
            featureJson.put("recall_queue_pos", recallQueuePosMap);
        }

        List<Tag> tags = NewsDocBuilder.getInstance().buildTags(predictItem, recommendContext);
        featureJson.put("uiTags", tags);

        Timer.Context singleExtract = taggedMetricRegistry.timer("usernews.dnn2.predict.single.itemfeature.extract.latency").time();
        JSONObject docRelevantIdFeatures = null;
        try {
            JSONObject docRelevantRawFeatures = com.td.rank.deepfm.felib.FeatureExtractor.extractDocRelevantRawFeatures(featureJson, docIrrelevantRawFeatures, featureConfig, FieldNavigatorModule.ONLINE());
            JSONObject docRelevantBucketizedFeatures = com.td.rank.deepfm.felib.FeatureExtractor.extractDocRelevantBucketizedFeatures(docRelevantRawFeatures, docIrrelevantBucketizedFeatures, featureConfig, FieldNavigatorModule.ONLINE(), buckets);
            Random random = new Random();
            float r = random.nextFloat();
            if (r < 0.01) {
                DNNFeatureMonitor.getInstance().asynMonitor(featureConfig, docRelevantBucketizedFeatures, 1, "dnn2", recommendContext);
            }
            docRelevantIdFeatures = com.td.rank.deepfm.felib.FeatureExtractor.extractDocRelevantIdFeatures(docRelevantBucketizedFeatures, featureConfig, vocabulary);
        } catch (Exception e) {
            LOG.error("DocRelevantIdFeatures extract error", e);
        }
        singleExtract.stop();
        return docRelevantIdFeatures;
    }

    private void extractDocFeatures(DocItem docItem, JSONObject featureJson) {
        for (String docFeature : DOC_FEATURES) {
            JSONObject dynamicDocFeature = new JSONObject();
            Optional<IFeatures> featuresOpt = docItem.getFeatures(docFeature);
            if (featuresOpt.isPresent()) {
                IFeatures features = featuresOpt.get();
                for (IFeature feature : features) {
                    dynamicDocFeature.put(feature.getName(), feature.getValue());
                }
                featureJson.put(docFeature, dynamicDocFeature);
            }
        }
    }

    private String dateFormat(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }

    private int caculateTotalTime(String startTime, String endTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = null;
        Date date = null;
        Long l = 0L;
        try {
            date = formatter.parse(startTime);
            long ts = date.getTime();
            date1 = formatter.parse(endTime);
            long ts1 = date1.getTime();
            l = (ts - ts1) / (1000 * 60 * 60 * 24);
        } catch (Exception e) {
            LOG.error("video_age time parse error", e);
        }
        return l.intValue();
    }
}
