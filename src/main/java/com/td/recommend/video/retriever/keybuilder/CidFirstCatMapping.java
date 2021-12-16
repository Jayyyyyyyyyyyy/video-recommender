package com.td.recommend.video.retriever.keybuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/6/20.
 */
public class CidFirstCatMapping {

    public static final Map<String,String> cidFirstCatMap = new HashMap<>();
    static {
        cidFirstCatMap.put(CidFirstCat.JIANSHEN.cid,CidFirstCat.JIANSHEN.firstCat);
        cidFirstCatMap.put(CidFirstCat.YANGSHENG.cid,CidFirstCat.YANGSHENG.firstCat);
        cidFirstCatMap.put(CidFirstCat.MEISHI.cid,CidFirstCat.MEISHI.firstCat);
    }
    public enum CidFirstCat{
        YANGSHENG("801","124"),
        JIANSHEN("802","1006"),
        MEISHI("803","83");

        private String cid;
        private String firstCat;

        public String getFirstCat() {
            return firstCat;
        }
        public String getCid() {
            return cid;
        }

        private CidFirstCat(String cid, String firstCat) {
            this.cid = cid;
            this.firstCat = firstCat;
        }
    }

}