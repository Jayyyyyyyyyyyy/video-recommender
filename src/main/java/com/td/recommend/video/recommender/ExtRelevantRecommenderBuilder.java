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
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.preprocessor.RelevantPreprocessorsFactory;
import com.td.recommend.video.rank.predictor.FeedPredictor;
import com.td.recommend.video.retriever.RetrieveEngineSingleton;
import com.td.recommend.video.retriever.blender.RetrieveBlendPolicy;
import com.td.recommend.video.retriever.keybuilder.relevant.ExtRelevantVideoRetrieveKeyBuilderFactory;
import com.td.recommend.video.retriever.validator.RetrieveValidator;

import java.util.Set;

public class ExtRelevantRecommenderBuilder extends RecommenderBuilder<DocItem> {
    private int num = 550;

    public ExtRelevantRecommenderBuilder(VideoRecommenderContext recommendContext) {
        super(recommendContext);
    }

    public ExtRelevantRecommenderBuilder(VideoRecommenderContext recommendContext, int num) {
        super(recommendContext);
        this.num = num;
    }

    @Override
    protected String buildName() {
        return "relevant-recommender";
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
        return ExtRelevantVideoRetrieveKeyBuilderFactory.getInstance().create(videoRecommendContext);
    }

    @Override
    protected IRetriever buildRetriever() {
        RetrieveEngine<IItem> retrieveEngine = RetrieveEngineSingleton.getInstance().getRetrieveEngine();
        return new RetrieverEngineBasedRetriever(retrieveEngine, new RetrieveContextBuilder(), recommendContext, null);
    }

    @Override
    protected IBlender<RetrieveKey, IItem> buildRetrieveBlender() {
        IBlendPolicy<RetrieveKey> blendPolicy;
        blendPolicy = new RetrieveBlendPolicy();
        blendPolicy.setBlendNum(this.num);

        return new MultiQueueRoundRobinBlender<>(blendPolicy,
                new ItemIdValidator<>(this.recommendContext.getUsed()));
    }

    @Override
    protected IValidator<IItem> buildRetrieveValidator() {
        VideoRecommenderContext context = getVideoRecommendContext();
        return new RetrieveValidator(context);
    }

    @Override
    protected ItemsProcessors<PredictItems<DocItem>> buildPreProcessors() {
        return RelevantPreprocessorsFactory.create(getVideoRecommendContext());
    }

    @Override
    protected IPredictor buildPredictor() {
        Set<String> buckets = getVideoRecommendContext().getBuckets();
        IPredictor<DocItem> predictor ;//= new RelevantPredictor(getVideoRecommendContext(), true);
        predictor = new FeedPredictor(getVideoRecommendContext(), true);
//        if(buckets.contains("relevant_user_boost-yes")){
//            return new UserWeightedPredictor(predictor);
//        }else {
//            return predictor;
//        }
        return  predictor;
    }

    @Override
    protected ItemsProcessors<PredictItems<DocItem>> buildPostProcessors() {
        ItemsProcessors<PredictItems<DocItem>> processors = new ItemsProcessors<>();
        return processors;
    }

    private VideoRecommenderContext getVideoRecommendContext() {
        return (VideoRecommenderContext) this.recommendContext;
    }
}
