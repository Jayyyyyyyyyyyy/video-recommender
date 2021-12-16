package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.rank.featuredumper.bean.DynamicDumpInfo;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by admin on 2017/12/2.
 */
public class RealTimeSearchRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final Logger LOG = LoggerFactory.getLogger(RealTimeSearchRetrieveKeyBuilder.class);

    public RealTimeSearchRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
//        addSearchRetrieveKeys(userItem, retrieveKeyContext, RetrieverType.sevteacher);
//        addSearchRetrieveKeys(userItem, retrieveKeyContext, RetrieverType.sevmp3);
        addSearchRetrieveKeys(retrieveKeyContext);
    }

    private void addSearchRetrieveKeys(RetrieveKeyContext retrieveKeyContext){
        List<DynamicDumpInfo.WordTime> keyWords = recommendContext.getKeyWords();
        if(null != keyWords && keyWords.size() > 0){
            for (int i = 0; i < keyWords.size() && i < 3; i++) {
                RetrieveKey retrieveKey = new RetrieveKey();
                retrieveKey.setType(RetrieverType.vrealtimesearch.name());
                retrieveKey.setAlias(RetrieverType.vrealtimesearch.alias());
                retrieveKey.setKey(keyWords.get(i).getW());
                retrieveKeyContext.addRetrieveKey(retrieveKey);
                String bucket = recommendContext.hasBucket("exercise_filter-yes") ? "exercise_filter-yes" : "";
                retrieveKey.setPlaceholder(bucket);

            }
        }
    }

    private void addSearchRetrieveKeys(UserItem userItem, RetrieveKeyContext retrieveKeyContext, RetrieverType type) {

        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, type);
        featureExtractor.extract(Collections.singletonList(type.name()));
        Set<RetrieveKey> retrieveKeys = featureExtractor.getKeyFeaturesMap().keySet();

        double p = Math.min(1.0, 3.0 / (retrieveKeys.size() + 0.1));
        BinomialDistribution bd = new BinomialDistribution(1, p);
        String lastSearchId = "";
        try {
            List<String> searchedIds = userItem.getUserRawData().get()
                    .getFeaturesMap().get(type.name()).getKey();
            lastSearchId = searchedIds.get(0);
        } catch (Exception ignored) {
        }
        String key = lastSearchId;

        retrieveKeys.stream()
                .peek(r -> {
                    if (r.getKey().equals(key)) {
                        retrieveKeyContext.addRetrieveKey(r);
                    }
                })
                .filter(r -> bd.sample() == 1)
                .forEach(retrieveKeyContext::addRetrieveKey);
    }

    public static void main(String[] args) {
        UserItem userItem = new UserItemDao().get("A00000738E7460").get();
        List<String> searchedIds;
        try {
            searchedIds = userItem.getUserRawData().get()
                    .getFeaturesMap().get(RetrieverType.sevmp3.name()).getKey();

            System.out.println("ljk" + searchedIds);
        } catch (Exception e) {
            System.out.println(e);
            ;
        }
    }
}
