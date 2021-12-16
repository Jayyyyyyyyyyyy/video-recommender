package com.td.recommend.video.recommender;

import com.td.featurestore.datasource.ItemDataSource;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.Items;
import com.td.featurestore.item.ItemsProcessors;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.request.Ihf;
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
import com.td.recommend.video.rank.predictor.*;
import com.td.recommend.video.retriever.RetrieveEngineSingleton;
import com.td.recommend.video.retriever.WithTrendRetrieverEngineBasedRetriever;
import com.td.recommend.video.retriever.blender.RetrieveBlendPolicy;
import com.td.recommend.video.retriever.keybuilder.VideoFeedRetrieveKeyBuilderFactory;
import com.td.recommend.video.retriever.validator.ExposesValidator;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by admin on 2017/12/7.
 */
public class VideoRecommenderBuilder extends RecommenderBuilder<DocItem> {

    private int num = 550;

    public VideoRecommenderBuilder(VideoRecommenderContext recommendContext) {
        super(recommendContext);
    }

    public VideoRecommenderBuilder(VideoRecommenderContext recommendContext, int num) {
        super(recommendContext);
        this.num = num;
    }

    @Override
    protected String buildName() {
        return "video-recommender";
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

        return VideoFeedRetrieveKeyBuilderFactory.getInstance().create(videoRecommendContext);

    }

    @Override
    protected IRetriever buildRetriever() {
        RetrieveEngine<IItem> retrieveEngine = RetrieveEngineSingleton.getInstance().getRetrieveEngine();
        IBatchValidator<IItem> historyBatchValidator;
        historyBatchValidator = new ExposesValidator(getVideoRecommendContext());
        if((recommendContext.getRecommendRequest().getIhf()==Ihf.VMIX_FEED.id() || recommendContext.getRecommendRequest().getIhf() ==Ihf.VSHOWDANCE_FEED.id()) &&
                recommendContext.getRecommendRequest().getFp()==1){
            return new WithTrendRetrieverEngineBasedRetriever(retrieveEngine, new RetrieveContextBuilder(), (VideoRecommenderContext)recommendContext, historyBatchValidator);
        }
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
        Set<String> buckets = getVideoRecommendContext().getBuckets();
        IPredictor<DocItem> predictor;
        predictor = new FeedPredictor(getVideoRecommendContext(), true);
//        predictor = new TalentWeightedPredictor(predictor);

        predictor = new FeedFreshWeightedPredictor(predictor); //新鲜度加权

        if(! Ihf.isTrend(getVideoRecommendContext().getRecommendRequest().getIhf())){

            predictor = new HorseWeightedPredictor(predictor);
            if (buckets.contains("vid_boost-yes")) { //实验no 全量
                predictor = new VideoWeightedPredictor(predictor);
            }
            predictor = new ExploreVideoWeightedPredictor(predictor);
            predictor = new InterestFreshWeightedPredictor(predictor);

            if(buckets.contains("negative_feedback-exp")) { //实验exp 全量
                predictor = new NegativeFeedbackWeightedPredictor(predictor);
            }

//            if(buckets.contains("talentcluster-yes")){
//                predictor = new TalentClusterPredictor(predictor);
//            }
            //达人降权放入该实验召回里
            if(buckets.contains("talentcluster-yes") || buckets.contains("talentclusterv2-yes")){
                predictor = new TalentClusterV2Predictor(predictor);
            }
            else {
                predictor = new TalentWeightedPredictor(predictor);
            }
//            if(buckets.contains("talentclusterv2-yes")){
//                predictor = new TalentClusterPredictor(predictor);
//            }
        }
        else{
            //社区首页
            predictor = new TalentWeightedPredictor(predictor);

            predictor = new ShowDanceFreshWeightedPredictor(predictor);

        }

        return predictor;
    }

    @Override
    protected IPredictor buildBasePredictor() {
        Set<String> buckets = getVideoRecommendContext().getBuckets();
        if (buckets.contains("baserank-exp")) {
            //return new FeedPredictor(getVideoRecommendContext(), true);
            return new FtrlRetrievePredictor(getVideoRecommendContext(), false);
        } else {
            return null;
        }
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
