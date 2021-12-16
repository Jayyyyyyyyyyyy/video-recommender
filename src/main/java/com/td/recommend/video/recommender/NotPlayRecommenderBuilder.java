package com.td.recommend.video.recommender;

import com.td.featurestore.datasource.ItemDataSource;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.Items;
import com.td.featurestore.item.ItemsProcessors;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.blender.IBlendPolicy;
import com.td.recommend.core.blender.IBlender;
import com.td.recommend.core.blender.ItemIdValidator;
import com.td.recommend.core.blender.MultiQueueRoundRobinBlender;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.recommender.RecommenderBuilder;
import com.td.recommend.core.retriever.IRetrieveKeyBuilder;
import com.td.recommend.core.retriever.IRetriever;
import com.td.recommend.core.validator.IValidator;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.retriever.RetrieveContextBuilder;
import com.td.recommend.retriever.RetrieverEngineBasedRetriever;
import com.td.recommend.retriever.engine.core.RetrieveEngine;
import com.td.recommend.validator.IBatchValidator;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.preprocessor.PreprocessorsFactory;
import com.td.recommend.video.rank.predictor.FeedPredictor;
import com.td.recommend.video.retriever.RetrieveEngineSingleton;
import com.td.recommend.video.retriever.blender.RetrieveBlendPolicy;
import com.td.recommend.video.retriever.keybuilder.notplay.NotPlayRetrieveKeyBuilderFactory;
import com.td.recommend.video.retriever.validator.ExposesValidator;

import java.util.Collections;
import java.util.concurrent.ExecutorService;

/**
 * Created by admin on 2017/12/7.
 */
public class NotPlayRecommenderBuilder extends RecommenderBuilder<DocItem> {

    private int num = 550;

    public NotPlayRecommenderBuilder(VideoRecommenderContext recommendContext) {
        super(recommendContext);
    }

    public NotPlayRecommenderBuilder(VideoRecommenderContext recommendContext, int num) {
        super(recommendContext);
        this.num = num;
    }

    @Override
    protected String buildName() {
        return "notplay-recommender";
    }

    @Override
    protected Items buildQueryItems() {
        return getVideoRecommendContext().getQueryItems();
    }

    @Override
    protected ItemDataSource<DocItem> buildItemDataSource() {
        return UserVideoItemDataSource.getInstance();
    }

    @Override
    protected IRetrieveKeyBuilder buildRetrieveKeyBuilder() {
        VideoRecommenderContext videoRecommendContext = this.getVideoRecommendContext();

        return NotPlayRetrieveKeyBuilderFactory.getInstance().create(videoRecommendContext);

    }

    @Override
    protected IRetriever buildRetriever() {
        RetrieveEngine<IItem> retrieveEngine = RetrieveEngineSingleton.getInstance().getRetrieveEngine();
        IBatchValidator<IItem> historyBatchValidator;
        historyBatchValidator = new ExposesValidator(getVideoRecommendContext());
        return new RetrieverEngineBasedRetriever(retrieveEngine, new RetrieveContextBuilder(), recommendContext, historyBatchValidator);
    }

    @Override
    protected IBlender<RetrieveKey, IItem> buildRetrieveBlender() {
        IBlendPolicy<RetrieveKey> blendPolicy;
        blendPolicy = new RetrieveBlendPolicy();
        if (this.recommendContext.getBuckets().contains("rank_num-base")) {
            blendPolicy.setBlendNum(this.num);
        } else {
            blendPolicy.setBlendNum(800);
        }
        return new MultiQueueRoundRobinBlender<>(blendPolicy,
                new ItemIdValidator<>(Collections.emptySet()));
    }

    @Override
    protected IValidator<IItem> buildRetrieveValidator() {
        return new ItemIdValidator<>(Collections.emptySet());
    }

    @Override
    protected ItemsProcessors<PredictItems<DocItem>> buildPreProcessors() {

        return PreprocessorsFactory.create(getVideoRecommendContext());

    }

    @Override
    protected IPredictor buildPredictor() {
        return new FeedPredictor(getVideoRecommendContext(), false);
    }

    @Override
    protected IPredictor buildBasePredictor() {
        return null;
    }

    @Override
    protected IBlender<RetrieveKey, IItem> buildBaseBlender() {
        IBlendPolicy<RetrieveKey> blendPolicy;
        blendPolicy = new RetrieveBlendPolicy();
        blendPolicy.setBlendNum(800);
        return new MultiQueueRoundRobinBlender<>(blendPolicy,
                new ItemIdValidator<>(this.recommendContext.getUsed()));

    }

    @Override
    protected ItemsProcessors<PredictItems<DocItem>> buildPostProcessors() {
        ItemsProcessors<PredictItems<DocItem>> processors = new ItemsProcessors<>();

        return processors;
    }

    private VideoRecommenderContext getVideoRecommendContext() {
        return (VideoRecommenderContext) this.recommendContext;
    }

    protected ExecutorService buildExecutorService() {
//        return ApplicationSharedExecutorService.getInstance().getExecutorService();
        return null;
    }
}
