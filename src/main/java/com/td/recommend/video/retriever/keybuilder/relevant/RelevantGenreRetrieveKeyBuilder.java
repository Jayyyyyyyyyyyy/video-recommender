package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by admin on 2017/6/21.
 */
public class RelevantGenreRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantGenreRetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public RelevantGenreRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        int ihf = recommendContext.getRecommendRequest().getIhf();
        String vid = recommendContext.getRecommendRequest().getVid();
        String genre;
        try {
            Optional<DocItem> docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(vid);
            genre = DocProfileUtils.getGenre(docItem.get());
        } catch (Exception e) {
            return;
        }
        if (StringUtils.isBlank(genre)) {
            return;
        }
        RetrieverType retrieverType;
        if(ihf == Ihf.VSMALL_RLVT.id()){
            retrieverType = RetrieverType.svgenre_rlvt;
        } else {
            retrieverType = RetrieverType.vgenre_rlvt;
        }
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(retrieverType.name())
                .setAlias(retrieverType.alias())
                .setScore(1.0)
                .setKey(genre);
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
