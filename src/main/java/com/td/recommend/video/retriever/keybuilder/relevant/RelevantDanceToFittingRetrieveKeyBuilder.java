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
public class RelevantDanceToFittingRetrieveKeyBuilder implements RetrieveKeyBuilder {
    public VideoRecommenderContext recommendContext;

    public RelevantDanceToFittingRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String vid = recommendContext.getRecommendRequest().getVid();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        String cat;
        try {
            Optional<DocItem> docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(vid);
            cat = docItem.get()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(KeyConstants.firstcat).get()
                    .getId();
        } catch (Exception e) {
            return;
        }
        if (StringUtils.isBlank(cat) || !cat.equals("264")) {
            return;
        }
        RetrieverType retrieverType;
        if (ihf == Ihf.VBIG_RLVT.id()) {
            retrieverType = RetrieverType.vcat_rlvt;
        } else {
            retrieverType = RetrieverType.svcat_rlvt;
        }
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(retrieverType.name())
                .setAlias(retrieverType.alias())
                .addAttribute("maxCnt", 10)
                .setKey("1006");//通过广场舞召回健身，用于健身场景扩展
        retrieveKeyContext.addRetrieveKey(retrieveKey);

    }

}
