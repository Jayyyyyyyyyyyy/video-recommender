package com.td.recommend.video.rank.predictor;

import com.td.data.profile.utils.DateUtils;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.LiveUserInfo;
import com.td.recommend.video.profile.UserProfileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LiveUserWeightedPredictor implements IPredictor<DocItem> {
    private IPredictor<DocItem> predictor;
    public LiveUserWeightedPredictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }
    private static final Logger log = LoggerFactory.getLogger(LiveUserWeightedPredictor.class);

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems);
        UserItem userItem = UserProfileHelper.getUserItem(queryItems);
        //离线(t-1)计算的diu感兴趣的次日liveuid:直播日期(天级别)
        Map<String, String> likedliveuid_livetimeMap = UserProfileHelper.getSValueFeaturesMap(userItem, "liveuid");
        Map<String, Double> needWeightUidMap = getLivingUserWeightMap(likedliveuid_livetimeMap);
        List<PredictItem<DocItem>> items = predictItems.getItems();
        int weightedVidCnt = 0;
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> predictItem = items.get(i);
            String uid = DocProfileUtils.getUid(predictItem.getItem());
            if(needWeightUidMap.containsKey(uid)){
                double weight = needWeightUidMap.get(uid);
                Double weightedScore = predictResult.getScores().get(i) * weight;
                predictResult.getScores().set(i, weightedScore);
                predictItem.addAttribute("liveboost",weight);
                weightedVidCnt += 1;
            }
        }
        log.info("in feed live user weightedVidCnt={} videos is weighted", weightedVidCnt);
        return predictResult;
    }
    private Map<String,Double> getLivingUserWeightMap(Map<String, String> likedliveuid_livetimeMap){
        Map<String, Double> needWeightUidMap = new HashMap<>();
        String liveDay = DateUtils.toDateTime(System.currentTimeMillis(),"yyyy-MM-dd");
        if(likedliveuid_livetimeMap.size()>0){
            //正在直播的uids
            Set<String> livingUidSet = LiveUserInfo.getLivingUidSet();
            //"0"表示redis里所有正在直播的用户的视频都需要加权
            if(likedliveuid_livetimeMap.containsKey("0")){
                if(likedliveuid_livetimeMap.get("0").equals(liveDay)) {
                    for (String uid : livingUidSet) {
                        needWeightUidMap.put(uid, LiveUserInfo.getUserWeight(uid));
                    }
                }
            }else {
                for(Map.Entry likedLiveuid_livetime:likedliveuid_livetimeMap.entrySet()){
                    String likedLiveUid = likedLiveuid_livetime.getKey().toString();
                    String liveTime = likedLiveuid_livetime.getValue().toString();
                    if(livingUidSet.contains(likedLiveUid) && liveTime.equals(liveDay)){
                        needWeightUidMap.put(likedLiveUid,LiveUserInfo.getUserWeight(likedLiveUid));
                    }
                }
            }
        }
        return needWeightUidMap;
    }
}
