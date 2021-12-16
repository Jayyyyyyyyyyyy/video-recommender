package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by admin on 2017/12/2.
 */
public class SelfRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final Logger LOG = LoggerFactory.getLogger(SelfRetrieveKeyBuilder.class);

    public SelfRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String uid = recommendContext.getRecommendRequest().getUid();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        try{
            //uid有可能是unknown
            if (Integer.parseInt(uid) < 0) {
                return;
            }
            RetrieveKey retrievekey = new RetrieveKey();
            retrievekey.setIhf(String.valueOf(ihf));
            retrievekey.setType(RetrieverType.vself.name());
            retrievekey.setAlias(RetrieverType.vself.alias());
            retrievekey.setKey(uid);
            //加强限定，目前只在社区首页生效
            if(ihf == Ihf.VSHOWDANCE_FEED.id()){
                retrievekey.setReason("你的作品");
            }
            retrieveKeyContext.addRetrieveKey(retrievekey);
        } catch (Exception ignore) {
            //LOG.error("parse uid error uid:"+uid+",ihf:"+String.valueOf(ihf)+",diu:"+recommendContext.getRecommendRequest().getDiu());
        }
    }
}
