package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

public class AtHomeRetrieveKeyBuilder implements RetrieveKeyBuilder {
  private VideoRecommenderContext recommendContext;

  public AtHomeRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
    this.recommendContext = recommendContext;
  }

  @Override
  public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
    RetrieveKey retrievekey = new RetrieveKey();
    retrievekey.setType(RetrieverType.vathome.name());
    retrievekey.setAlias(RetrieverType.vathome.alias());
    retrievekey.setKey(recommendContext.getRecommendRequest().getCid());
    retrievekey.setReason("在家锻炼");
    retrievekey.setScore(1.0);

    retrieveKeyContext.addRetrieveKey(retrievekey);
  }
}
