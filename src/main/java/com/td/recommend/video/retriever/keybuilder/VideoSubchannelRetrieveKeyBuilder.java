package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.userstore.data.UserItem;

public class VideoSubchannelRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private String catName;
    public VideoSubchannelRetrieveKeyBuilder(String catName) {
        this.catName = catName;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                   .setType(RetrieverType.vcat.name())
                   .setKey(catName)
                   .setReason(catName)
                   .addTag(KeyTag.channel.name())
                   .setPlaceholder("v");

        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
