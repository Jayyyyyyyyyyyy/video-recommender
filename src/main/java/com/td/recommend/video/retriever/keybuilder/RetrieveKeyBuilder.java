package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.ItemBasedRetrieveKeyBuilder;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;

/**
 * Created by admin on 2017/6/8.
 */
public interface RetrieveKeyBuilder extends ItemBasedRetrieveKeyBuilder<UserItem> {
    void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext);
}
