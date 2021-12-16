package com.td.recommend.video.retriever.validator;

import com.td.recommend.core.blender.ItemIdValidator;
import com.td.featurestore.item.IItem;
import com.td.recommend.core.validator.IValidator;
import com.td.recommend.core.validator.Validators;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.Set;

/**
 * Created by admin on 2017/6/10.
 */
public class RetrieveValidator implements IValidator<IItem> {
    private Validators<IItem> validators = new Validators<>();

    public RetrieveValidator(VideoRecommenderContext recommendContext) {
        Set<String> used = recommendContext.getUsed();
        ItemIdValidator<IItem> validator = new ItemIdValidator<>(used);
        validators.add(validator);
    }

    @Override
    public boolean valid(IItem item) {
        return validators.valid(item);
    }
}
