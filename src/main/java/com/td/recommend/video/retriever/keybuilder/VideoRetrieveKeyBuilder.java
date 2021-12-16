package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKeyBuilderPipeline;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.core.retriever.IRetrieveKeyBuilder;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/12/11.
 */
public class VideoRetrieveKeyBuilder implements IRetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(VideoRetrieveKeyBuilder.class);
    private final VideoRecommenderContext recommenderContext;
    private RetrieveKeyBuilderPipeline<UserItem> pipeline;

    public VideoRetrieveKeyBuilder(RetrieveKeyBuilderPipeline<UserItem> pipeline, VideoRecommenderContext recommenderContext) {
        this.pipeline = pipeline;
        this.recommenderContext = recommenderContext;
    }

    @Override
    public Collection<RetrieveKey> build(Items items) {
        RetrieveKeyContext retrieveKeyContext = new RetrieveKeyContext();

        Optional<IItem> userItemOpt = items.get(ItemKey.user);
        if (!userItemOpt.isPresent()) {
            LOG.error("Get UserItem items failed!");
            return Collections.emptySet();
        }

        UserItem userItem = (UserItem) userItemOpt.get();
        pipeline.build(userItem, retrieveKeyContext);


        return buildRetrieveKeySet(retrieveKeyContext.getRetrieveKeys());
    }

    private Set<RetrieveKey> buildRetrieveKeySet(List<RetrieveKey> retrieveKeys) {
        Map<RetrieveKey, RetrieveKey> retrieveKeyMap = new HashMap<>();
        for (RetrieveKey retrieveKey : retrieveKeys) {
            if (retrieveKey.getScore() < 0) {
                continue;
            }

            RetrieveKey targetKey = retrieveKeyMap.get(retrieveKey);
            if (targetKey == null) {
                retrieveKeyMap.put(retrieveKey, retrieveKey);
            } else {
                Set<String> keyTags = retrieveKey.getTags();
                targetKey.addTags(keyTags);
            }
        }

        return retrieveKeyMap.values().stream().collect(Collectors.toSet());
    }
}
