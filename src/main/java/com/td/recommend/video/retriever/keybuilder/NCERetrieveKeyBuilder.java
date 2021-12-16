package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.KeyTag;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.userstore.data.UserItem;

/**
 * Created by admin on 2017/8/8.
 */
public class NCERetrieveKeyBuilder implements RetrieveKeyBuilder {

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String id = userItem.getId();

        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(RetrieverType.vnce.name())
                .setAlias(RetrieverType.vnce.alias())
                .setKey(id)
                .addTag(KeyTag.sixhour.name());

        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
