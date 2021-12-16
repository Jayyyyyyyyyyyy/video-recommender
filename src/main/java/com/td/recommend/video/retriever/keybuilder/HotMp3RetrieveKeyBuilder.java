package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

public class HotMp3RetrieveKeyBuilder implements RetrieveKeyBuilder {
  private VideoRecommenderContext recommendContext;

  public HotMp3RetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
    this.recommendContext = recommendContext;
  }

  @Override
  public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
    RetrieveKey retrievekey = new RetrieveKey();
    retrievekey.setType(RetrieverType.vhotmp3.name());
    retrievekey.setAlias(RetrieverType.vhotmp3.alias());
    retrievekey.setKey(recommendContext.getRecommendRequest().getCid());
    retrievekey.setScore(1.0);

    retrieveKeyContext.addRetrieveKey(retrievekey);
  }
}
