package com.td.recommend.video.preprocessor;

import com.td.featurestore.item.ItemsProcessor;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.datasource.Mp3CopyrightData;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Mp3CopyrightItemFilterPreprocessor implements ItemsProcessor<PredictItems<DocItem>> {
    private static final Logger LOG = LoggerFactory.getLogger(Mp3CopyrightItemFilterPreprocessor.class);
    private VideoRecommenderContext context;

    public Mp3CopyrightItemFilterPreprocessor(VideoRecommenderContext context) {
        this.context = context;
    }

    @Override
    public PredictItems<DocItem> process(PredictItems<DocItem> items) {

        Set<String> midRiskSet = Mp3CopyrightData.getMidRiskMp3Set();
        Set<String> highRiskSet = Mp3CopyrightData.getHighRiskMp3Set();
        List<PredictItem<DocItem>> predictItemList = new ArrayList<>();

        for (PredictItem<DocItem> item : items) {
            String mp3name = DocProfileUtils.getMp3(item.getItem());
            if (!midRiskSet.contains(mp3name) && !highRiskSet.contains(mp3name)) {
                predictItemList.add(item);
            }
        }

        PredictItems<DocItem> predictItems = new PredictItems<>();
        predictItems.setItems(predictItemList);

        LOG.info("mp3copyright filter before size:{},after size:{}, match size:{}",items.getSize(),predictItemList.size(),items.getSize()-predictItemList.size());
        return predictItems;
    }


}
