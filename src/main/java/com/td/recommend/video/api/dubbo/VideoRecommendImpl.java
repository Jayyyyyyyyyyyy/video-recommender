package com.td.recommend.video.api.dubbo;

import com.alibaba.fastjson.JSON;
import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.api.ApiResultBuilder;
import com.td.recommend.commons.json.JsonUtils;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.response.NewsDoc;
import com.td.recommend.video.api.VideoRecommend;
import com.td.recommend.video.api.vo.ExtInfoBuilder;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.service.VideoRecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VideoRecommendImpl implements VideoRecommend {
    private static final Logger LOG = LoggerFactory.getLogger(VideoRecommendImpl.class);
    private static final Logger REQUEST_LOG = LoggerFactory.getLogger("dubboRequest");

    @Override
    public String recommend(String diu, RecommendRequest recommendRequest, boolean isDebug) {
        // recommendRequest 请求信息
        String videos = "";
        try {
            TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
            String ihf = String.valueOf(recommendRequest.getIhf()); // important 用于区分业务 ihf.class
            taggedMetricRegistry.taggedMeter("dubbo.uservideo.recommend.qps", ImmutableMap.of("ihf", ihf)).mark();
            Timer timer = taggedMetricRegistry.taggedTimer("dubbo.uservideo.recommend.request.latency", ImmutableMap.of("ihf", ihf));
            Timer.Context time = timer.time();

            long beginTime = System.currentTimeMillis();
            String queryTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(beginTime));
            REQUEST_LOG.info(queryTime + "\t" + JSON.toJSONString(recommendRequest));

            VideoRecommendContextBuilder recommendContextBuilder = VideoRecommendContextBuilder.getInstance();
            VideoRecommenderContext recommendContext = recommendContextBuilder.build(recommendRequest, isDebug); //创建context实例，里面放着推荐之前所需要的各个资源，包括用户画像，现有的请求字段。如果是相关推荐，还包含看的vid信息
            VideoRecommendService recommendService = new VideoRecommendService(recommendContext);
            List<NewsDoc> result = recommendService.recommend();

            long latency = System.currentTimeMillis() - beginTime;
            Map<String, Object> extMap = ExtInfoBuilder.build(recommendContext, latency);
            LOG.info("dubbo request latency={} diu={} ihf={} ", latency, recommendRequest.getDiu(), recommendRequest.getIhf());
            time.stop();

            Map<String, Object> resultMap = ApiResultBuilder.create().success(result).build(); //build 返回结果，转成json
            resultMap.put("ext", extMap); //转json携带的一些信息
            Optional<String> resultJsonOpt = JsonUtils.toJson(resultMap);
            if (resultJsonOpt.isPresent()) {
                videos = resultJsonOpt.get();
            }
        } catch (Exception e) {
            Map<String, Object> resultMap = ApiResultBuilder.create().failure(e.getMessage()).build();
            Optional<String> jsonOpt = JsonUtils.toJson(resultMap);
            if (jsonOpt.isPresent()) {
                videos = jsonOpt.get();
            }
            LOG.error("recommend request failed!", e);
        }
        return videos;
    }
}
