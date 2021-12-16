package com.td.recommend.video.rank.featureextractor;

import com.td.featurestore.feature.IFeature;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.docstore.data.DocItem;

import java.util.List;

/**
 * Created by admin on 2017/6/23.
 */
public interface GBDTFeatureExtractor {
    List<IFeature> extract(PredictItem<DocItem> predictItem, Items queryItems);
}
