package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.List;
import java.util.stream.Collectors;

public class Item2VecRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final int MAX_COUNT = 50;
    private static final int MAX_ELAPSED_SECONDS = 60 * 60 * 6;

    public Item2VecRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<String> clicks = recommendContext.getClicks().subList(0, Math.min(2, recommendContext.getClicks().size()));
        List<DynamicDumpInfo.VidDuration> played = recommendContext.getPlayed();
        List<String> filtered = played.stream().filter(k -> k.getPt() > 30).map(k -> k.getId()).collect(Collectors.toList());
        for (String id : filtered) {
            if (clicks.size()<4 && !clicks.contains(id)) {
                clicks.add(id);
            }
            if (clicks.size()>=4) {
                break;
            }
        }
        if (clicks.size()<4 && recommendContext.getClicks().size()>2) {
            int endpos = 4-clicks.size()+2;
            clicks.addAll(recommendContext.getClicks().subList(2, Math.min(endpos,recommendContext.getClicks().size())));
        }
        int maxCnt = 5;
        for (String docId : clicks) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(RetrieverType.vitem2vec.name())
                    .setAlias(RetrieverType.vitem2vec.alias())
                    .setScore(1.0)
                    .setKey(docId)
                    .addAttribute("maxCnt", maxCnt);
            String bucket = recommendContext.hasBucket("exercise_filter-yes") ? "exercise_filter-yes" : "";
            retrieveKey.setPlaceholder(bucket);

            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }
    }
}
