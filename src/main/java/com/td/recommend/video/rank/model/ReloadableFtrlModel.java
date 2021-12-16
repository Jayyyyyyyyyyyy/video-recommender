package com.td.recommend.video.rank.model;

import com.alibaba.fastjson.JSONObject;
import com.google.common.io.Files;
import com.td.rank.deepfm.felib.*;
import com.td.recommend.commons.rank.model.DNNModel;
import com.td.recommend.commons.rank.model.DNNModel2;
import com.td.recommend.commons.rank.model.FtrlModel;
import com.td.recommend.video.utils.UserVideoConfig;
import com.typesafe.config.Config;
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
import java.util.stream.Collectors;

public class ReloadableFtrlModel {
    private static final Logger LOG = LoggerFactory.getLogger(ReloadableFtrlModel.class);

    private CuratorFramework client;
    private String watchPath;
    private File backupModelFile;
    private LocalFtrlModel localmodel;

    public static final ReloadableFtrlModel ftrl_base = new ReloadableFtrlModel("ftrl-base", "hdfs-ftrl-base-model", LocalFtrlModel.ftrl_base);


    public ReloadableFtrlModel(String strategy, String hdfskey, LocalFtrlModel localFtrlModel) {
        Config rootConfig = UserVideoConfig.getInstance().getRootConfig();
        Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
        backupModelFile = new File(userNewsConfig.getString(strategy));
        this.localmodel = localFtrlModel;
        File localBackupDir = backupModelFile.getParentFile();
        if (!localBackupDir.exists()) {
            localBackupDir.mkdirs();
        }

        Config config = rootConfig.getConfig(hdfskey);
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

        boolean isFirst = false;
        String bucketName = FilenameUtils.getName(data.getPath());
        LOG.info("start model checkout,data path:{},data:{}", data.getPath(),data.getData());
        String hdfsModelFile = StringUtils.toEncodedString(data.getData(), Charset.forName("UTF-8"));
//        try {
//            JSONObject hdfsModelFileObj = JSONObject.parseObject(hdfsModelFile);
//            hdfsModelFile = hdfsModelFileObj.getString("path");
//        } catch (Exception e) {
//            LOG.warn("dnn hdfsModelFile is not json");
//        }

        String modefile = localmodel.getModelFile();
        File file = new File(modefile);
        if (!file.exists() || !file.isFile()) {
            isFirst = true;
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

                if (fs.isFile(new Path(hdfsModelFile))) {
                    LocalFileSystem localFileSystem = LocalFileSystem.getLocal(conf);

                    LOG.info("copy model from hdfs={} to local={}", hdfsModelFile, localModelName);

                    boolean copy = FileUtil.copy(fs, new Path(hdfsModelFile), localFileSystem,
                            new Path(localModelName), false, conf);
                    if (copy) {
                        Files.copy(new File(localModelName), backupModelFile);
                    } else {
                        LOG.error("copy file fa");
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
        if (isFirst) {
            localmodel.reloadModeFile("ftrl-base");
            LOG.info("first load ftrl model.");
        }





    }

    public void processRemove(ChildData data) {
        String bucket = FilenameUtils.getName(data.getPath());
        LOG.info("remove model bucket:{}", bucket);
    }

    public void close() {
        CloseableUtils.closeQuietly(this.client);
    }


    public FtrlModel getFtrlModel() {
        return localmodel.getFtrlModel();
    }

    public LocalFtrlModel getLocalmodel() {
        return this.localmodel;
    }
}
