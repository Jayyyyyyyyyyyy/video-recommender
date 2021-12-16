package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.ShowDanceConfigs;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.Set;

/**
 * Created by admin on 2017/12/2.
 */
public class OfAlbumRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public OfAlbumRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        Set<String> album = ShowDanceConfigs.getAlbum();
        album.forEach(albumId -> {
            RetrieveKey retrieveKey = new RetrieveKey();
            int ihf = recommendContext.getRecommendRequest().getIhf();
            retrieveKey.setIhf(String.valueOf(ihf))
                    .setType(RetrieverType.vofalbum.name())
                    .setAlias(RetrieverType.vofalbum.alias())
                    .setKey(albumId);
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        });
    }
}
