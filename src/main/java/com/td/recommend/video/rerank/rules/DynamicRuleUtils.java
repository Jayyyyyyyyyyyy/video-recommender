package com.td.recommend.video.rerank.rules;

import com.td.data.profile.TVariance;
import com.td.featurestore.datasource.ItemDAO;
import com.td.featurestore.datasource.ItemDataSource;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.rerank.RatioRule;
import com.td.recommend.commons.rerank.RatioRuleBuilder;
import com.td.recommend.commons.rerank.RuleTag;
import com.td.recommend.commons.rerank.WindowType;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.CategoryMap;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.InterestUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicRuleUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicRuleUtils.class);


    private static ImmutablePair<String, String> vcat_facets = ImmutablePair.of("vcat_ck", "vcat_cs");
    private static ImmutablePair<String, String> vsubcat_facets = ImmutablePair.of("vsubcat_ck", "vsubcat_cs");

    private static final int BASE_RULE_ID = 3000;


    public static boolean isAllNotInterest(UserItem userItem, List<String> keylist, ImmutablePair<String, String> facets) {

        List<String> interestKeys = keylist.stream().filter(
                interest->{
                    if (InterestUtils.isMoreWeakInterest(userItem, interest, facets)) {
                        return true;
                    }
                    return false;
                }
        ).collect(Collectors.toList());
        return interestKeys.size()>0 ? false : true;
    }

    public static boolean isNotInterestByCk(UserItem userItem, String interest, String ckfacet) {
        TVariance tCkVariance = UserProfileUtils.getVarianceFeatureMap(userItem, ckfacet).get(interest);
        if (tCkVariance != null) {
            double negCnt = tCkVariance.getNegCnt();
            double posCnt = tCkVariance.getPosCnt();
            if (negCnt > 30 && posCnt/negCnt <0.02) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotInterestByCs(UserItem userItem, String interest, String csfacet) {
        TVariance tCsVariance = UserProfileUtils.getVarianceFeatureMap(userItem, csfacet).get(interest);
        if (tCsVariance != null) {
            double mean = tCsVariance.getMean();
            if (mean<0.0) {
                return true;
            }
        }else {
            return true;
        }
        return false;

    }

    public static boolean isNeedLimit(UserItem userItem, Map<String, List<String>> catSubcatList) {
        for (String firstcat : catSubcatList.keySet()) {
            List<String> subcatlist = catSubcatList.get(firstcat);
            if (!isAllNotInterest(userItem, subcatlist, vsubcat_facets)) {
                return true;
            }
        }
        return false;
    }

    public static List<RatioRule> generate(UserItem userItem, VideoRecommenderContext context, int windowsize ) {
        List<RatioRule> dynamicRules = new ArrayList<>();
        Map<String, List<String>> catSubcatList = CategoryMap.getInstance().getCatSubcatListMap();

        if (!isNeedLimit(userItem, catSubcatList)) {
            LOG.info("not need limit for diu:{}",userItem.getId());
            return dynamicRules;
        }
        int count = 0;
        for (String firstcat : catSubcatList.keySet()) {
            List<String> subcatlist = catSubcatList.get(firstcat);
            if (isAllNotInterest(userItem, subcatlist, vsubcat_facets)) {
                if (isNotInterestByCk(userItem, firstcat, "vcat_ck")
                        && isNotInterestByCs(userItem, firstcat,"vcat_cs")) {
                    RatioRule ratioRule = RatioRuleBuilder.create()
                            .setId(BASE_RULE_ID+count++)
                            .setPriority(2)
                            .setWindowSize(windowsize)
                            .setWindowType(WindowType.slide)
                            .setRuleTag(new RuleTag(firstcat, "firstcat"))
                            .setMax(1)
                            .build();
                    dynamicRules.add(ratioRule);
                    LOG.info("dynamic firstcat rule:{},diu:{},firstcat:{}",ratioRule.toString(),
                            context.getRecommendRequest().getDiu(), firstcat);
                }
            }

            for (String subcat : subcatlist) {
                if (isNotInterestByCk(userItem, subcat, "vsubcat_ck")
                        && isNotInterestByCs(userItem, subcat, "vsubcat_cs")) {
                    RatioRule ratioRule = RatioRuleBuilder.create()
                            .setId(BASE_RULE_ID + count++)
                            .setPriority(2)
                            .setWindowSize(windowsize)
                            .setWindowType(WindowType.slide)
                            .setRuleTag(new RuleTag(subcat, "secondcat"))
                            .setMax(1)
                            .build();
                    dynamicRules.add(ratioRule);
                }
            }

        }
        return dynamicRules;
    }



    public static List<RatioRule> generateSpecial(UserItem userItem, VideoRecommenderContext context) {
        List<RatioRule> dynamicRules = new ArrayList<>();
        Map<String, List<String>> catSubcatList = CategoryMap.getInstance().getCatSubcatListMap();

        int count = 0;
        for (String firstcat : catSubcatList.keySet()) {
            List<String> subcatlist = catSubcatList.get(firstcat);
            if (isAllNotInterest(userItem, subcatlist, vsubcat_facets)) {
                if (isNotInterestByCk(userItem, firstcat, "vcat_ck")
                && isNotInterestByCs(userItem, firstcat,"vcat_cs")) {
                    RatioRule ratioRule = RatioRuleBuilder.create()
                            .setId(BASE_RULE_ID+count++)
                            .setPriority(2)
                            .setWindowSize(4)
                            .setWindowType(WindowType.slide)
                            .setRuleTag(new RuleTag(firstcat, "firstcat"))
                            .setMax(1)
                            .build();
                    dynamicRules.add(ratioRule);
                    LOG.info("dynamic firstcat rule:{},diu:{},firstcat:{}",ratioRule.toString(),
                            context.getRecommendRequest().getDiu(), firstcat);
                }
            }

            for (String subcat : subcatlist) {
                if (isNotInterestByCk(userItem, subcat, "vsubcat_ck")
                        && isNotInterestByCs(userItem, subcat, "vsubcat_cs")) {
                    RatioRule ratioRule = RatioRuleBuilder.create()
                            .setId(BASE_RULE_ID + count++)
                            .setPriority(2)
                            .setWindowSize(6)
                            .setWindowType(WindowType.slide)
                            .setRuleTag(new RuleTag(subcat, "secondcat"))
                            .setMax(1)
                            .build();
                    dynamicRules.add(ratioRule);
                }
            }

        }
        return dynamicRules;
    }

    public static void main(String[] argv) {
        Map<String, List<String>> catSubcatList = CategoryMap.getInstance().getCatSubcatListMap();
        for (String cat : catSubcatList.keySet()) {
            System.out.println("cat:"+cat);
            System.out.println(String.join(",", catSubcatList.get(cat)));
        }
        ItemDataSource<DocItem> dataSource;
        dataSource = UserVideoItemDataSource.getInstance();

        Map<String, ItemDAO<? extends IItem>> queryDAOs = dataSource.getQueryDAOs();

        UserItemDao itemDAO = (UserItemDao) queryDAOs.get(ItemKey.user.name());
        UserItem userItem = itemDAO.get("6304c0101ec2e8f4").get();

        List<RatioRule> ratioRules = DynamicRuleUtils.generate(userItem, null, 6);

        ratioRules.forEach(
                rule -> {
                    System.out.println(rule.toString());
                }
        );

    }
}
