package com.td.recommend.video.rank.predictor;

import com.td.featurestore.item.Items;
import com.td.recommend.commons.idgenerator.PredictIdGenerator;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by admin on 2017/8/3.
 */
public class DoNothingPredictor implements IPredictor<DocItem> {
    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        UserItem userItem = UserProfileUtils.getUserItem(queryItems);
        String predictId = PredictIdGenerator.getInstance().generate(userItem.getId());

        PredictResult predictResult = new PredictResult();
        List<Double> scoreList = IntStream.range(0, predictItems.getSize())
                .mapToDouble(i -> Math.pow(0.999, i)).boxed()
                .collect(Collectors.toList());
        predictResult.setScores(scoreList);
        predictResult.setPredictId(predictId);
        predictItems.setModelName("none");

        return predictResult;

    }
}
