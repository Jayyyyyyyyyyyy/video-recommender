package com.td.recommend.video.retriever.keybuilder;

import com.google.common.collect.ImmutableSet;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.*;
import java.util.stream.Collectors;

public class PhraseRetrieveKeyBuilder implements RetrieveKeyBuilder {

    public VideoRecommenderContext recommendContext;

    public PhraseRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    //1889 1890 1891 1892 1897 1898 1899 1990 1905 1906 1907 1908 1913 1914 1915 1916 1917 1918 1919 1920 2089 2090 2091 2092 2093 2094
    private static Set<String> pidSet = ImmutableSet.of("1889","1890","1891","1892","1897","1898","1899","1990","1905","1906","1907","1908","1913","1914","1915","1916","1917","1918","1919","1920","2089","2090","2091","2092","2093","2094");

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.vphrase, Arrays.asList("vphrase_ck", "vphrase_cs"));
    }

    private void addInterestRetrieveKeys(RetrieveKeyContext retrieveKeyContext,
                                         RetrieverType retrieverType,
                                         List<String> facets) {
        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, retrieverType);
        featureExtractor.extractL2VarianceMap(facets);
        Set<RetrieveKey> retrieveKeys = featureExtractor.getKeyFeaturesMap().keySet()
                .stream().filter(k -> pidSet.contains(k.getKey())).collect(Collectors.toSet());
        Set<RetrieveKey> interests = new HashSet<>();
        Set<RetrieveKey> nonSample = new HashSet<>();
        retrieveKeys.forEach(k -> {
            Object interestLevel = k.getAttribute("interestLevel").orElse("");
            if (k.getType().matches(".*(_ev|_en|_tr|_op|_live|_f3)|vx.*") ||
                    (interestLevel.equals(InterestFeatureExtractor.Level.STRONG) && recommendContext.hasBucket("weak_interest-exp"))) {
                nonSample.add(k);
            } else {
                interests.add(k);
            }
        });

        double p = Math.min(1.0, 4 / (interests.size() + 0.1));
        BinomialDistribution bd = new BinomialDistribution(1, p);
        interests.stream()
                .filter(r -> bd.sample() == 1)
                .forEach(retrieveKeyContext::addRetrieveKey);

        nonSample.forEach(retrieveKeyContext::addRetrieveKey);
    }
}