package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.data.profile.item.KeyItem;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/21.
 */
public class RelevantTagRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantTagRetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public RelevantTagRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String vid = recommendContext.getRecommendRequest().getVid();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        List<String> tags;
        try {
            Optional<DocItem> docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(vid);
            tags = docItem.get()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getContent_tag().stream()
                    .map(KeyItem::getId).collect(Collectors.toList());
        } catch (Exception e) {
            return;
        }
        RetrieverType retrieverType;
        if(ihf == Ihf.VBIG_RLVT.id()){
            retrieverType = RetrieverType.vtag_rlvt;
        } else {
            retrieverType = RetrieverType.svtag_rlvt;
        }
        tags.subList(0, Math.min(2, tags.size())).forEach(tag -> {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(retrieverType.name())
                    .setAlias(retrieverType.alias())
                    .setScore(1.0)
                    .setKey(tag);
            retrieveKeyContext.addRetrieveKey(retrieveKey);

        });

    }
}
