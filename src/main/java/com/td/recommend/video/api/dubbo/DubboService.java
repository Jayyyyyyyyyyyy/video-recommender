package com.td.recommend.video.api.dubbo;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.td.recommend.video.api.VideoRecommend;
import com.td.recommend.video.api.servlet.Service;
import com.td.recommend.video.rank.model.LocalDNNModel;
import com.td.recommend.video.rank.model.ReloadableDNNModel;
import com.td.recommend.video.rerank.model.LocalRerankDNNModel;
import com.td.recommend.video.rerank.model.ReloadableRerankDNNModel;
import com.td.recommend.video.utils.JvmPauseMonitor;
import com.td.recommend.video.utils.LogCleaner;
import com.td.recommend.video.utils.OpenTsdbMetrics;
import com.td.recommend.video.warmup.dubbo.WarmUpService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.dubbo.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanghongtao
 */
public class DubboService {
    private static final Logger LOG = LoggerFactory.getLogger(DubboService.class);
    private static final String DEFAULT_LOG_PATH = "../logs";

    @Parameter(names="-dubboPort")
    private int dubboPort = 8089;
    @Parameter(names="-port")
    private int port = 8088;
    @Parameter(names="-coreThreads")
    private int coreThreads = 200;
    private boolean warmup = true;
    private String nacosHost = "nacos://10.42.101.14:8848";
    private String serverName = "videoRecommend";

    private JvmPauseMonitor jvmPauseMonitor;

    public void run(){
        String logPath = getLogPath();
        LOG.info("logPath: {}", logPath);

        initConfig();

        VideoRecommend recommend = new VideoRecommendImpl(); // 视频推荐执行

        ApplicationConfig application = new ApplicationConfig();
        application.setName(serverName);

        // 连接注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress(nacosHost);

        MetadataReportConfig metadataReport = new MetadataReportConfig();
        metadataReport.setAddress(nacosHost);

        ConfigCenterConfig configCenter = new ConfigCenterConfig();
        configCenter.setAddress(nacosHost);

        // 服务提供者协议配置
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("dubbo");
        protocol.setPort(dubboPort);
        protocol.setCorethreads(coreThreads);

        // 注意：ServiceConfig为重对象，内部封装了与注册中心的连接，以及开启服务端口
        // 服务提供者暴露服务配置
        // 此实例很重，封装了与注册中心的连接，请自行缓存，否则可能造成内存和连接泄漏
        ServiceConfig<VideoRecommend> service = new ServiceConfig();
        service.setApplication(application);
        // 多个注册中心可以用setRegistries()
        service.setRegistry(registry);
        service.setMetadataReportConfig(metadataReport);
        service.setConfigCenter(configCenter);
        // 多个协议可以用setProtocols()
        service.setProtocol(protocol);
        service.setInterface(VideoRecommend.class); // 参数
        service.setRef(recommend); //视频推荐执行
        service.setVersion("1.0.0");
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setGroup("dubbo");
        service.setProvider(providerConfig);

        LogCleaner.getInstance().cleanLogs(logPath);
        OpenTsdbMetrics.initMetrics();
        jvmPauseMonitor = new JvmPauseMonitor();
        jvmPauseMonitor.start();
        long beginTime = System.currentTimeMillis();
        //List<String> mp3s = HotSearchGetter.getTopProfileByKeyName("content_mp3.tagname");
        try {
            if(warmup) {
                ReloadableDNNModel.warmUp();
                LocalDNNModel.warmUp();
                ReloadableRerankDNNModel.warmUp();
                LocalRerankDNNModel.warmUp();
                LOG.info("warmup start ...");
                WarmUpService warmUpService = new WarmUpService("/tmp/warmup-dubbo.log", 500);
                warmUpService.warmUp();
                LOG.info("warmup end ...");
            }
        } catch (Exception e) {
            LOG.warn("Warmup failed!", e);
        } finally {
            LOG.info("warm up used timeInMills={}", System.currentTimeMillis() - beginTime);
        }

        // 暴露及注册服务
        service.export();
        System.out.println("videoRecommend is running.");
    }

    void initConfig() {
        Config config = ConfigFactory.load();
        warmup = config.getConfig("server").getBoolean("warmup");

        Config dubboConf = config.getConfig("dubbo");
        this.serverName = dubboConf.getString("server.name");
        this.coreThreads = dubboConf.getInt("coreThreads");
        this.nacosHost = dubboConf.getString("nacos.host");
    }

    private String getLogPath() {
        String logPath = System.getenv("dubbo.logs");
        if (logPath == null) {
            logPath = DEFAULT_LOG_PATH;
        }
        return logPath;
    }

    public static void main(String[] args) { //服务启动
//        File logConfig = new File("log4j.properties");
//        if (logConfig.exists()) {
//            System.out.println("User config " + logConfig);
//            PropertyConfigurator.configure(logConfig.toString());
//        }
        //启动dubbo
        DubboService dubboService = new DubboService();
        JCommander jcommander = new JCommander(dubboService);
        jcommander.parse(args);
        dubboService.run();

        //启动http
        Service jettyService = new Service();
        JCommander jettyJcommander = new JCommander(jettyService);
        jettyJcommander.parse(args);
        jettyService.run();
    }
}

