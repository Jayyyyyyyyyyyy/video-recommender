package com.td.recommend.video.retriever.validator;

import com.td.featurestore.item.IItem;
import com.td.recommend.validator.IBatchValidator;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExposesValidator implements IBatchValidator<IItem> {
    private VideoRecommenderContext recommendContext;

    public ExposesValidator(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public List<IItem> filter(List<IItem> items) {
        Set<String> exposeHistory = recommendContext.getUsed();
        return items.stream()
                .filter(item -> !exposeHistory.contains(item.getId()))
                .collect(Collectors.toList());
    }
}
