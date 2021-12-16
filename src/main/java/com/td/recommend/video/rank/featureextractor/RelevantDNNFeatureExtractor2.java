package com.td.recommend.video.rank.featureextractor;

import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

public class RelevantDNNFeatureExtractor2 implements DNNFeatureExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantDNNFeatureExtractor2.class);
    private static final RelevantDNNFeatureExtractor2 instance = new RelevantDNNFeatureExtractor2();

    public static RelevantDNNFeatureExtractor2 getInstance() {
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
            extractStaticDocFeatures(featureJson, docItem, "");
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
                    if (score < pos.doubleValue()) {
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
        Timer.Context singleExtract = taggedMetricRegistry.timer("rlvt.dnn2.predict.single.itemfeature.extract.latency").time();
        JSONObject docRelevantIdFeatures = null;
        try {
            JSONObject docRelevantRawFeatures = com.td.rank.deepfm.felib.FeatureExtractor.extractDocRelevantRawFeatures(featureJson, docIrrelevantRawFeatures, featureConfig, FieldNavigatorModule.ONLINE());
            JSONObject docRelevantBucketizedFeatures = com.td.rank.deepfm.felib.FeatureExtractor.extractDocRelevantBucketizedFeatures(docRelevantRawFeatures, docIrrelevantBucketizedFeatures, featureConfig, FieldNavigatorModule.ONLINE(), buckets);
            Random random = new Random();
            float r = random.nextFloat();
            if (r < 0.01) {
                DNNFeatureMonitor.getInstance().asynMonitor(featureConfig, docRelevantBucketizedFeatures, 1, "rlvtdnn2", recommendContext);
            }
            docRelevantIdFeatures = com.td.rank.deepfm.felib.FeatureExtractor.extractDocRelevantIdFeatures(docRelevantBucketizedFeatures, featureConfig, vocabulary);
        } catch (Exception e) {
            LOG.error("DocRelevantIdFeatures extract error", e);
        }
        singleExtract.stop();
        return docRelevantIdFeatures;
    }

    public static void extractStaticDocFeatures(JSONObject featureJson, DocItem docItem, String prefix) {
        Optional<ItemDocumentData> itemDocumentData = docItem.getNewsDocumentData();
        if (itemDocumentData.isPresent()) {
            Optional<VideoItem> videoItem = itemDocumentData.get().getStaticDocumentData();
            if (videoItem.isPresent()) {
                String createTime = videoItem.get().getCreatetime();
                if (StringUtils.isNotBlank(createTime)) {
                    featureJson.put(prefix + "video_age", caculateTotalTime(dateFormat(""), createTime));
                }
            }
            Optional<VideoItem> videoItemOptional = itemDocumentData.get().getStaticDocumentData();
            if (videoItemOptional.isPresent()) {
                VideoItem videoItem1 = videoItemOptional.get();
                Optional<KeyItem> fistcatOptional = videoItem1.getKeyItemByName("firstcat");
                fistcatOptional.ifPresent(keyItem -> featureJson.put(prefix + "firstcat", keyItem.getId()));

                Optional<KeyItem> secondcatOptional = videoItem1.getKeyItemByName("secondcat");
                secondcatOptional.ifPresent(keyItem -> featureJson.put(prefix + "secondcat", keyItem.getId()));

                featureJson.put(prefix + "content_tag", videoItem1.getContent_tag());

                Optional<KeyItem> content_mp3Optional = videoItem1.getKeyItemByName("content_mp3");
                content_mp3Optional.ifPresent(keyItem -> featureJson.put(prefix + "content_mp3", keyItem.getName()));

                featureJson.put(prefix + "doc_uid", videoItem1.getUid());

                Optional<KeyItem> content_genreOptional = videoItem1.getKeyItemByName("content_genre");
                content_genreOptional.ifPresent(keyItem -> featureJson.put(prefix + "content_genre", keyItem.getId()));

                featureJson.put(prefix + "title_len", videoItem1.getTitle_len());

                Optional<KeyItem> content_teachOptional = videoItem1.getKeyItemByName("content_teach");
                content_teachOptional.ifPresent(keyItem -> featureJson.put(prefix + "content_teach", keyItem.getId()));

                featureJson.put(prefix + "duration", videoItem1.getDuration());
            }
        }
        featureJson.put(prefix + "docId", docItem.getId());
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

    private static String dateFormat(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }

    private static int caculateTotalTime(String startTime, String endTime) {
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
