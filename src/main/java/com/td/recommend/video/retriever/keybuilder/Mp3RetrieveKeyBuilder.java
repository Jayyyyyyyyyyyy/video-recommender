package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class Mp3RetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(Mp3RetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public Mp3RetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        if (recommendContext.hasBucket("baserank-exp")){
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vmp3rank_st, Arrays.asList("st_vmp3_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vmp3rank, Arrays.asList("vmp3_cs", "vmp3_ck"));
        }else {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vmp3_st, Arrays.asList("st_vmp3_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vmp3, Arrays.asList("vmp3_cs", "vmp3_ck"));
        }

    }

    private void addInterestRetrieveKeys(RetrieveKeyContext retrieveKeyContext,
                                         RetrieverType retrieverType,
                                         List<String> facets) {
        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, retrieverType);
        featureExtractor.extractL2VarianceMap(facets);
        Set<RetrieveKey> retrieveKeys = featureExtractor.getKeyFeaturesMap().keySet();
        Set<RetrieveKey> interests = new HashSet<>();
        Set<RetrieveKey> nonSample = new HashSet<>();
        retrieveKeys.forEach(k -> {
            if (k.getType().matches(".*(_ev|_en|_tr|_op|_live|_f3)|vx.*")) {
                nonSample.add(k);
            } else {
                interests.add(k);
            }
        });

        double p = Math.min(1.0, 4 / (interests.size() + 0.1));
        BinomialDistribution bdw = new BinomialDistribution(1, p);
        interests.stream()
                .filter(r -> bdw.sample() == 1)
                .forEach(retrieveKeyContext::addRetrieveKey);

        addExploreKeys(retrieveKeyContext, nonSample);

    }

    private void addExploreKeys(RetrieveKeyContext retrieveKeyContext, Set<RetrieveKey> keys) {
        ArrayList<RetrieveKey> keyList = new ArrayList<>(keys);
        Collections.shuffle(keyList);
        keyList.stream().limit(10).forEach(retrieveKeyContext::addRetrieveKey);
    }
}
