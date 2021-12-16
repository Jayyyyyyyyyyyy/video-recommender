package com.td.recommend.video.retriever.keybuilder;

import com.ning.http.client.RequestBuilder;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.retriever.RetrieveKeyBuilderPipeline;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frang on 2018/5/8.
 */
public class VideoSubchannelRetrieveKeyBuilderFactory implements RetrieveKeyBuilderFactory {
    private static final Logger LOG = LoggerFactory.getLogger(VideoSubchannelRetrieveKeyBuilderFactory.class);

    private static VideoSubchannelRetrieveKeyBuilderFactory instance = new VideoSubchannelRetrieveKeyBuilderFactory();

    public static VideoSubchannelRetrieveKeyBuilderFactory getInstance() {
        return instance;
    }

    @Override
    public VideoRetrieveKeyBuilder create(VideoRecommenderContext context) {
        List<RetrieveKeyBuilder> retrieveKeyBuilders = new ArrayList<>();
        RequestBuilder requestBuilder = new RequestBuilder();


        RecommendRequest recommendRequest = context.getRecommendRequest();

        String channelId = String.valueOf(recommendRequest.getChannel());

        List<String> catNames = ChannelUtils.getCatNames(channelId);
        for (String catName : catNames) {
            RetrieveKeyBuilder retrieveKeyBuilder = new VideoSubchannelRetrieveKeyBuilder(catName);
            retrieveKeyBuilders.add(retrieveKeyBuilder);
        }


        RetrieveKeyBuilderPipeline pipeline = new RetrieveKeyBuilderPipeline(retrieveKeyBuilders);
        return new VideoRetrieveKeyBuilder(pipeline, context);
    }
}
