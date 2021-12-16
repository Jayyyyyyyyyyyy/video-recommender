package com.td.recommend.video.retriever.blender;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.blender.IBlendPolicy;
import com.td.recommend.core.blender.QueuePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by admin on 2017/6/10.
 */
public class RetrieveBlendPolicy implements IBlendPolicy<RetrieveKey> {
    private int num = 550;
    Logger logger = LoggerFactory.getLogger(RetrieveBlendPolicy.class);

    @Override
    public QueuePolicy getQueuePolicy(RetrieveKey retrieveKey) {
        int maxCnt = (int) retrieveKey.getAttribute("maxCnt").orElse(num);
        QueuePolicy queuePolicy = new QueuePolicy();
        queuePolicy.setMaxCnt(maxCnt);
        return queuePolicy;
    }

    @Override
    public int getBlendNum() {
        return num;
    }

    @Override
    public void setBlendNum(int num) {
        this.num = num;
    }
}
