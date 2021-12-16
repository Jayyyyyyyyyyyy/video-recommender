package com.td.recommend.video.rerank.model;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.td.recommend.commons.io.FileChangeSubject;
import com.td.recommend.commons.io.FileChangedEvent;
import com.td.recommend.commons.io.FileChangedListener;
import com.td.recommend.video.utils.UserVideoConfig;
import com.td.rerank.dnn.felib.*;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LocalRerankDNNModel {
    private static final Logger LOG = LoggerFactory.getLogger(LocalRerankDNNModel.class);
    public static final LocalRerankDNNModel rerank = new LocalRerankDNNModel("rerank-dnn-model");
    public static final LocalRerankDNNModel rerank2 = new LocalRerankDNNModel("rerank-dnn-model2");
    public static final LocalRerankDNNModel rerank3 = new LocalRerankDNNModel("rerank-dnn-model3");
    private volatile RerankDNNModel rerankDNNModel;
    private volatile RerankDNNModel rerankDNNModelTemp;
    private String modelFile;
    private String featureConfigFileName = "feature.conf";
    private String mtlConfigFileName = "mtl.conf";
    private String vocabFileName = "vocab.txt";
    private String bucketFileName = "buckets.txt";
    private String onlineFileName = "online.conf";
    private volatile FeatureConfig featureConfig;
    private volatile FeatureConfig featureConfigTemp;
    private volatile MtlConfigV3 mtlConfigV3;
    private volatile MtlConfigV3 mtlConfigV3Temp;
    private volatile Vocabulary vocabulary;
    private volatile Vocabulary vocabularyTemp;
    private volatile Buckets buckets;
    private volatile Buckets bucketsTemp;
    private volatile OnlineConfig onlineConfig;
    private volatile OnlineConfig onlineConfigTemp;
    private FileChangeSubject fileChangeSubject;

    public static void warmUp() {
    }

    private LocalRerankDNNModel(String modelKey) {
        long start = System.currentTimeMillis();
        try {
            LOG.info("rerank dnn load from {} start ...", modelKey);
            Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
            this.modelFile = userNewsConfig.getString(modelKey);
            init();
            if (rerankDNNModelTemp != null) {
                rerankDNNModel = rerankDNNModelTemp;
            }
            featureConfig = featureConfigTemp;
            mtlConfigV3 = mtlConfigV3Temp;
            vocabulary = vocabularyTemp;
            buckets = bucketsTemp;
            onlineConfig = onlineConfigTemp;
            File file = new File((modelFile));
            String watchDir = file.getParent();
            String watchFile = file.getName();

            fileChangeSubject = new FileChangeSubject(watchDir, watchFile);
            fileChangeSubject.addListener(new ReloadModelListener());
        } catch (IOException e) {
            LOG.error("create rerank model from {} failed!", modelKey, e);
        }
        LOG.info("create rerank model from {} end. {}ms", modelKey, (System.currentTimeMillis() - start));
    }

    public RerankDNNModel getRerankDNNModel() {
        return this.rerankDNNModel;
    }

    public FeatureConfig getFeatureConfig() {
        return this.featureConfig;
    }

    public MtlConfigV3 getMtlConfigV3() {
        return this.mtlConfigV3;
    }

    public Vocabulary getVocabulary() {
        return this.vocabulary;
    }

    public Buckets getBuckets() {
        return this.buckets;
    }

    public OnlineConfig getOnlineConfig() {
        return this.onlineConfig;
    }

    private void init() {
        File mf = new File(this.modelFile);
        if (mf.exists()) {
            rerankDNNModelTemp = new RerankDNNModel(this.modelFile, "serve");
        }

        String featureConfigFilePath = String.join("/", this.modelFile, this.featureConfigFileName);
        File cf = new File(featureConfigFilePath);
        if (cf.exists()) {
            featureConfigTemp = new FeatureConfig();
            if (featureConfigTemp.loadFromLocal(featureConfigFilePath)) {
                LOG.info("load rerank featureConfig success||path : {} ", featureConfigFilePath);
            }
        }

        String mtlConfigFilePath = String.join("/", this.modelFile, this.mtlConfigFileName);
        File mcf = new File(mtlConfigFilePath);
        if (mcf.exists()) {
            mtlConfigV3Temp = new MtlConfigV3();
            try {
                if (mtlConfigV3Temp.load(mtlConfigFilePath)) {
                    LOG.info("load rerank mtlConfigV2 success||path : {} ", mtlConfigFilePath);
                }
            } catch (Exception e) {
                LOG.error("load rerank mtlConfigV2 error", e);
            }
        }

        String bucketFilePath = String.join("/", this.modelFile, this.bucketFileName);
        File bf = new File(bucketFilePath);
        if (bf.exists()) {
            bucketsTemp = new Buckets();
            try {
                if (bucketsTemp.load(bucketFilePath)) {
                    LOG.info("load rerank buckets success||path : {} ", bucketFilePath);
                }
            } catch (Exception e) {
                LOG.error("load rerank buckets error", e);
            }
        }

        String vocabFilePath = String.join("/", this.modelFile, this.vocabFileName);
        File vf = new File(vocabFilePath);
        if (vf.exists()) {
            vocabularyTemp = new Vocabulary();
            try {
                if (vocabularyTemp.load(vocabFilePath)) {
                    LOG.info("locad rerank vocabulary success||path : {} ", vocabFilePath);
                }
            } catch (Exception e) {
                LOG.error("load rerank vocabulary error", e);
            }
        }

        String onlineFilePath = String.join("/", this.modelFile, this.onlineFileName);
        File of = new File(onlineFilePath);
        if (of.exists()) {
            onlineConfigTemp = new OnlineConfig();
            try {
                if (onlineConfigTemp.load(onlineFilePath)) {
                    LOG.info("locad rerank onlineConfig success||path : {} ", onlineFilePath);
                }
            } catch (Exception e) {
                LOG.error("load rerank onlineConfig error", e);
            }
        }
    }

    private class ReloadModelListener implements FileChangedListener {
        @Override
        public void onChanged(FileChangedEvent event) {
            String fileName = event.getFileName();
            LOG.warn("File name={} has changed, begin reload model...", fileName);
            init();
            for (int j = 0; j < 100; j++) {
                JSONObject user = FeatureExtractor.buildRandomIdfeatures(featureConfigTemp, 0);
                List<JSONObject> items = Lists.newArrayList();
                List<Integer> indexList = Lists.newArrayList();
                for (int i = 0; i < onlineConfigTemp.topN(); i++) {
                    indexList.add(0);
                    items.add(FeatureExtractor.buildRandomIdfeatures(featureConfigTemp, 1));
                }
                long start = System.currentTimeMillis();
                if (rerankDNNModelTemp != null) {
                    rerankDNNModelTemp.predict(items, user, featureConfigTemp, onlineConfigTemp.modelTaskNum(), items.size(), indexList, indexList, indexList);
                }
                long end = System.currentTimeMillis();
                LOG.info("rerank dnn from model {} 第 {} 轮 warmup 耗时 {}ms", modelFile, j, end - start);
                if (end - start < 100) {
                    break;
                }
            }
            if (rerankDNNModelTemp != null) {
                RerankDNNModel closeModel = rerankDNNModel;
                rerankDNNModel = rerankDNNModelTemp;
                closeModel.close();
            }
            featureConfig = featureConfigTemp;
            mtlConfigV3 = mtlConfigV3Temp;
            vocabulary = vocabularyTemp;
            buckets = bucketsTemp;
            onlineConfig = onlineConfigTemp;
            LOG.warn("Successful load rerank dnn model from {}.", modelFile);
        }
    }
}
