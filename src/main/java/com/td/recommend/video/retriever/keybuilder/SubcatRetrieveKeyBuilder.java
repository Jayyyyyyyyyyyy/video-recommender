package com.td.recommend.video.retriever.keybuilder;

import com.google.common.collect.ImmutableSet;
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
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/21.
 */
public class SubcatRetrieveKeyBuilder implements RetrieveKeyBuilder {
    public VideoRecommenderContext recommendContext;

    public SubcatRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        if (recommendContext.hasBucket("baserank-exp")) {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vsubcatrank_st, Arrays.asList("st_vsubcat_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vsubcatrank, Arrays.asList("vsubcat_cs", "vsubcat_ck"));
        } else {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vsubcat_st, Arrays.asList("st_vsubcat_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vsubcat, Arrays.asList("vsubcat_cs", "vsubcat_ck"));
        }
        if (recommendContext.hasBucket("virtual-yes")) {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vxsubcat, Arrays.asList("vxsubcat_cs", "vxsubcat_ck"));
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vxsubcat_st, Arrays.asList("st_vxsubcat_ck"));
        }
    }

    private void addInterestRetrieveKeys(RetrieveKeyContext retrieveKeyContext,
                                         RetrieverType retrieverType,
                                         List<String> facets) {
        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, retrieverType);
        featureExtractor.extractL2VarianceMap(facets);
        Set<RetrieveKey> retrieveKeys = featureExtractor.getKeyFeaturesMap().keySet();

        Set<RetrieveKey> interest = new HashSet<>();
        Set<RetrieveKey> nonSample = new HashSet<>();
        retrieveKeys.forEach(k -> {
            if (k.getType().matches(".*(_ev|_en|_tr|_op|_live|_f3)|vx.*")) {
                nonSample.add(k);
            } else {
                interest.add(k);
                if (!ImmutableSet.of("285", "300").contains(k.getKey())) {
                    interest.add(k);
                }
                addMappingKeys(retrieveKeyContext, interest);

            }
        });

        double p = Math.min(1.0, 4 / (interest.size() + 0.1));
        BinomialDistribution bdw = new BinomialDistribution(1, p);
        interest.stream()
                .filter(r -> bdw.sample() == 1)
                .forEach(retrieveKeyContext::addRetrieveKey);

        nonSample.forEach(retrieveKeyContext::addRetrieveKey);

    }

    private void addMappingKeys(RetrieveKeyContext retrieveKeyContext, Set<RetrieveKey> interests) {
        Set<String> existedKeys = retrieveKeyContext.getRetrieveKeys().stream().map(RetrieveKey::getKey).collect(Collectors.toSet());
        if (existedKeys.contains("124")) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setAlias("secondcat.tagid");
            retrieveKey.setKey("1009");
            retrieveKey.addAttribute("maxCnt", 10);
            retrieveKey.setType(RetrieverType.vsubcat.name());
            interests.add(retrieveKey);
        }
    }
}