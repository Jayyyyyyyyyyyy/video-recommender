package com.td.recommend.video.datasource;

import com.td.featurestore.datasource.ItemDAO;
import com.td.featurestore.datasource.ItemDataSource;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.recommend.bucket.core.BucketGetter;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.docstore.dao.DocItemDao;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by admin on 2017/6/8.
 */
public class UserVideoItemDataSource implements ItemDataSource<DocItem> {
    private static UserVideoItemDataSource instance = null;
    private UserItemDao userItemDao;
    private DocItemDao docItemDao;

    public static UserVideoItemDataSource getInstance() {
        if (instance == null) {
            synchronized (UserVideoItemDataSource.class) {
                if (instance == null) {
                    instance = new UserVideoItemDataSource();
                }
            }
        }

        return instance;
    }

    private UserVideoItemDataSource() {
        userItemDao = new UserItemDao();
        docItemDao = new DocItemDao();
    }


    public Map<String, ItemDAO<? extends IItem>> getQueryDAOs() {
        Map<String, ItemDAO<? extends IItem>> queryDAOs = new HashMap<>();
        queryDAOs.put(ItemKey.user.name(), userItemDao);
        queryDAOs.put(ItemKey.doc.name(), docItemDao);
        return queryDAOs;
    }

    public ItemDAO<DocItem> getCandidateDAO() {//add cache should
        return docItemDao;
    }

    public UserItemDao getUserItemDao() {
        return userItemDao;
    }

    public static void main(String[] args) {//1500664260689
        Set<String> strings = BucketGetter.get("897C4F54-8CB9-4606-A01E-057D7D6A076A");
        System.out.println(strings);
        UserItemDao userItemDao = new UserItemDao();
        Optional<UserItem> userItem = userItemDao.get("868241041237414");
        UserItem userItem1 = userItem.get();
        Optional<UserItem> userItem2Opt = userItemDao.getEu("868241041237414");
        UserItem userItem2 = userItem2Opt.get();

        Optional<UserItem> userItem3Opt = userItemDao.newGet("868241041237414");
        UserItem userItem3 = userItem3Opt.get();


        Optional<DocItem> docItem = new DocItemDao().get("1500676169142");
        docItem = new DocItemDao().get("1500678261825");
        DocItem docItem1 = docItem.get();
        double textQuality = DocProfileUtils.getTextQuality(docItem1);
        System.out.println(textQuality==2.0);
        System.out.println(textQuality==2);
        DocItem docItem2 = new DocItem("0");
        System.out.println(docItem2.getId());


        System.out.println(docItem2.getNewsDocumentData().get().getStaticDocumentData().get().getTitle());

    }
}
