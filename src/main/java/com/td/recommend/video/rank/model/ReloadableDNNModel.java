package com.td.recommend.video.rank.model;

import com.alibaba.fastjson.JSONObject;
import com.td.rank.deepfm.felib.*;
import com.td.recommend.commons.rank.model.DNNModel;
import com.td.recommend.commons.rank.model.DNNModel2;
import com.td.recommend.video.utils.UserVideoConfig;
import com.typesafe.config.Config;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zhanghongtao
 */
public class ReloadableDNNModel {

    private static final Logger LOG = LoggerFactory.getLogger(ReloadableDNNModel.class);

    private ConcurrentHashMap<String, DNNModel> bucketModel = new ConcurrentHashMap<>();

    @Getter
    private CuratorFramework client;
    private String watchPath;
    private File backupModelFile;
    private LocalDNNModel localmodel;
    public static final ReloadableDNNModel feed = new ReloadableDNNModel("dnn-model", "hdfs-dnn-model", LocalDNNModel.feed);
    public static final ReloadableDNNModel feed2 = new ReloadableDNNModel("dnn-model2", "hdfs-dnn-model2", LocalDNNModel.feed2);
    public static final ReloadableDNNModel feed3 = new ReloadableDNNModel("dnn-model3", "hdfs-dnn-model3", LocalDNNModel.feed3);
    public static final ReloadableDNNModel rlvt = new ReloadableDNNModel("dnn-model-rlvt", "hdfs-dnn-rlvt", LocalDNNModel.rlvt);
    public static final ReloadableDNNModel rlvt2 = new ReloadableDNNModel("dnn-model-rlvt2", "hdfs-dnn-rlvt2", LocalDNNModel.rlvt2);

    public static void warmUp() { }

    private ReloadableDNNModel(String localKey, String hdfsKey, LocalDNNModel localmodel) {
        Config rootConfig = UserVideoConfig.getInstance().getRootConfig();
        Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
        backupModelFile = new File(userNewsConfig.getString(localKey));
        this.localmodel = localmodel;
        File localBackupDir = backupModelFile.getParentFile();
        if (!localBackupDir.exists()) {
            localBackupDir.mkdirs();
        }

        Config config = rootConfig.getConfig(hdfsKey);
        watchPath = config.getString("watch-path");
        while (true) {
            try {
                initClient(config);
                startListener(client, watchPath);
                break;
            } catch (Exception e) {
                LOG.error("", e);
                close();
            }
        }
        LOG.info("start auto checkout model server: watch path:{}, zk:{}", config.getString("watch-path"), config.getString("zookeeper-address"));
    }

    public void initClient(Config config) {
        client = CuratorFrameworkFactory.newClient(
                config.getString("zookeeper-address"),
                new RetryNTimes(100, 10000)
        );
        client.start();
    }

    public void startListener(CuratorFramework client, String ZK_PATH) throws Exception {

        TreeCache treeCache = new TreeCache(client, ZK_PATH);
        //设置监听器和处理过程
        treeCache.getListenable().addListener((curatorFramework, event) -> {
            try {
                ChildData data = event.getData();
                if (data == null || data.getData() == null) {
                    LOG.info("path:{}, data is null : {}", ZK_PATH, event.getType());
                    return;
                }

                if (FilenameUtils.equals(watchPath, FilenameUtils.getPath(data.getPath()))) {
                    return;
                }

                switch (event.getType()) {
                    case NODE_ADDED:
                        processAdded(data);
                        break;
                    case NODE_REMOVED:
                        processRemove(data);
                        break;
                    case NODE_UPDATED:
                        processUpdate(data);
                        break;
                    default:
                        break;
                }

            } catch (Exception e) {
                LOG.error("", e);
            }
        });
        //开始监听
        treeCache.start();
        LOG.info("start zk listener...");
    }

    public void processAdded(ChildData data) {
        LOG.info("add model bucket:{},path:{}", FilenameUtils.getName(data.getPath()), data.getPath());
        processUpdate(data);
    }

    public void processUpdate(ChildData data) {
        if (data.getData().length < 1) {
            return;
        }

        String bucketName = FilenameUtils.getName(data.getPath());
        LOG.info("start model checkout,data path:{}", data.getPath());
        String hdfsModelFile = StringUtils.toEncodedString(data.getData(), Charset.forName("UTF-8"));
        try {
            JSONObject hdfsModelFileObj = JSONObject.parseObject(hdfsModelFile);
            hdfsModelFile = hdfsModelFileObj.getString("path");
        } catch (Exception e) {
            LOG.warn("dnn hdfsModelFile is not json");
        }

        String localModelName = FilenameUtils.concat(FileUtils.getTempDirectoryPath(), bucketName + "_" + hdfsModelFile.hashCode());

        LOG.info("init tmp path:{}", localModelName);

        Configuration conf = new Configuration();
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", LocalFileSystem.class.getName());

        while (true) {
            try {
                FileSystem fs = FileSystem.get(conf);
                File modelFile = new File(localModelName);
                if (modelFile.exists()) {
                    modelFile.deleteOnExit();
                }

                if (fs.isDirectory(new Path(hdfsModelFile))) {
                    LocalFileSystem localFileSystem = LocalFileSystem.getLocal(conf);
                    LOG.info("copy model from hdfs={} to local={}", hdfsModelFile, localModelName);
                    FileUtils.deleteDirectory(new File(localModelName));
                    boolean copy = FileUtil.copy(fs, new Path(hdfsModelFile), localFileSystem, new Path(localModelName), false, conf);
                    if (copy) {
                        FileUtils.copyDirectory(new File(localModelName), backupModelFile);
                        LOG.info("copy model from local {} to local {}", localModelName, backupModelFile);
                    } else {
                        LOG.error("copy file failed");
                    }
                } else {
                    LOG.warn("not found hdfs modelFile:{}", hdfsModelFile);
                }
                break;
            } catch (IOException e) {
                LOG.error("", e);
            }

            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                LOG.error("", e);
            }
        }

        DNNModel dnnModel = null;
        try {
            dnnModel = new DNNModel(localModelName, "serve");
        } catch (Exception e) {
            LOG.error("Create DNNTModel from modelFile={}, failed!", localModelName, e);
        }

        if (dnnModel != null) {
            bucketModel.put(bucketName, dnnModel);
        }
        LOG.info("bucket:{} update local tmp model path: {}, " + "hdfs model source:{}", bucketName, localModelName, hdfsModelFile);
        LOG.info("current bucket:{}", bucketModel.entrySet().stream().map(x -> x.getKey()).collect(Collectors.toList()));

    }

    public void processRemove(ChildData data) {
        String bucket = FilenameUtils.getName(data.getPath());
        LOG.info("remove model bucket:{}", bucket);
        bucketModel.remove(bucket);
    }

    public void close() {
        CloseableUtils.closeQuietly(this.client);
    }


    public DNNModel getDNNModel() {
        return localmodel.getDnnModel();
    }

    public DNNModel2 getDNNModel2() {
        return localmodel.getDnnModel2();
    }

    public FeatureConfig getFeatureConfig() {
        return localmodel.getFeatureConfig();
    }

    public MtlConfig getMtlConfig() {
        return localmodel.getMtlConfig();
    }
    public MtlConfigV2 getMtlConfigV2() {
        return localmodel.getMtlConfigV2();
    }

    public Vocabulary getVocabulary() {
        return localmodel.getVocabulary();
    }

    public Buckets getBuckets() {
        return localmodel.getBuckets();
    }
}
