package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.SmallVideoAuthorSupport;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by admin on 2017/6/21.
 */
public class RelevantAuthorSupportRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantAuthorSupportRetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public RelevantAuthorSupportRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<String> authorList = SmallVideoAuthorSupport.getUidList();
        double p = Math.min(1.0, 4 / (authorList.size() + 0.1));
        BinomialDistribution bd = new BinomialDistribution(1, p);
        for (int i = 0; i < authorList.size(); i++) {
            if(bd.sample() == 1){
                RetrieveKey retrieveKey = new RetrieveKey();
                retrieveKey.setType(RetrieverType.svauthorsupport_rlvt.name());
                retrieveKey.setAlias(RetrieverType.svauthorsupport_rlvt.alias());
                retrieveKey.setKey(authorList.get(i));
                retrieveKeyContext.addRetrieveKey(retrieveKey);
            }
        }
    }
}
