package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.*;

/**
 * Created by admin on 2017/6/19.
 */
public class GenreRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public GenreRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        if (recommendContext.hasBucket("baserank-exp")) {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vgenrerank_st, Arrays.asList("st_vgenre_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vgenrerank, Arrays.asList("vgenre_cs", "vgenre_ck"));
        } else {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vgenre_st, Arrays.asList("st_vgenre_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vgenre, Arrays.asList("vgenre_cs", "vgenre_ck"));
        }
    }

    private void addInterestRetrieveKeys(RetrieveKeyContext retrieveKeyContext,
                                         RetrieverType retrieverType,
                                         List<String> facets) {
        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, retrieverType);
        featureExtractor.extractL2VarianceMap(facets);
        Set<RetrieveKey> retrieveKeys = featureExtractor.getKeyFeaturesMap().keySet();
        Set<RetrieveKey> weakInterests = new HashSet<>();
        Set<RetrieveKey> nonSample = new HashSet<>();
        retrieveKeys.forEach(k -> {
            if (k.getType().matches(".*(_ev|_en|_tr|_op|_live|_f3)|vx.*")) {
                nonSample.add(k);
            } else {
                weakInterests.add(k);
            }
        });

        double p = Math.min(1.0, 4 / (weakInterests.size() + 0.1));
        BinomialDistribution bdw = new BinomialDistribution(1, p);
        weakInterests.stream()
                .filter(r -> bdw.sample() == 1)
                .forEach(retrieveKeyContext::addRetrieveKey);
        nonSample.forEach(retrieveKeyContext::addRetrieveKey);
    }
}
