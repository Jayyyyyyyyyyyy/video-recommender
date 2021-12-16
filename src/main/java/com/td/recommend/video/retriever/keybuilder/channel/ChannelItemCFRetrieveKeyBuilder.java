package com.td.recommend.video.retriever.keybuilder.channel;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.List;

public class ChannelItemCFRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final int MAX_COUNT = 50;
    private static final int MAX_ELAPSED_SECONDS = 60 * 60 * 6;

    public ChannelItemCFRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<String> clicked = recommendContext.getClicks();
        List<String> recentClicked = clicked.subList(0, Math.min(3, clicked.size()));
        int maxCnt = 5;
        for (String docId : recentClicked) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(RetrieverType.vitemcf_chnl.name())
                    .setAlias(RetrieverType.vitemcf_chnl.alias())
                    .setScore(1.0)
                    .setKey(docId)
                    .addAttribute("maxCnt", maxCnt);
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }

        List<String> restClicked = clicked.subList(recentClicked.size(), Math.min(MAX_COUNT, clicked.size()));
        double p = Math.min(1.0, 3 / (restClicked.size() + 0.1));
        BinomialDistribution bd = new BinomialDistribution(1, p);
        restClicked.stream()
                .filter(docId -> bd.sample() == 1)
                .forEach(docId -> {
                    RetrieveKey retrieveKey = new RetrieveKey();
                    retrieveKey.setType(RetrieverType.vitemcf_chnl.name())
                            .setAlias(RetrieverType.vitemcf_chnl.alias())
                            .setScore(1.0)
                            .setKey(docId)
                            .addAttribute("maxCnt", maxCnt);
                    retrieveKeyContext.addRetrieveKey(retrieveKey);
                });


    }

}

