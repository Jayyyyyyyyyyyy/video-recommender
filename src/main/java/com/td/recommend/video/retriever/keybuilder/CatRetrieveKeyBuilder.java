package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by admin on 2017/6/19.
 */
public class CatRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public CatRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {

        if (recommendContext.hasBucket("baserank-exp")) {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vcatrank_st, Arrays.asList("st_vcat_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vcatrank, Arrays.asList("vcat_cs", "vcat_ck"));
        } else {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vcat_st, Arrays.asList("st_vcat_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vcat, Arrays.asList("vcat_cs", "vcat_ck"));
        }
        if (recommendContext.hasBucket("virtual-yes")) {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vxcat, Arrays.asList("vxcat_cs", "vxcat_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vxcat_st, Arrays.asList("st_vxcat_ck"));
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
        nonSample.forEach(retrieveKeyContext::addRetrieveKey);

    }
}
