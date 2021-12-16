package com.td.recommend.video.api.servlet;

import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.recommend.commons.api.ApiResultBuilder;
import com.td.recommend.commons.json.JsonUtils;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.response.NewsDoc;
import com.td.recommend.video.api.vo.ExtInfoBuilder;
import com.td.recommend.video.debug.RerankDebugInfo;
import com.td.recommend.video.debug.RerankDebugService;
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
 * create by pansm at 2019/08/08
 */
public class VideoRerankDebugServlet extends HttpServlet {
    private static final int DEFAULT_REQ_NUM = 7;
    private static final Logger LOG = LoggerFactory.getLogger(VideoRecommendServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            long beginTime = System.currentTimeMillis();
            resp.setContentType("application/json");
            resp.setCharacterEncoding("utf-8");
            req.setCharacterEncoding("utf-8");

            VideoRecommendContextBuilder recommendContextBuilder = VideoRecommendContextBuilder.getInstance();
            VideoRecommenderContext recommendContext = recommendContextBuilder.build(req);

            RerankDebugService debugService = RerankDebugService.getInstance();
            RerankDebugInfo debugInfo = debugService.recommend(recommendContext);


            long latency = System.currentTimeMillis() - beginTime;
            Map<String, Object> extMap = ExtInfoBuilder.build(recommendContext, latency);
            RecommendRequest recommendRequest = recommendContext.getRecommendRequest();
            LOG.info("debug video-recommend request latency={} diu={} ", latency, recommendRequest.getDiu());

            Map<String, Object> result = ApiResultBuilder.create().success(debugInfo).build();
            Optional<String> jsonOpt = JsonUtils.toJson(result);
            if (jsonOpt.isPresent()) {
                resp.getWriter().println(jsonOpt.get());
            }
        } catch (Exception e) {

            Map<String, Object> resultMap = ApiResultBuilder.create().failure(e.getMessage()).build();
            Optional<String> jsonOpt = JsonUtils.toJson(resultMap);
            if (jsonOpt.isPresent()) {
                resp.getWriter().println(jsonOpt.get());
            }
            LOG.error("debug video-recommend request failed!", e);
        }
    }
}
