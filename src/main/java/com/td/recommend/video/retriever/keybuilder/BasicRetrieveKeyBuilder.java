package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

public class BasicRetrieveKeyBuilder implements RetrieveKeyBuilder {
  private VideoRecommenderContext recommendContext;

  public BasicRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
    this.recommendContext = recommendContext;
  }

  @Override
  public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
    RetrieveKey retrievekey = new RetrieveKey();
    retrievekey.setType(RetrieverType.vbasic.name());
    retrievekey.setAlias(RetrieverType.vbasic.alias());
    retrievekey.setKey(recommendContext.getRecommendRequest().getCid());
    retrievekey.setReason("新人必看");
    retrievekey.setScore(1.0);

    retrieveKeyContext.addRetrieveKey(retrievekey);
  }
}
