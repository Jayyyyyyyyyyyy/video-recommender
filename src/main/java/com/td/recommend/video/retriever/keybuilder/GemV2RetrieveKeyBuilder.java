package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GemV2RetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final int MAX_COUNT = 50;
    private static final int MAX_ELAPSED_SECONDS = 60 * 60 * 6;

    public GemV2RetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<String> candidates = new ArrayList<>();
        if (recommendContext.getClicks().size() > 0) {
            candidates.add(recommendContext.getClicks().get(0)); //取第一个点击的视频vid
        }
        List<String> played = recommendContext.getPlayed().stream().filter(k -> k.getPt() > 30).map(k -> k.getId()).limit(4).collect(Collectors.toList());
        candidates.addAll(played); //播放过的视频大于30秒，取4个

        BetaDistribution bd = new BetaDistribution(1, 2);
        for (int i = 0; i < Math.min(5, played.size()); i++) {
            int index = (int) (bd.sample() * played.size());
            candidates.add(played.get(index));//又添加了5个
        }

        for (String docId : candidates) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(RetrieverType.vgem.name())
                    .setAlias(RetrieverType.vgem.alias())
                    .setScore(1.0)
                    .setKey(docId)
                    .addAttribute("maxCnt", 5);
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }
    }
}
