package com.td.recommend.video.retriever.keybuilder.derived;


import com.td.recommend.commons.retriever.RetrieveKey;

import java.util.Set;

/**
 * Created by admin on 2017/10/30.
 */
public interface UserDerivedRetrieveKeyBuilder {
    Set<RetrieveKey> build(Set<RetrieveKey> retrieveKeys);
}
