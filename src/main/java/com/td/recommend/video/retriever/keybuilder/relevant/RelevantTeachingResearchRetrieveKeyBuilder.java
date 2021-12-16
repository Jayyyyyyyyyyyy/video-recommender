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
public class RelevantTeachingResearchRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public RelevantTeachingResearchRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        Optional<IItem> iItem = recommendContext.getQueryItems().get(ItemKey.doc);
        if (!iItem.isPresent()) {
            return;
        }
        int ihf = recommendContext.getRecommendRequest().getIhf();
        String vid = recommendContext.getRecommendRequest().getVid();
        if(ihf == Ihf.VBIG_RLVT.id()){
            RetrieverType retrieverType = RetrieverType.vteachingresearch_rlvt;
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(retrieverType.name())
                    .setAlias(retrieverType.alias())
                    .setKey(vid);
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }
    }
}
