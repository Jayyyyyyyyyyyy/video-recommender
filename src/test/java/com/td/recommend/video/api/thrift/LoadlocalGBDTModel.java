package com.td.recommend.video.api.thrift;

import com.td.recommend.commons.rank.model.GBDTModel2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjl on 2019/7/30.
 */
public class LoadlocalGBDTModel {


    public static void main(String[] args) {
//        String path = "/Users/zjl/model.dump.txt";
        String path1 = "/data/gbdt_model/gbt_0100.model";
        GBDTModel2 gbdtModel2 = new GBDTModel2(path1);
        Long modelVersion = gbdtModel2.getModelVersion();
        System.out.println(modelVersion);
        List<String> x = new ArrayList<>();
        x.add("1");x.add("1");x.add("1");x.add("1");x.add("1");x.add("1");x.add("1");x.add("1");
        System.out.println(x.subList(0,8).size());
        System.out.println(x.get(8));
    }

}
