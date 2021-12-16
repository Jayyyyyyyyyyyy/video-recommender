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

public class ItemCFRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final int MAX_COUNT = 50;
    private static final int MAX_ELAPSED_SECONDS = 60 * 60 * 6;

    public ItemCFRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<String> candidates = new ArrayList<>();
        if (recommendContext.getClicks().size() > 0) {
            candidates.add(recommendContext.getClicks().get(0));
        }
        List<String> played = recommendContext.getPlayed().stream().filter(k -> k.getPt() > 30).map(k -> k.getId()).limit(4).collect(Collectors.toList());
        candidates.addAll(played);

        BetaDistribution bd = new BetaDistribution(1, 2); //beta分布取数据
        for (int i = 0; i < Math.min(5, played.size()); i++) {
            int index = (int) (bd.sample() * played.size());
            candidates.add(played.get(index));
        }

        for (String docId : candidates) {
            RetrieveKey retrieveKey = new RetrieveKey();
//            //retrieveKey.setType(RetrieverType.vitemcf_v2.name())
//
//            if (recommendContext.hasBucket("itemcf-base")){
//                retrieveKey.setType(RetrieverType.vitemcf.name())
//                        .setAlias(RetrieverType.vitemcf.alias())
//                        .setScore(1.0)
//                        .setKey(docId)
//                        .addAttribute("maxCnt", 5);
//            }else{
//                    retrieveKey.setType(RetrieverType.vitemcfv2.name())
//                            .setAlias(RetrieverType.vitemcfv2.alias())
//                            .setScore(1.0)
//                            .setKey(docId)
//                            .addAttribute("maxCnt", 5);
//            }

            retrieveKey.setType(RetrieverType.vitemcf.name())
                    .setAlias(RetrieverType.vitemcf.alias())
                    .setScore(1.0)
                    .setKey(docId)
                    .addAttribute("maxCnt", 5);
//            if (recommendContext.hasBucket('itemcf-exp')) { //召回实验在这改,或者放setPlaceholder，
//                retrieveKey.setPlaceholder("itemcf-exp"); //将bucket名称传到召回服务端进行判断
//
//            }else{
//                retrieveKey.setPlaceholder("itemcf-base");
//            }

            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }
    }
}
