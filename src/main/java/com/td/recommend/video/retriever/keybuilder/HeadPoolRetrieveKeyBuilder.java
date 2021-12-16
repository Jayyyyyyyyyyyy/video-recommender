package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;

public class HeadPoolRetrieveKeyBuilder  implements RetrieveKeyBuilder {
  private VideoRecommenderContext recommendContext;

  public HeadPoolRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
    this.recommendContext = recommendContext;
  }

  @Override
  public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
    RetrieveKey retrievekey = new RetrieveKey();
    retrievekey.setType(RetrieverType.vheadpool.name());
    retrievekey.setKey(recommendContext.getRecommendRequest().getCid());
    retrievekey.setAlias(RetrieverType.vheadpool.alias());
    retrievekey.setScore(1.0);

    retrieveKeyContext.addRetrieveKey(retrievekey);
  }
}
