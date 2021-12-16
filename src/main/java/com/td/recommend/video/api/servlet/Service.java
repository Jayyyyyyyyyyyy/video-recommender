package com.td.recommend.video.api.servlet;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.td.recommend.video.rank.model.LocalDNNModel;
import com.td.recommend.video.rank.model.ReloadableDNNModel;
import com.td.recommend.video.rerank.model.LocalRerankDNNModel;
import com.td.recommend.video.rerank.model.ReloadableRerankDNNModel;
import com.td.recommend.video.utils.JvmPauseMonitor;
import com.td.recommend.video.utils.LogCleaner;
import com.td.recommend.video.utils.OpenTsdbMetrics;
import com.td.recommend.video.utils.UserUvMonitor;
import com.td.recommend.video.warmup.WarmUpService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Service {
    private static final Logger LOG = LoggerFactory.getLogger(Service.class);
    private static final String DEFAULT_LOG_PATH = "../logs";

    @Parameter(names="-dubboPort")
    private int dubboPort = 8089;
    @Parameter(names="-port")
    private int port = 8088;
    @Parameter(names="-config")
    private String config;
    private boolean warmup = true;

    private JvmPauseMonitor jvmPauseMonitor;

    public void run() {
        QueuedThreadPool pool = new QueuedThreadPool(400, 50);
        Server server = new Server(pool);
        ServerConnector connector=new ServerConnector(server);
        connector.setIdleTimeout(10000L);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        server.setStopAtShutdown(true);


        //Server server = new Server(port);
        ServletContextHandler service = new ServletContextHandler(ServletContextHandler.SESSIONS);
        service.setContextPath("/video");
        ServletHolder recommendHolder = new ServletHolder(new VideoRecommendServlet());
        recommendHolder.setInitParameter("config", config);
        service.addServlet(recommendHolder, "/recommend");

        ServletHolder rerankDebugHolder =  new ServletHolder(new VideoRerankDebugServlet());
        service.addServlet(rerankDebugHolder, "/rerankDebug");


        ServletContextHandler root = new ServletContextHandler(ServletContextHandler.SESSIONS);
        root.setContextPath("/");

        ServletHolder healthHolder =  new ServletHolder(new VideoRecommendHealthServlet());
        healthHolder.setInitParameter("port", String.valueOf(port));
        root.addServlet(healthHolder, "/health");

        ContextHandler pages = new ContextHandler();
        pages.setContextPath("/static");
        ResourceHandler handler = new ResourceHandler();
        handler.setBaseResource(JarResource.newClassPathResource("."));
        handler.setDirectoriesListed(true);
        pages.setHandler(handler);

        RequestLogHandler requestLogHandler = new RequestLogHandler();


        String logPath = getLogPath();
        LOG.info("logPath: {}", logPath);

        NCSARequestLog requestLog = new NCSARequestLog(logPath + "/jetty-yyyy_MM_dd.request.log");
        requestLog.setFilenameDateFormat("yyyy_MM_dd");
        requestLog.setRetainDays(90);
        requestLog.setAppend(true);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT+8");

        requestLogHandler.setRequestLog(requestLog);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.addHandler(service);
        contexts.addHandler(pages);
        contexts.addHandler(root);

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{contexts, new DefaultHandler(), requestLogHandler});

        server.setHandler(handlers);

        LogCleaner.getInstance().cleanLogs(logPath);
        OpenTsdbMetrics.initMetrics();
        UserUvMonitor.init();
        initConfig();
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
                WarmUpService warmUpService = new WarmUpService("/tmp/warmup.log", 500);
                warmUpService.warmUp();
            }
        } catch (Exception e) {
            LOG.warn("Warmup failed!", e);
        } finally {
            LOG.info("warm up used timeInMills={}", System.currentTimeMillis() - beginTime);
        }

        try {
            server.start();
        } catch (Exception e) {
            LOG.error("Start server failed!", e);
        }

        LOG.info("Service Started");

        try {
            server.join();
        } catch (InterruptedException e) {
            LOG.error("", e);
        }

        LOG.info("Service End");
    }

    void initConfig() {
        Config config = ConfigFactory.load();
        warmup = config.getConfig("server").getBoolean("warmup");
    }


    private String getLogPath() {
        String logPath = System.getenv("jetty.logs");
        if (logPath == null) {
            logPath = DEFAULT_LOG_PATH;
        }
        return logPath;
    }


    public static void main(String[] args) {
        File logConfig = new File("log4j.properties");
        if (logConfig.exists()) {
            System.out.println("User config " + logConfig);
            PropertyConfigurator.configure(logConfig.toString());
        }
        Service service = new Service();
        JCommander jcommander = new JCommander(service);
        jcommander.parse(args);
        service.run();
    }
}

