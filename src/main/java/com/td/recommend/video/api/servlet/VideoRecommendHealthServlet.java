package com.td.recommend.video.api.servlet;

import com.alibaba.fastjson.JSON;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.response.NewsDoc;
import com.td.recommend.video.api.dubbo.VideoRecommendContextBuilder;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.service.VideoRecommendService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class VideoRecommendHealthServlet extends HttpServlet {
    String port = "8088";

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        port = servletConfig.getInitParameter("port");
        super.init(servletConfig);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String params = "{\"ab\":\"feed_add_module3-old\",\"alg\":\"ftrl\",\"appId\":\"t01\",\"appStore\":\"realease\",\"channel\":\"oppo\",\"cid\":\"80000\",\"city\":\"东莞市\",\"cityCode\":0,\"client\":\"2\",\"device\":\"PADM00-Android:10\",\"district\":\"惠来县\",\"diu\":\"860665048679897\",\"duration\":0,\"highEnd\":false,\"ihf\":1,\"lat\":0.0,\"lon\":0.0,\"manufacture\":\"OPPO\",\"model\":\"PADM00\",\"netType\":\"WIFI\",\"new_user\":0,\"newstype\":-1,\"nid\":\"0\",\"num\":12,\"pageNo\":1,\"province\":\"unknown\",\"ruleType\":\"newrule-exp\",\"screenHeight\":2200,\"screenWidth\":1080,\"sex\":0,\"ssl\":0,\"uid\":\"18451016\",\"version\":\"7.2.0\",\"vid\":\"\",\"is_first_access\":\"0\",\"template\":\"unknown\"}";
        RecommendRequest recommendRequest = JSON.parseObject(params, RecommendRequest.class);
        VideoRecommendContextBuilder userNewsRecommendContextBuilder = VideoRecommendContextBuilder.getInstance();
        VideoRecommenderContext recommendContext = userNewsRecommendContextBuilder.build(recommendRequest, true);
        VideoRecommendService recommendService = new VideoRecommendService(recommendContext);
        List<NewsDoc> recommend = recommendService.recommend();
        if (recommend == null || recommend.isEmpty()) {
            resp.setStatus(500);
            resp.getWriter().print("sick");
        } else {
            resp.setStatus(200);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print(JSON.toJSONString(recommend));
        }
    }
}
