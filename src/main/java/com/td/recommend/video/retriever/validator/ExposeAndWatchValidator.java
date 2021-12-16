package com.td.recommend.video.retriever.validator;

import com.td.featurestore.item.IItem;
import com.td.featurestore.item.Id;
import com.td.recommend.core.cache.ImmunityGetter;
import com.td.recommend.validator.IBatchValidator;
import com.td.recommend.video.history.ClickHistory;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExposeAndWatchValidator implements IBatchValidator<IItem> {
    private VideoRecommenderContext recommendContext;

    public ExposeAndWatchValidator(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public List<IItem> filter(List<IItem> items) {
        String userId = recommendContext.getUserItem().getId();
        List<String> ids = items.stream().map(Id::getId).collect(Collectors.toList());
        Set<String> watched = ClickHistory.getInstance().watched(userId, ids);
        Set<String> exposeHistory = recommendContext.getUsed();
        List<String> specialList = ImmunityGetter.getInstance().getItemList();

        return items.stream()
                .filter(item -> ((specialList.contains(item.getId())) || (!watched.contains(item.getId()) && !exposeHistory.contains(item.getId()))))
                .collect(Collectors.toList());
    }
}
