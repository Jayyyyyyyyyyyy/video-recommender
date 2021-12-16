package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.searchhot.HotSearchGetter;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.List;

/**
 * create by pansm at 2019/08/01
 */
public class HotSearchRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public HotSearchRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        addHotSearchRetrieveKeysNormal(userItem, retrieveKeyContext, RetrieverType.hotsearchvmp3);
        addHotSearchRetrieveKeys(userItem, retrieveKeyContext, RetrieverType.hotsearchvteacher);
        addHotSearchRetrieveKeys(userItem,retrieveKeyContext,RetrieverType.hotsearchvdance);
    }

    private void addHotSearchRetrieveKeys(UserItem userItem, RetrieveKeyContext retrieveKeyContext, RetrieverType type) {
        List<String> searchedIds;
        try {
            //todo
            searchedIds = HotSearchGetter.getTopProfileByKeyName(type.alias());
        } catch (Exception e) {
            return;
        }
        if (searchedIds == null||searchedIds.size()==0) {
            return;
        }
        double p = Math.min(1.0, 3.0 / (searchedIds.size() + 0.1));
        BinomialDistribution bd = new BinomialDistribution(1, p);
        searchedIds.stream().filter(r -> bd.sample() == 1).forEach(key -> {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setKey(key);
            retrieveKey.setType(type.name());
            retrieveKey.setAlias(type.alias());
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        });
    }

    private void addHotSearchRetrieveKeysNormal(UserItem userItem, RetrieveKeyContext retrieveKeyContext, RetrieverType type) {
        List<String> searchedIds;
        try {
            //todo
            searchedIds = HotSearchGetter.getTopProfileByKeyName(type.alias());
        } catch (Exception e) {
            return;
        }
        if (searchedIds == null||searchedIds.size()==0) {
            return;
        }
        int max=2;
        int count=0;
        for (int i=0;i<searchedIds.size()&&count<max; i++) {
            String key = searchedIds.get(i);
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setKey(key);
            retrieveKey.setType(type.name());
            retrieveKey.setAlias(type.alias());
            retrieveKeyContext.addRetrieveKey(retrieveKey);
            count++;
        }
    }

    public static void main(String[] args) {
        UserItem userItem = new UserItemDao().get("A00000738E7460").get();
        List<String> searchedIds;
        try {
            searchedIds = userItem.getUserRawData().get()
                    .getFeaturesMap().get(RetrieverType.sevmp3.name()).getKey();

            System.out.println("ljk"+searchedIds);
        } catch (Exception e) {
            System.out.println(e);;
        }
    }
}
