package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

public class TeachingRetrieveKeyBuilder implements RetrieveKeyBuilder {
  private VideoRecommenderContext recommendContext;

  public TeachingRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
    this.recommendContext = recommendContext;
  }

  @Override
  public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
    RetrieveKey retrievekey = new RetrieveKey();
    retrievekey.setType(RetrieverType.vteaching.name());
    retrievekey.setAlias(RetrieverType.vteaching.alias());
    retrievekey.setKey(recommendContext.getRecommendRequest().getCid());
    retrievekey.setScore(1.0);
    retrievekey.setReason("舞蹈教学");

    retrieveKeyContext.addRetrieveKey(retrievekey);
  }
}
