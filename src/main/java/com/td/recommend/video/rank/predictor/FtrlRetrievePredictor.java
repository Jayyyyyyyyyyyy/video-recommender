package com.td.recommend.video.rank.predictor;

import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.idgenerator.PredictIdGenerator;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.concurrent.ApplicationSharedExecutorService;
import com.td.recommend.video.rank.featuredumper.UserNewsFeatureDumperSampler;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class FtrlRetrievePredictor implements IPredictor<DocItem> {
    private static final Logger LOG = LoggerFactory.getLogger(FtrlRetrievePredictor.class);
    private VideoRecommenderContext recommendContext;
    String modelName;
    private InnerPredictor innerPredictor;

    private ExecutorService executorService = ApplicationSharedExecutorService.getInstance().getExecutorService();
    TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();

    public FtrlRetrievePredictor(VideoRecommenderContext recommendContext, boolean userRemote) {
        this.recommendContext = recommendContext;
        this.innerPredictor = new FtrlRetrieveInnerPredictor(recommendContext, 200);
    }

    private String generatePredictId(VideoRecommenderContext videoRecommenderContext) {
        String version = videoRecommenderContext.getRecommendRequest().getVersion();
        String diu = videoRecommenderContext.getRecommendRequest().getDiu();
        if (version.compareTo("6.8.6.121622") > 0) {
            return PredictIdGenerator.getInstance().generateNew("");
        } else {
            return PredictIdGenerator.getInstance().generate(diu);
        }
    }
    private Map<String, Long> getRetrieveTypeCountMap(PredictItems<DocItem> predictItems) {
        return predictItems.getItems().stream().
                flatMap(i -> i.getRetrieveKeys().stream().map(RetrieveKey::getType))
                .collect(Collectors.groupingBy(key -> key, Collectors.counting()));
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {

        String predictId = generatePredictId(recommendContext);
        if (predictItems.isEmpty()) {
            PredictResult predictResult = new PredictResult();
            predictResult.setPredictId(predictId);
            predictResult.setScores(Collections.emptyList());
            return predictResult;
        }

        Map<String, Long> baserankMap = getRetrieveTypeCountMap(predictItems);
        LOG.info("base rank retrieve type size:{}",baserankMap);
        Optional<PredictResult> predictResultOpt = innerPredictor.predict(predictItems, queryItems, predictId);
        if (predictResultOpt.isPresent()) {
            LOG.info("ftrl base rank size:{}",predictItems.getItems().size());
            return predictResultOpt.get();
        } else {
            LOG.error("FtrlRetrievePredictor rank failed.");
            return null;
        }

    }
}
