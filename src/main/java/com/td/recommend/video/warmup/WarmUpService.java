package com.td.recommend.video.warmup;

import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.response.NewsDoc;
import com.td.recommend.commons.thread.FixedNumConcurrentTaskExecutor;
import com.td.recommend.video.api.servlet.VideoRecommendContextBuilder;
import com.td.recommend.video.concurrent.ApplicationSharedExecutorService;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.service.VideoRecommendService;
import com.td.recommend.video.utils.OpenTsdbMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by admin on 2017/8/18.
 */
public class WarmUpService {
    private static final Logger LOG = LoggerFactory.getLogger(WarmUpService.class);

    private File warmUpFile;
    private int warmUpLogNum;

    public WarmUpService(String warmUpFile, int warmUpLogNum) {
        this.warmUpFile = new File(warmUpFile);
        this.warmUpLogNum = warmUpLogNum;
    }

    public void warmUp() {
        OpenTsdbMetrics.initMetrics();
//        Optional<String> warmUpLogFileOpt = getWarmUpLogFile();
        if (! warmUpFile.exists()) {
            LOG.error("Warmup file={} not exists!", warmUpFile.getAbsoluteFile());
            return;
        }

        List<String> warmUpURLs = getWarmUpURLs(warmUpFile);

        FixedNumConcurrentTaskExecutor.TaskFactory<String, List<NewsDoc>> taskFactory = url -> new WarmUpTask(url);

        FixedNumConcurrentTaskExecutor<String, List<NewsDoc>> executor =
                new FixedNumConcurrentTaskExecutor<>(ApplicationSharedExecutorService.getInstance().getExecutorService(), 10, true);

        executor.execute(warmUpURLs, taskFactory);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            LOG.warn("sleep interrupted", e);
        }
    }

    public void warmUpThrift() {
        OpenTsdbMetrics.initMetrics();
//        Optional<String> warmUpLogFileOpt = getWarmUpLogFile();
        if (! warmUpFile.exists()) {
            LOG.error("Warmup file={} not exists!", warmUpFile.getAbsoluteFile());
            return;
        }

        List<String> warmUpURLs = getWarmUpThriftURLs(warmUpFile);

        FixedNumConcurrentTaskExecutor.TaskFactory<String, List<NewsDoc>> taskFactory = url -> new WarmUpThriftTask(url);

        FixedNumConcurrentTaskExecutor<String, List<NewsDoc>> executor =
            new FixedNumConcurrentTaskExecutor<>(ApplicationSharedExecutorService.getInstance().getExecutorService(), 10, true);

        executor.execute(warmUpURLs, taskFactory);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.warn("sleep interrupted", e);
        }
    }

    private List<String> getWarmUpThriftURLs(File warmUpFile) {
        List<String> warmUpUrls = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(warmUpFile), "UTF-8"))) {

            String line;
            while((line = reader.readLine()) != null) {
                Optional<String> urlOpt = WarmUpLogParser.parseThriftURL(line);
                if (urlOpt.isPresent()) {
                    String url = urlOpt.get();
                    if (url.startsWith("/video/recommend")) {
                        warmUpUrls.add(url);
                    }

                    if (warmUpUrls.size() >= warmUpLogNum) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Get video warmup urls failed!", e);
        }

        return warmUpUrls;
    }


    public static class WarmUpTask implements Callable<List<NewsDoc>> {
        private String warmUpUrl;

        public WarmUpTask(String warmUpUrl) {
            this.warmUpUrl = warmUpUrl;
        }

        @Override
        public List<NewsDoc> call() throws Exception {
            LOG.info("warmUp with url={}", warmUpUrl);
            WarmUpHttpServletRequest req = new WarmUpHttpServletRequest(warmUpUrl);
            req.setParamMap("debug", "true");
            VideoRecommendContextBuilder userNewsRecommendContextBuilder = VideoRecommendContextBuilder.getInstance();
            VideoRecommenderContext recommendContext = userNewsRecommendContextBuilder.build(req);
            VideoRecommendService recommendService = new VideoRecommendService(recommendContext);
            return recommendService.recommend();
        }
    }


    public static class WarmUpThriftTask implements Callable<List<NewsDoc>> {
        private String warmUpUrl;

        public WarmUpThriftTask(String warmUpUrl) {
            this.warmUpUrl = warmUpUrl;
        }

        @Override
        public List<NewsDoc> call() throws Exception {
//            RecommendRequest recommendRequest = buildRecommendRequest(warmUpUrl);
//            VideoRecommendServiceImpl recommendService = new VideoRecommendServiceImpl();
//            RecommendResponse response = recommendService.recommend(recommendRequest);
//
//            int status = response.getStatus();
//            LOG.info("warmup with url={} get status={}", warmUpUrl, status);

            return Collections.emptyList();
        }
    }

    private List<String> getWarmUpURLs(File warmUpFile) {
        List<String> warmUpUrls = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(warmUpFile), "UTF-8"))) {

            String line;
            while((line = reader.readLine()) != null) {
                Optional<String> urlOpt = WarmUpLogParser.parseURL(line);
                if (urlOpt.isPresent()) {
                    String url = urlOpt.get();
                    if (url.startsWith("/video/recommend")) {
                        warmUpUrls.add(url);
                    }

                    if (warmUpUrls.size() >= warmUpLogNum) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Get warmup urls failed!", e);
        }

        return warmUpUrls;
    }

    public static RecommendRequest buildRecommendRequest(String warmUpUrl) {
        RecommendRequest recommendRequest = new RecommendRequest();
        Map<String, String> paramMap = new HashMap<>();

        String[] fields = warmUpUrl.split("\\?");
        if (fields.length < 2) {
            return null;
        }

        String params = fields[1];
        String[] paramArray = params.split("&");
        for (String param : paramArray) {
            String[] pair = param.split("=");
            if (pair.length >= 2) {
                paramMap.put(pair[0], pair[1]);
            }
        }

        //recommendRequest.setParams(paramMap);

        return recommendRequest;
    }

//    public Optional<String> getWarmUpLogFile() {
//        File file = new File();
//        if (file.exists()) {
//            return Optional.of(file.getAbsolutePath());
//        } else {
//            return Optional.empty();
//        }

//        String[] warmUpLogFiles = this.accessLogPath.list((dir, name) -> {
//            LocalDate date = LocalDate.now();
//            for (int i = 0; i < 5; ++i) {
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
//                String datePart = formatter.format(date);
//                if (name.startsWith("jetty-" + datePart)) {
//                    return true;
//                }
//
//                date = date.minus(1, ChronoUnit.DAYS);
//            }
//            return false;
//        });
//
//        if (warmUpLogFiles != null && warmUpLogFiles.length > 0) {
//            return Optional.of(this.accessLogPath.getAbsolutePath() + File.separator + warmUpLogFiles[0]);
//        } else {
//            return Optional.empty();
//        }
//    }

//    public static void main(String[] args) {
//        WarmUpService warmUpService = new WarmUpService("/Users/frang/data/logs");
//        warmUpService.warmUp();
//    }
}
