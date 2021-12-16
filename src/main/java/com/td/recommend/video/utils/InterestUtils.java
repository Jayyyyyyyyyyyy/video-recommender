package com.td.recommend.video.utils;

import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.userstore.data.UserItem;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;

public class InterestUtils {
    private static final double wilsonLimit = 0.1;
    private static final double wilsonConfidence = 0.8;
    public static boolean isStrongInterest(Map.Entry<String, TVariance> entry, String facetName) {
        TVariance var = entry.getValue();
        double varLimit, meanLimit;
        if (facetName.startsWith("st_")) {
            double wilsonScore = WilsonInterval.lowerBound(var.getPosCnt(), var.getNegCnt(), wilsonConfidence);
            return wilsonScore > wilsonLimit * 0.8;
        } else {
            varLimit = 0.9;
            meanLimit = facetName.endsWith("_cs") ? 0.2 : 0.1;
            return var.getVariance() < varLimit && var.getMean() > meanLimit;
        }
    }
    public static boolean isStrongInterest(TVariance var, String facetName) {
        double varLimit, meanLimit;
        if (facetName.startsWith("st_")) {
            double wilsonScore = WilsonInterval.lowerBound(var.getPosCnt(), var.getNegCnt(), wilsonConfidence);
            return wilsonScore > wilsonLimit * 0.8;
        } else {
            varLimit = 0.9;
            meanLimit = facetName.endsWith("_cs") ? 0.2 : 0.1;
            return var.getVariance() < varLimit && var.getMean() > meanLimit;
        }
    }
    //画像中无1006强兴趣
    public static boolean isStrongInterest(UserItem userItem, String interest , ImmutablePair<String, String> facetPair){
        boolean isCkStrongInterest,isCsStrongInterest;
        TVariance variance_ck = UserProfileUtils.getVarianceFeatureMap(userItem, facetPair.left).get(interest);
        TVariance variance_cs = UserProfileUtils.getVarianceFeatureMap(userItem, facetPair.right).get(interest);
        if(variance_ck == null){
            isCkStrongInterest = false;
        }else {
            isCkStrongInterest = InterestUtils.isStrongInterest(variance_ck, facetPair.left);
        }
        if(variance_cs == null){
            isCsStrongInterest = false;
        }else {
            isCsStrongInterest = InterestUtils.isStrongInterest(variance_cs, facetPair.right);
        }
        if(isCkStrongInterest || isCsStrongInterest){
            return true;
        }
        return false;
    }
    public static boolean isNotStrongInterest(UserItem userItem, String interest , ImmutablePair<String, String> facetPair) {
        return ! isStrongInterest(userItem,interest,facetPair);
    }
    public static boolean isWeakInterest(Map.Entry<String, TVariance> entry, String facetName) {
        TVariance var = entry.getValue();
        double varLimit, meanWeak;
        if (facetName.startsWith("st_")) {
            double wilsonScore = WilsonInterval.lowerBound(var.getPosCnt(), var.getNegCnt(), wilsonConfidence);
            return wilsonScore > wilsonLimit * 0.8;
        } else {
            varLimit = 0.9;
            meanWeak = facetName.endsWith("_cs") ? 0.1 : 0.1;
            return var.getVariance() < varLimit && var.getMean() > meanWeak;
        }
    }



    public static boolean isMoreWeakInterest(UserItem userItem, String interest , ImmutablePair<String, String> facetPair){
        boolean isCkStrongInterest,isCsStrongInterest;
        TVariance variance_ck = UserProfileUtils.getVarianceFeatureMap(userItem, facetPair.left).get(interest);
        TVariance variance_cs = UserProfileUtils.getVarianceFeatureMap(userItem, facetPair.right).get(interest);
        if(variance_ck == null){
            isCkStrongInterest = false;
        }else {
            isCkStrongInterest = InterestUtils.isMoreWeakInterest(variance_ck, facetPair.left);
        }
        if(variance_cs == null){
            isCsStrongInterest = false;
        }else {
            isCsStrongInterest = InterestUtils.isMoreWeakInterest(variance_cs, facetPair.right);
        }
        if(isCkStrongInterest || isCsStrongInterest){
            return true;
        }
        return false;
    }

    public static boolean isMoreWeakInterest(TVariance var, String facetName) {
        double varLimit, meanWeak;
        if (facetName.startsWith("st_")) {
            double wilsonScore = WilsonInterval.lowerBound(var.getPosCnt(), var.getNegCnt(), wilsonConfidence);
            return wilsonScore > wilsonLimit * 0.8;
        } else {
            varLimit = 0.9;
            meanWeak = facetName.endsWith("_cs") ? 0.0 : 0.1;
            return var.getVariance() < varLimit && var.getMean() > meanWeak;
        }
    }
}
