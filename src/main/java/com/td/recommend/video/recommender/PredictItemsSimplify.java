package com.td.recommend.video.recommender;

import com.td.data.profile.item.VideoItem;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.docstore.data.DocItem;

import java.util.Optional;

public class PredictItemsSimplify {
    public static void simplify(PredictItems<DocItem> predictItems) {
        predictItems.simplify(predictItem -> {
            DocItem docItem = predictItem.getItem();
            Optional<VideoItem> staticDocumentDataOpt = DocProfileUtils.getStaticDocumentData(docItem);

        });
    }
}