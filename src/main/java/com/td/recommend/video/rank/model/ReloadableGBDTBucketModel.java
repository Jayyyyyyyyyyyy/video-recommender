package com.td.recommend.video.rank.model;

import com.codahale.metrics.Histogram;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.io.Files;
import com.td.recommend.commons.env.Env;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.rank.model.GBDTModel2;
import com.td.recommend.commons.rank.model.PredictModel;
import com.td.recommend.video.abtest.BucketConstants;
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
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReloadableGBDTBucketModel {

  private final static Logger logger = LoggerFactory.getLogger(ReloadableGBDTBucketModel.class);

  private ConcurrentHashMap<String, PredictModel> bucketModel = new ConcurrentHashMap<>();

  @Getter
  private CuratorFramework client;
  private String watchPath;
  private File backupModelFile;

  private ReloadableGBDTBucketModel() {
    Config rootConfig = UserVideoConfig.getInstance().getRootConfig();

    Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
    backupModelFile = new File(userNewsConfig.getString("model-file"));

    File localBackupDir = backupModelFile.getParentFile();
    if (!localBackupDir.exists()) {
      localBackupDir.mkdirs();
    }

    Config config = rootConfig.getConfig("hdfs-model");
    watchPath = config.getString("watch-path");
    while (true) {
      try {
        initClient(config);
        startListener(client, watchPath);
        break;
      } catch (Exception e) {
        logger.error("", e);
        close();
      }
    }
    logger.info("start auto checkout model server: watch path:{}, zk:{}",
            config.getString("watch-path"), config.getString("zookeeper-address"));
  }

  public void startListener(CuratorFramework client, String ZK_PATH) throws Exception {
    TreeCache treeCache = new TreeCache(client, ZK_PATH);
    //设置监听器和处理过程
    treeCache.getListenable().addListener(new TreeCacheListener() {
      @Override
      public void childEvent(CuratorFramework client, TreeCacheEvent event) {
        try {
          ChildData data = event.getData();
          if (data == null || data.getData() == null) {
            logger.info("path:{}, data is null : {}", ZK_PATH, event.getType());
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
          logger.error("", e);
        }
      }
    });
    //开始监听
    treeCache.start();
    logger.info("start zk listener...");
  }


  public void processUpdate(ChildData data) {
    if (data.getData().length < 1) {
      return;
    }

    String bucketName = FilenameUtils.getName(data.getPath());
    logger.info("start model checkout,data path:{}",data.getPath());
    String hdfsModelFile = StringUtils.toEncodedString(data.getData(), Charset.forName("UTF-8"));

    String localModelName = FilenameUtils
        .concat(FileUtils.getTempDirectoryPath(), bucketName + "_" + FilenameUtils.getName(hdfsModelFile));

    logger.info("init tmp path:{}", localModelName);

    Configuration conf = new Configuration();
    conf.set("fs.hdfs.impl",
        org.apache.hadoop.hdfs.DistributedFileSystem.class.getName()
    );
    conf.set("fs.file.impl",
        LocalFileSystem.class.getName()
    );

    Config rootConfig = UserVideoConfig.getInstance().getRootConfig();

    if (Env.isWuXi(rootConfig)) {
      System.setProperty("java.security.krb5.realm", "WIFI.COM");
      System.setProperty("java.security.krb5.kdc", "10.2.13.1");
      conf.set("keytab.file", "/home/halo_op/tjonline.keytab");
      conf.set("hbase.security.authentication", "kerberos");
      conf.set("hadoop.security.authentication", "kerberos");

      try {
        UserGroupInformation.loginUserFromKeytab("tjonline@WIFI.COM", "/home/halo_op/tjonline.keytab");
      } catch (IOException e) {
        logger.error("login failed!", e);
      }
    }

    while (true) {
      try {
        FileSystem fs = FileSystem.get(conf);
        File modelFile = new File(localModelName);
        if (modelFile.exists()) {
          modelFile.deleteOnExit();
        }

        if (fs.isFile(new Path(hdfsModelFile))) {
          LocalFileSystem localFileSystem = LocalFileSystem.getLocal(conf);

            logger.info("copy model from hdfs={} to local={}", hdfsModelFile, localModelName);

          boolean copy = FileUtil.copy(fs, new Path(hdfsModelFile), localFileSystem,
                  new Path(localModelName), false, conf);
          if (copy) {
            Files.copy(new File(localModelName), backupModelFile);
          } else {
            logger.error("copy file fa");
          }
        } else {
          logger.warn("not found hdfs modelFile:{}", hdfsModelFile);
        }
        break;
      } catch (IOException e) {
        logger.error("", e);
      }

      try {
        Thread.sleep(5000L);
      } catch (InterruptedException e) {
        logger.error("", e);
      }
    }

    GBDTModel2 gbdtModel = null;
    try {
      gbdtModel = new GBDTModel2(localModelName);
    } catch (Exception e) {
      logger.error("Create GBDTModel from modelFile={}, failed!", localModelName, e);
    }

    if (gbdtModel != null) {
      bucketModel.put(bucketName, gbdtModel);
    }

    logger.info("bucket:{} update local tmp model path: {}, "
            + "hdfs model source:{}", bucketName, localModelName, hdfsModelFile);

    logger.info("current bucket:{}", bucketModel.entrySet().stream().map(x -> x.getKey()).collect(
        Collectors.toList()));
  }

  public void processRemove(ChildData data) {
    String bucket = FilenameUtils.getName(data.getPath());
    logger.info("remove model bucket:{}", bucket);
    bucketModel.remove(bucket);
  }

  public void processAdded(ChildData data) {
    logger.info("add model bucket:{},path:{}", FilenameUtils.getName(data.getPath()),data.getPath());
    processUpdate(data);
  }




  public void initClient(Config config) {
    client = CuratorFrameworkFactory.newClient(
        config.getString("zookeeper-address"),
        new RetryNTimes(100, 10000)
    );
    client.start();
  }

  public void close() {
    CloseableUtils.closeQuietly(this.client);
  }

  public PredictModel getGBDTModel(String bucket) {
    TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
    Histogram histogram = taggedMetricRegistry.histogram("usernews.zkmodel.errorate");

    if (bucketModel.containsKey(bucket)) {
      histogram.update(0);
      return bucketModel.get(bucket);
    } else {
      histogram.update(100);
    }

    logger.error("bucket model:[{}] not found...", bucket);

    return LocalGBDTModel.getInstance().getGBDTModel();
  }

  private static final ReloadableGBDTBucketModel instance = new ReloadableGBDTBucketModel();

  public static final ReloadableGBDTBucketModel getInstance() {
    return instance;
  }

  public static void main(String[] args) throws Exception {


    ReloadableGBDTBucketModel model = ReloadableGBDTBucketModel.getInstance();
    for (int i = 0; i < 200; i++) {
      PredictModel gbdtModel = model.getGBDTModel(BucketConstants.MODEL_EXP);
      Thread.sleep(100);
      System.out.println(gbdtModel);
    }

//    Thread.sleep(Long.MAX_VALUE);
//    GBDTModel model1 = model.getGBDTModel("default");
//    CuratorFramework cf = model.client;
//    cf.create().creatingParentsIfNeeded().forPath("/puma/model/default1", "hdfs://ns-tj/puma/model/version/gbdt/20170829/gdbt_0100.model".getBytes());
  }

}
