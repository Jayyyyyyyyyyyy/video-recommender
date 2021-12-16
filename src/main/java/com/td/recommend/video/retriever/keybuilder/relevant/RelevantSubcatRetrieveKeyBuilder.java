package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.data.profile.common.KeyConstants;
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

import java.util.Optional;

/**
 * Created by admin on 2017/6/21.
 */
public class RelevantSubcatRetrieveKeyBuilder implements RetrieveKeyBuilder {
    public VideoRecommenderContext recommendContext;
    public RelevantSubcatRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String vid = recommendContext.getRecommendRequest().getVid();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        String subcat;
        try {
            Optional<DocItem> docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(vid);
            subcat = docItem.get()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(KeyConstants.secondcat).get()
                    .getId();
        } catch (Exception e) {
            return;
        }
        if (StringUtils.isBlank(subcat)) {
            return;
        }
        RetrieverType retrieverType;
        if(ihf == Ihf.VBIG_RLVT.id()){
            retrieverType = RetrieverType.vsubcat_rlvt;
        } else {
            retrieverType = RetrieverType.svsubcat_rlvt;
        }
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(retrieverType.name())
                .setAlias(retrieverType.alias())
                .setScore(1.0)
                .setKey(subcat);
        retrieveKeyContext.addRetrieveKey(retrieveKey);

    }

}
