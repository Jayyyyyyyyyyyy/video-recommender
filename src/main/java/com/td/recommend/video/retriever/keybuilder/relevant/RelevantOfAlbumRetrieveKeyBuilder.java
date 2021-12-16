package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.ShowDanceConfigs;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by admin on 2017/6/21.
 */
public class RelevantOfAlbumRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RelevantOfAlbumRetrieveKeyBuilder.class);

    private VideoRecommenderContext recommendContext;

    public RelevantOfAlbumRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        int ihf = recommendContext.getRecommendRequest().getIhf();
        Set<String> album = ShowDanceConfigs.getAlbum();
        album.forEach(albumId -> {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setIhf(String.valueOf(ihf))
                    .setType(RetrieverType.vofalbum_rlvt.name())
                    .setAlias(RetrieverType.vofalbum_rlvt.alias())
                    .setKey(albumId);
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        });

    }
}
