package com.td.recommend.video.rank.model;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.td.rank.deepfm.felib.*;
import com.td.recommend.commons.io.FileChangeSubject;
import com.td.recommend.commons.io.FileChangedEvent;
import com.td.recommend.commons.io.FileChangedListener;
import com.td.recommend.commons.rank.model.DNNModel;
import com.td.recommend.commons.rank.model.DNNModel2;
import com.td.recommend.video.utils.UserVideoConfig;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LocalDNNModel {
    private static final Logger LOG = LoggerFactory.getLogger(LocalDNNModel.class);
    public static final LocalDNNModel feed = new LocalDNNModel("dnn-model");
    public static final LocalDNNModel feed2 = new LocalDNNModel("dnn-model2");
    public static final LocalDNNModel feed3 = new LocalDNNModel("dnn-model3");
    public static final LocalDNNModel rlvt = new LocalDNNModel("dnn-model-rlvt");
    public static final LocalDNNModel rlvt2 = new LocalDNNModel("dnn-model-rlvt2");
    private volatile DNNModel dnnModel;
    private volatile DNNModel dnnModelTemp;
    private volatile DNNModel2 dnnModel2;
    private volatile DNNModel2 dnnModelTemp2;
    private String modelFile;
    private String featureConfigFileName = "feature.conf";
    private String mtlConfigFileName = "mtl.conf";
    private String vocabFileName = "vocab.txt";
    private String bucketFileName = "buckets.txt";
    private volatile FeatureConfig featureConfig;
    private volatile FeatureConfig featureConfigTemp;
    private volatile MtlConfig mtlConfig;
    private volatile MtlConfig mtlConfigTemp;
    private volatile MtlConfigV2 mtlConfigV2;
    private volatile MtlConfigV2 mtlConfigV2Temp;
    private volatile Vocabulary vocabulary;
    private volatile Vocabulary vocabularyTemp;
    private volatile Buckets buckets;
    private volatile Buckets bucketsTemp;
    private FileChangeSubject fileChangeSubject;

    public static void warmUp() {
    }

    private LocalDNNModel(String modelKey) {
        long start = System.currentTimeMillis();
        try {
            LOG.info("dnn load from {} start ...", modelKey);
            Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
            this.modelFile = userNewsConfig.getString(modelKey);
            init(modelKey);
            if (dnnModelTemp != null) {
                dnnModel = dnnModelTemp;
            }
            if (dnnModelTemp2 != null) {
                dnnModel2 = dnnModelTemp2;
            }
            featureConfig = featureConfigTemp;
            mtlConfig = mtlConfigTemp;
            mtlConfigV2 = mtlConfigV2Temp;
            vocabulary = vocabularyTemp;
            buckets = bucketsTemp;
            File file = new File((modelFile));
            String watchDir = file.getParent();
            String watchFile = file.getName();

            fileChangeSubject = new FileChangeSubject(watchDir, watchFile);
            fileChangeSubject.addListener(new LocalDNNModel.ReloadModelListener());
        } catch (IOException e) {
            LOG.error("Create model from {} failed!", modelKey, e);
        }
        LOG.info("Create model from {} end. {}ms", modelKey, (System.currentTimeMillis() - start));
    }

    public DNNModel getDnnModel() {
        return dnnModel;
    }

    public DNNModel2 getDnnModel2() {
        return dnnModel2;
    }

    public FeatureConfig getFeatureConfig() {
        return this.featureConfig;
    }

    public MtlConfig getMtlConfig() {
        return this.mtlConfig;
    }

    public MtlConfigV2 getMtlConfigV2() {
        return this.mtlConfigV2;
    }

    public Vocabulary getVocabulary() {
        return this.vocabulary;
    }

    public Buckets getBuckets() {
        return this.buckets;
    }

    private void init(String modelKey) {
        File mf = new File(this.modelFile);
        if (mf.exists()) {

            if (modelKey.equals("dnn-model-rlvt")) {
                dnnModelTemp = new DNNModel(this.modelFile, "serve");
            } else {
                dnnModelTemp2 = new DNNModel2(this.modelFile, "serve");
            }
        }

        String featureConfigFilePath = String.join("/", this.modelFile, this.featureConfigFileName);
        File cf = new File(featureConfigFilePath);
        if (cf.exists()) {
            featureConfigTemp = new FeatureConfig();
            if (featureConfigTemp.loadFromLocal(featureConfigFilePath)) {
                LOG.info("load FeatureConfig success||path : {} ", featureConfigFilePath);
            }
        }

        String mtlConfigFilePath = String.join("/", this.modelFile, this.mtlConfigFileName);
        File mcf = new File(mtlConfigFilePath);
        if (mcf.exists()) {
            mtlConfigV2Temp = new MtlConfigV2();
            try {
                if (mtlConfigV2Temp.load(mtlConfigFilePath)) {
                    LOG.info("load MtlConfigV2 success||path : {} ", mtlConfigFilePath);
                }
            } catch (Exception e) {
                LOG.error("load MtlConfigV2 error", e);
            }
//            if (modelKey.endsWith("model") || modelKey.endsWith("model2")) {
//            } else {
//                mtlConfigTemp = new MtlConfig();
//                try {
//                    if (mtlConfigTemp.load(mtlConfigFilePath)) {
//                        LOG.info("load MtlConfig success||path : {} ", mtlConfigFilePath);
//                    }
//                } catch (Exception e) {
//                    LOG.error("load MtlConfig error", e);
//                }
//            }
        }

        String bucketFilePath = String.join("/", this.modelFile, this.bucketFileName);
        File bf = new File(bucketFilePath);
        if (bf.exists()) {
            bucketsTemp = new Buckets();
            try {
                if (bucketsTemp.load(bucketFilePath)) {
                    LOG.info("load Buckets success||path : {} ", bucketFilePath);
                }
            } catch (Exception e) {
                LOG.error("load Buckets error", e);
            }
        }

        String vocabFilePath = String.join("/", this.modelFile, this.vocabFileName);
        File vf = new File(vocabFilePath);
        if (vf.exists()) {
            vocabularyTemp = new Vocabulary();
            try {
                if (vocabularyTemp.load(vocabFilePath)) {
                    LOG.info("locad Vocabulary success||path : {} ", vocabFilePath);
                }
            } catch (Exception e) {
                LOG.error("load Vocabulary error", e);
            }
        }
    }

    private class ReloadModelListener implements FileChangedListener {
        @Override
        public void onChanged(FileChangedEvent event) {
            String fileName = event.getFileName();
            LOG.warn("File name={} has changed, begin reload model...", fileName);
            init(fileName.trim());
            for (int j = 0; j < 100; j++) {
                JSONObject user = FeatureExtractor.buildRandomIdfeatures(featureConfigTemp, 0);
                List<JSONObject> items = Lists.newArrayList();
                for (int i = 0; i < 100; i++) {
                    items.add(FeatureExtractor.buildRandomIdfeatures(featureConfigTemp, 1));
                }
                long start = System.currentTimeMillis();
                if (dnnModelTemp != null) {
                    dnnModelTemp.predict(items, user, featureConfigTemp);
                }
                if (dnnModelTemp2 != null) {
                    dnnModelTemp2.predict(items, user, featureConfigTemp, mtlConfigV2Temp.size(), "warmup");
                }
                long end = System.currentTimeMillis();
                LOG.info("dnn from model {} 第 {} 轮 warmup 耗时 {}ms", modelFile, j, end - start);
                if (end - start < 100) {
                    break;
                }
            }
            if (dnnModelTemp != null) {
                DNNModel closeModel = dnnModel;
                dnnModel = dnnModelTemp;
                closeModel.close();
            }
            if (dnnModelTemp2 != null) {
                DNNModel2 closeModel2 = dnnModel2;
                dnnModel2 = dnnModelTemp2;
                closeModel2.close();
            }
            featureConfig = featureConfigTemp;
            mtlConfig = mtlConfigTemp;
            mtlConfigV2 = mtlConfigV2Temp;
            vocabulary = vocabularyTemp;
            buckets = bucketsTemp;
            LOG.warn("Successful load dnn model from {}.", modelFile);
        }
    }
}
