package com.td.recommend.video.retriever.blender;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.blender.IBlendPolicy;
import com.td.recommend.core.blender.QueuePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by admin on 2017/6/10.
 */
public class BaseBlendPolicy implements IBlendPolicy<RetrieveKey> {
    private int num = 1000;
    Logger logger = LoggerFactory.getLogger(BaseBlendPolicy.class);

    @Override
    public QueuePolicy getQueuePolicy(RetrieveKey retrieveKey) {
        QueuePolicy queuePolicy = new QueuePolicy();
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
