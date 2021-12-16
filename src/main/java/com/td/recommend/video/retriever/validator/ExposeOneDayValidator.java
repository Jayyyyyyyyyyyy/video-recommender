package com.td.recommend.video.retriever.validator;

import com.td.featurestore.item.IItem;
import com.td.recommend.validator.IBatchValidator;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExposeOneDayValidator implements IBatchValidator<IItem> {
    private VideoRecommenderContext recommendContext;

    public ExposeOneDayValidator(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public List<IItem> filter(List<IItem> items) {
        Set<String> exposeHistory = new HashSet<>(recommendContext.getOneDayExposes());
        return items.stream()
                .filter(item -> !exposeHistory.contains(item.getId()))
                .collect(Collectors.toList());
    }
}
