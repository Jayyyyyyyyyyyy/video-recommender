package com.td.recommend.video.preprocessor;

import com.td.featurestore.item.ItemsProcessor;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Frang on 2018/7/25.
 */
public class RetrieveKeyFixPreprocessor implements ItemsProcessor<PredictItems<DocItem>> {
    private static final Set<String> retrieverTypes = new HashSet<>();

    static {
        retrieverTypes.add(RetrieverType.vcat.name());
        retrieverTypes.add(RetrieverType.vsubcat.name());
        retrieverTypes.add(RetrieverType.vtag.name());
    }

    private static char fixChar = '@';

    @Override
    public PredictItems<DocItem> process(PredictItems<DocItem> predictItems) {
        for (PredictItem<DocItem> predictItem : predictItems) {
            List<RetrieveKey> retrieveKeys = predictItem.getRetrieveKeys();
            for (RetrieveKey retrieveKey : retrieveKeys) {
                String key = retrieveKey.getKey();
                if (key != null) {
                    int index = key.indexOf(fixChar);
                    if (index != -1) {
                        retrieveKey.setKey(key.substring(index));
                    }
                }
            }
        }
        return predictItems;
    }
}
