package com.td.recommend.video.api.servlet;

import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.api.ApiResultBuilder;
import com.td.recommend.commons.json.JsonUtils;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.request.RequestParamHelper;
import com.td.recommend.commons.request.RequestParams;
import com.td.recommend.commons.response.NewsDoc;
import com.td.recommend.video.api.vo.ExtInfoBuilder;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.service.VideoRecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by admin on 2017/12/11.
 */
public class VideoRecommendServlet extends HttpServlet {
    private static final int DEFAULT_REQ_NUM = 7;
    private static final Logger LOG = LoggerFactory.getLogger(VideoRecommendServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
            String ihf = String.valueOf(RequestParamHelper.getInt(req, RequestParams.ihf.name(), 0));
            taggedMetricRegistry.taggedMeter("http.uservideo.recommend.qps", ImmutableMap.of("ihf", ihf)).mark();
            Timer timer = taggedMetricRegistry.taggedTimer("http.uservideo.recommend.request.latency", ImmutableMap.of("ihf", ihf));
            Timer.Context time = timer.time();

            long beginTime = System.currentTimeMillis();
            resp.setContentType("application/json");
            resp.setCharacterEncoding("utf-8");
            req.setCharacterEncoding("utf-8");

            VideoRecommendContextBuilder recommendContextBuilder = VideoRecommendContextBuilder.getInstance();
            VideoRecommenderContext recommendContext = recommendContextBuilder.build(req);
            VideoRecommendService recommendService = new VideoRecommendService(recommendContext);
            List<NewsDoc> result = recommendService.recommend();

            long latency = System.currentTimeMillis() - beginTime;
            Map<String, Object> extMap = ExtInfoBuilder.build(recommendContext, latency);
            RecommendRequest recommendRequest = recommendContext.getRecommendRequest();
            LOG.info("http request latency={} diu={} ihf={}", latency, recommendRequest.getDiu(), recommendRequest.getIhf());
            time.stop();

            Map<String, Object> resultMap = ApiResultBuilder.create().success(result).build();
            resultMap.put("ext", extMap);
            Optional<String> resultJsonOpt = JsonUtils.toJson(resultMap);
            if (resultJsonOpt.isPresent()) {
                resp.getWriter().println(resultJsonOpt.get());
            }
        } catch (Exception e) {
            Map<String, Object> resultMap = ApiResultBuilder.create().failure(e.getMessage()).build();
            Optional<String> jsonOpt = JsonUtils.toJson(resultMap);
            if (jsonOpt.isPresent()) {
                resp.getWriter().println(jsonOpt.get());
            }

            LOG.error("recommend request failed!", e);
        }
    }
}
