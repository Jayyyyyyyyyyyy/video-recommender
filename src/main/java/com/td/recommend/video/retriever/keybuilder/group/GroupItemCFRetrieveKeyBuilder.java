package com.td.recommend.video.retriever.keybuilder.group;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.List;

public class GroupItemCFRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final int MAX_COUNT = 50;
    private static final int MAX_ELAPSED_SECONDS = 60 * 60 * 6;

    public GroupItemCFRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<String> clicked = recommendContext.getClicks();
        List<String> recentClicked = clicked.subList(0, Math.min(3, clicked.size()));

        int maxCnt = 500;
        for (String docId : recentClicked) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(RetrieverType.gitemcf.name())
                    .setAlias(RetrieverType.gitemcf.alias())
                    .setScore(1.0)
                    .setKey(docId)
                    .addAttribute("maxCnt", maxCnt);
            if (UserProfileUtils.is32StepUser(userItem, null)) {
                retrieveKey.setPlaceholder("32step");
            } else if (UserProfileUtils.is32StepUser(userItem, null)) {
                retrieveKey.setPlaceholder("fitness");
            } else {
                retrieveKey.setPlaceholder("other");
            }

            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }

        List<String> restClicked = clicked.subList(recentClicked.size(), Math.min(MAX_COUNT, clicked.size()));


        BetaDistribution bd = new BetaDistribution(1, 4);
        int extendNum = Math.min(3, restClicked.size());


        for (int i = 0; i < extendNum; i++) {
            int index = (int) (bd.sample() * restClicked.size());
            String docId = restClicked.get(index);
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(RetrieverType.gitemcf.name())
                    .setAlias(RetrieverType.gitemcf.alias())
                    .setScore(1.0)
                    .setKey(docId)
                    .addAttribute("maxCnt", maxCnt);
            if (UserProfileUtils.isFitnessUser(userItem, null)) {
                retrieveKey.setPlaceholder("1007");
            } else if (UserProfileUtils.is32StepUser(userItem, null)) {
                retrieveKey.setPlaceholder("268");
            }
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }

    }

}

