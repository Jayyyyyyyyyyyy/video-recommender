package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.data.profile.common.KeyConstants;
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


public class RelevantMp3RetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantMp3RetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public RelevantMp3RetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String vid = recommendContext.getRecommendRequest().getVid();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        String template = recommendContext.getRecommendRequest().getTemplate();
        String mp3Name;
        Optional<DocItem> docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(vid);
        try {
            mp3Name = docItem.get()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(KeyConstants.content_mp3).get()
                    .getName();
        } catch (Exception e) {
            return;
        }
        if (StringUtils.isBlank(mp3Name)) {
            return;
        }
        if (template.equals("mp3") && !DocProfileUtils.getFirstCat(docItem.get()).equals("264")) {
            return;
        }
        RetrieverType retrieverType;
        if (ihf == Ihf.VSMALL_RLVT.id()) {
            retrieverType = RetrieverType.svmp3_rlvt;
        } else {
            retrieverType = RetrieverType.vmp3_rlvt;
        }
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(retrieverType.name())
                .setAlias(retrieverType.alias())
                .setScore(1.0)
                .setKey(mp3Name);
        if (recommendContext.hasBucket("relevant_filter-yes")) {
            retrieveKey.setPlaceholder("relevant_filter-yes");
        }
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
