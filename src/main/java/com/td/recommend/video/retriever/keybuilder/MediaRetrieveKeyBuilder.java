package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.abtest.BucketConstants;
import com.td.recommend.video.profile.UserProfileHelper;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.MediaFeatureScorer;
import com.td.recommend.video.retriever.keybuilder.utils.RetrieveKeyBuilderHelper;
import com.td.recommend.video.utils.CommonConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by admin on 2017/6/21.
 */
public class MediaRetrieveKeyBuilder implements RetrieveKeyBuilder {

    public VideoRecommenderContext recommendContext;

    public MediaRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, RetrieverType.vfrom);
        List<String> facets = new ArrayList<>(Arrays.asList("vmedia_name_cs", "vmedia_name_ck", "vinner_source_cs", "vinner_source_ck"));

        featureExtractor.extract(facets);

        MediaFeatureScorer featureScorer = new MediaFeatureScorer(recommendContext);


        List<RetrieveKey> retrieveKeys = RetrieveKeyBuilderHelper.getScoredRetrieveKeys(featureExtractor, featureScorer);

        retrieveKeys.sort(Comparator.comparing(RetrieveKey::getScore).reversed());

        if (retrieveKeys.size() > 15) {
            retrieveKeys = retrieveKeys.subList(0, 15);
        }

        retrieveKeyContext.addRetrieveKeys(retrieveKeys);

        if (recommendContext.hasBucket(BucketConstants.BOBO_EXP) && UserProfileHelper.shouldBobo(userItem)) {
            RetrieveKey boboRetrieveKey = buildBoboRetrieveKey();
            retrieveKeyContext.addRetrieveKey(boboRetrieveKey);
        }
    }


    public static RetrieveKey buildBoboRetrieveKey() {
        RetrieveKey  retrieveKey = new RetrieveKey();
        retrieveKey.setKey(CommonConstants.BOBO_KEY)
                .setType(RetrieverType.vfrom.name())
                .setScore(1.0)
                .setReason("bobo vfrom")
                .addTag("bobo");

        return retrieveKey;
    }
}
