package com.td.recommend.video.retriever;

import com.typesafe.config.Config;
import com.td.featurestore.item.IItem;
import com.td.recommend.retriever.engine.core.DefaultRetrieveEngine;
import com.td.recommend.retriever.engine.core.RetrieveEngine;
import com.td.recommend.video.utils.UserVideoConfig;

/**
 * Created by admin on 2017/6/10.
 */
public class RetrieveEngineSingleton {
    private volatile static RetrieveEngineSingleton instance = null;
    private RetrieveEngine<IItem> retrieveEngine;


    public static RetrieveEngineSingleton getInstance() {
        if (instance == null) {
            synchronized (RetrieveEngineSingleton.class) {
                if (instance == null) {
                    instance = new RetrieveEngineSingleton();
                }
            }
        }
        return instance;
    }

    private RetrieveEngineSingleton() {
        Config newsRecommendConfig = UserVideoConfig.getInstance().getRootConfig();
        Config retrieveEngineConfig = newsRecommendConfig.getConfig("retriever-engine");

        retrieveEngine = new DefaultRetrieveEngine<>(retrieveEngineConfig);
    }

    public RetrieveEngine<IItem> getRetrieveEngine() {
        return retrieveEngine;
    }
}
