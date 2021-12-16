package com.td.recommend.video.datasource;

import com.td.recommend.video.utils.UserVideoConfig;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryMap {
    private static final Logger LOG = LoggerFactory.getLogger(CategoryMap.class);

    private static CategoryMap instance = new CategoryMap();

    @Setter
    @Getter
    private Map<String, List<String>> catSubcatListMap = new HashMap<>();

    public static CategoryMap getInstance() {
        return instance;
    }

    private CategoryMap() {
        Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
        loadCategoryMap(userNewsConfig.getConfigList("category-mapping_list"));
    }

    private void loadCategoryMap(List<? extends Config> configList) {
        try {
            catSubcatListMap.clear();
            for (Config config : configList) {
                String firstcat = config.getString("firstcat");
                List<String> subcatList = config.getStringList("secondcat");
                catSubcatListMap.put(firstcat, subcatList);
            }
        }catch (Exception ex) {
            LOG.error("CategoryMapm loadCategoryMap failed:{}", ex);
        }
    }
}
