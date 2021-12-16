package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

import java.util.Optional;

/**
 * Created by admin on 2017/12/2.
 */
public class RelevantAuthorFreshRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public RelevantAuthorFreshRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        Optional<IItem> iItem = recommendContext.getQueryItems().get(ItemKey.doc);
        if (!iItem.isPresent()) {
            return;
        }
        String template = recommendContext.getRecommendRequest().getTemplate();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        DocItem docItem = (DocItem) iItem.get();
        if (template.equals("author") && DocProfileUtils.getTalentStar(docItem) < 4) {
            return;
        }
        String uid = DocProfileUtils.getUid(docItem);
        RetrieveKey retrieveKey = new RetrieveKey();
        RetrieverType retrieverType;
        if(ihf == Ihf.VBIG_RLVT.id()){
            retrieverType = RetrieverType.vauthorfresh_rlvt;
        } else {
            retrieverType = RetrieverType.svauthorfresh_rlvt;
        }
        retrieveKey.setType(retrieverType.name())
                .setAlias(retrieverType.alias())
                .setReason("最新")
                .setKey(uid);
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
