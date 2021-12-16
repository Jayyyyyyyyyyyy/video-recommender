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
public class RelevantAuthorRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantAuthorRetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public RelevantAuthorRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        int ihf = recommendContext.getRecommendRequest().getIhf();
        String vid = recommendContext.getRecommendRequest().getVid();
        String uid;
        String template = recommendContext.getRecommendRequest().getTemplate();
        Optional<DocItem> docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(vid);
        try {
            uid = String.valueOf(docItem.get()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getUid());
        } catch (Exception e) {
            return;
        }
        if (StringUtils.isBlank(uid)) {
            return;
        }
        if (template.equals("author") && DocProfileUtils.getTalentStar(docItem.get()) < 4) {
            return;
        }
        RetrieveKey retrieveKey = new RetrieveKey();

        RetrieverType retrieverType;
        if (ihf == Ihf.VSMALL_RLVT.id()) {
            retrieverType = RetrieverType.svauthor_rlvt;
        } else {
            retrieverType = RetrieverType.vauthor_rlvt;
        }
        retrieveKey.setType(retrieverType.name())
                .setAlias(retrieverType.alias())
                .setScore(1.0)
                .setKey(uid);
        if (recommendContext.hasBucket("relevant_filter-yes")) {
            retrieveKey.setPlaceholder("relevant_filter-yes");
        }
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
