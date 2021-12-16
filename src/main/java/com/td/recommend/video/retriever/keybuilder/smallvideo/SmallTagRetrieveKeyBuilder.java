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

/**
 * Created by admin on 2017/6/21.
 */
public class SmallTagRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(SmallTagRetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public SmallTagRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.svtag_st, Arrays.asList("st_vtag_ck"));
        if (recommendContext.hasBucket("xuserprofile-yes")) {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.svxtag, Arrays.asList("v_xtag_cs","v_xtag_ck","v_xtag_ct"));
        } else {
            addInterestRetrieveKeys(retrieveKeyContext, RetrieverType.svtag, Arrays.asList("vtag_cs", "vtag_ck"));
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
