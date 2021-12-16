package com.td.recommend.video.retriever.requestbuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.requestbuilder.RetrieveKeyHttpRequestBuilder;
import com.td.recommend.retriever.engine.request.HttpRequest;
import com.typesafe.config.Config;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class ItemCFRequestBuilder implements RetrieveKeyHttpRequestBuilder {
  private static final String URI = "/recall/itemcf";
  private int num = 10;

  private ItemCFRequestBuilder() {
  }

  public ItemCFRequestBuilder(Config config) {
    if (config.hasPath("num")) {
      this.num = config.getInt("num");
    }

  }

  public CompletableFuture<Optional<HttpRequest>> apply(RetrieveKey retrieveKey) {
    Map<String, String> requestMap = new TreeMap<>();
    requestMap.put("bucket", retrieveKey.getPlaceholder());
    requestMap.put("key", retrieveKey.getKey());
    requestMap.put("type", retrieveKey.getType());
    requestMap.put("num", String.valueOf(this.num));

    return CompletableFuture.completedFuture(Optional.of(new HttpRequest(URI, requestMap)));
  }
}
