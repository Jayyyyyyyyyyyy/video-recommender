package com.td.recommend.video.retriever.keybuilder.smallvideo;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class SmallMp3RetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(SmallMp3RetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public SmallMp3RetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.svmp3_st, Arrays.asList("st_vmp3_ck"));
        if (recommendContext.hasBucket("xuserprofile-yes")) {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.svxmp3, Arrays.asList("v_xmp3_cs","v_xmp3_ck","v_xmp3_ct"));
        } else {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.svmp3, Arrays.asList("vmp3_cs", "vmp3_ck"));
        }

    }

    private void addInterestRetrieveKeys(RetrieveKeyContext retrieveKeyContext,
                                         RetrieverType retrieverType,
                                         List<String> facets) {
        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, retrieverType);
        featureExtractor.extractL2VarianceMap(facets);
        Set<RetrieveKey> retrieveKeys = featureExtractor.getKeyFeaturesMap().keySet();
        double p = Math.min(1.0, 4 / (retrieveKeys.size() + 0.1));
        BinomialDistribution bd = new BinomialDistribution(1, p);
        retrieveKeys.stream()
                .filter(r -> bd.sample() == 1)
                .forEach(retrieveKeyContext::addRetrieveKey);
    }

}
