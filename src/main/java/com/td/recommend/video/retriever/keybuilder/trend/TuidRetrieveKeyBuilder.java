package com.td.recommend.video.retriever.keybuilder.trend;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by sunjian on 2021/09/10
 */
public class TuidRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public TuidRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String tuid = recommendContext.getRecommendRequest().getTrendUid();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        //只限社区首页的召回
        if(ihf==Ihf.VSHOWDANCE_FEED.id() && StringUtils.isNumeric(tuid)){
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setIhf(String.valueOf(recommendContext.getRecommendRequest().getIhf()));
            retrieveKey.setType(RetrieverType.tuidfollow.name());
            retrieveKey.setAlias(RetrieverType.tuidfollow.alias());
            retrieveKey.setKey(tuid);
            retrieveKeyContext.addRetrieveKey(retrieveKey);

        }
    }

    public static void main(String[] args) {
        UserItem userItem = new UserItemDao().get("11688183").get();
        Map<String, String> featureMap = UserProfileUtils.getSValueFeaturesMap(userItem, "tuidfollow");
        for (Map.Entry<String, String> entry : featureMap.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

}
