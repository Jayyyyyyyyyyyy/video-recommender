package com.td.recommend.video.retriever.keybuilder;

import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.HorseConfigs;
import com.td.recommend.video.recommender.VideoRecommenderContext;

import java.util.Map;

/**
 * Created by admin on 2017/12/2.
 */
public class HorseRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public HorseRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        Map<String, HorseConfigs.Horse> horses = HorseConfigs.get(); //思颖将配置写入redis
        horses.forEach((horseName, horseConf) -> { // 每一个赛马都是一路召回
            double max_horse_view = horseConf.getConfig().getMax_horse_view();
            double cur_horse_view = horseConf.getStats().stream().map(HorseConfigs.Stat::getView).reduce(Double::sum).orElse(0.0);//当天此赛马所有视频view的总次数
            if (cur_horse_view < max_horse_view) { //当天所有池中视频view总次数小于max_horse_view
                TVariance cs = UserProfileUtils.getVarianceFeatureMap(userItem, horseConf.getConfig().getType() + "_cs").get(horseConf.getConfig().getKey());//type:vcat_cs, key:264
                TVariance ck = UserProfileUtils.getVarianceFeatureMap(userItem, horseConf.getConfig().getType() + "_ck").get(horseConf.getConfig().getKey());//type:vcat_ck, key:264
                if ((cs != null && cs.getMean() > 0.2 && cs.getVariance() < 0.9) ||  //获取cs ck值，来判断是否具有兴趣点
                        (ck != null && ck.getMean() > 0.1 && ck.getVariance() < 0.9)) { //对命中此兴趣点的用户进行召回
                    RetrieveKey retrieveKey = new RetrieveKey();
                    retrieveKey.setType(horseName) // v?_horse
                            .setAlias(horseConf.getConfig().getType()) // 264 直接写，retrievetype不需要写
                            .addAttribute("maxCnt", 5)
                            .setKey(horseConf.getConfig().getKey());
                    retrieveKeyContext.addRetrieveKey(retrieveKey);
                }
            }
        });
    }
}
