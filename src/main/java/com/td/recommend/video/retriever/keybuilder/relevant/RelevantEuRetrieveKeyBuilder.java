package com.td.recommend.video.retriever.keybuilder.relevant;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.td.data.profile.TVariance;
import com.td.data.profile.common.KeyConstants;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.explorer.Arm;
import com.td.recommend.video.retriever.explorer.MultiArmedBandit;
import com.td.recommend.video.retriever.keybuilder.PriorityEuRetrieveKeyBuilder;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import com.td.recommend.video.utils.InterestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RelevantEuRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(PriorityEuRetrieveKeyBuilder.class);
    private VideoRecommenderContext recommendContext;
    private static final int maxSize = 1;
    private double minVar = 0.0035;
    private double minVar_special = 0.0035;
    private static final Map<String, String> targetInterestGroup = ImmutableMap.<String, String>builder()
            .put("4113", "1009")
            .put("1705", "1009")
            .put("1034", "1006")
            .put("1010", "1006")
            .put("1007", "1006")
            .put("1053", "1009")
            .put("1094", "1007")
            .build();
    private  final static ImmutablePair vcat = ImmutablePair.of("vcat_ck","vcat_cs");
    private  final static ImmutablePair vsubcat = ImmutablePair.of("vsubcat_ck","vsubcat_cs");

    public RelevantEuRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }
    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<RetrieverType.RlvtEu> euList = Arrays.stream(RetrieverType.RlvtEu.values()).collect(Collectors.toList());
        Set<RetrieveKey> retrieveKeys = generate(userItem, euList);
        retrieveKeyContext.addRetrieveKeys(retrieveKeys);
    }

    private Optional<String> getFirstCat(String vid) {
        String firstcat = null;
        try {
            Optional<DocItem> docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(vid);
            firstcat = docItem.get()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(KeyConstants.firstcat).get()
                    .getId();
            return  Optional.ofNullable(firstcat);
        } catch (Exception e) {
            LOG.error("get firstcat by vid={} failed:{}",vid,e);
        }
        return Optional.empty();
    }

    private Optional<String> getSecondCat(String vid) {
        String secondcat = null;
        try {
            Optional<DocItem> docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(vid);
            secondcat = docItem.get()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(KeyConstants.secondcat).get()
                    .getId();
            return  Optional.ofNullable(secondcat);
        } catch (Exception e) {
            LOG.error("get firstcat by vid={} failed:{}",vid,e);
        }
        return Optional.empty();
    }

    private Set<RetrieveKey> generate(UserItem userItem, List<RetrieverType.RlvtEu> euList) {
        List<Arm> arms = new ArrayList<>();
        for (RetrieverType.RlvtEu candidate : euList) {
            TVariance variance = UserProfileUtils.getVarianceFeatureMap(userItem, candidate.getFacet()).get(candidate.getKey());
            TVariance shortVariance = new TVariance(0.0,0.0,0.0,0.0);
            //1009:中老年保健操感兴趣人群进行exercise_body=4113 eu
            if (ImmutableSet.of("1705", "4113", "1034", "1010").contains(candidate.getKey())) {
                String target_facet_prefix = candidate.getTarget_facet_prefix();
                ImmutablePair facets = ImmutablePair.of(target_facet_prefix+"_ck", target_facet_prefix+"_cs");
                if (InterestUtils.isStrongInterest(userItem, targetInterestGroup.get(candidate.getKey()), facets)) {
                    add2ArmList(candidate.getKey(),arms,variance,shortVariance);
                } else {
                    LOG.info("eu candidate:{} removed because this interest is not strongInterest, diu:{}",  candidate.getKey(), userItem.getId());
                }
            }else if (ImmutableSet.of("306", "305").contains(candidate.getKey())) {  //播放页一级类
                String vid = recommendContext.getRecommendRequest().getVid();
                Optional<String> firstcatOpt = getFirstCat(vid);
                if (firstcatOpt.isPresent() && firstcatOpt.get().equals("264")) {
                    add2ArmList(candidate.getKey(),arms,variance,shortVariance);
                } else {
                    LOG.info("eu candidate:{} removed because the firstcat of vid[{}] is not 264, diu:{}",  candidate.getKey(), vid);
                }
            } else if (ImmutableSet.of("1053", "1094").contains(candidate.getKey())) { //播放页二级类
                String vid = recommendContext.getRecommendRequest().getVid();
                Optional<String> secondcatOpt = getSecondCat(vid);
                if (secondcatOpt.isPresent()) {
                    String secondcat = secondcatOpt.get();
                    if (targetInterestGroup.get(candidate.getKey()).equals(secondcat)) {
                        add2ArmList(candidate.getKey(),arms,variance,shortVariance);
                    }else {
                        LOG.info("eu candidate:{} removed because the secondcat of vid[{}] is not target secondcat, vid:{}",  candidate.getKey(), vid);
                    }
                }

            } else if (candidate.getKey().equals("1007")) {
                ImmutablePair facets = ImmutablePair.of("vsubcat_ck", "vsubcat_cs");
                if (InterestUtils.isStrongInterest(userItem, "265", facets) ||
                        InterestUtils.isStrongInterest(userItem, "312", facets)) {
                    add2ArmList(candidate.getKey(),arms,variance,shortVariance);
                }else {
                    LOG.info("eu candidate:{} removed because this interest is strongInterest, diu:{}",  candidate.getKey(), userItem.getId());
                }
            } else if (candidate.getKey().equals("1009")) {
                String vid = recommendContext.getRecommendRequest().getVid();
                Optional<String> firstcatOpt = getFirstCat(vid);
                if (firstcatOpt.isPresent()) {
                    String firstcat = firstcatOpt.get();
                    if (firstcat.equals("264") || firstcat.equals("312")) {
                        add2ArmList(candidate.getKey(), arms, variance, shortVariance);
                    }else {
                        LOG.info("eu candidate:{} removed because the firstcat of vid[{}] is not 264,312, diu:{}",  candidate.getKey(), vid);
                    }
                }
            }
            else {
                add2ArmList(candidate.getKey(), arms, variance, shortVariance);
            }

        }

        List<Arm> topArms = MultiArmedBandit.thompsonSampling(arms);
        Map<String, RetrieverType.RlvtEu> keyToType = euList.stream().collect(Collectors.toMap(RetrieverType.RlvtEu::getKey, k -> k));
        Set<RetrieveKey> keys = topArms.stream()
                .filter(arm -> {
                    if(ImmutableSet.of("1705", "1588" ,"4113").contains(arm.getName())){
                        if (arm.getVariance() > minVar_special) {
                            return true;
                        }
                    }else {
                        if (arm.getVariance() > minVar) {
                            return true;
                        }
                    }
                    LOG.info("eu candidate:{} removed because win:{}, loose:{}, diu:{}", arm.getName(), arm.getWin(), arm.getLoose(), userItem.getId());
                    return false;
                })
                .sorted(Comparator.comparing(Arm::getVariance))
                .limit(maxSize)
                .map(arm -> {
                    RetrieveKey retrieveKey = new RetrieveKey();
                    retrieveKey.setType(keyToType.get(arm.getName()).name())
                            .setAlias(keyToType.get(arm.getName()).getAlias())
                            .setKey(arm.getName())
                            .addAttribute("maxCnt", 5);
                    return retrieveKey;
                }).collect(Collectors.toSet());
        return keys;

    }

    private static double getNegCnt(TVariance longVariance, TVariance shortVariance) {
        double negCnt = 0.0;

        if (longVariance != null) {
            negCnt += longVariance.getNegCnt();
        }

        if (shortVariance != null) {
            negCnt += shortVariance.getNegCnt();
        }

        return negCnt;
    }

    private static double getPosCnt(TVariance longVariance, TVariance shortVariance) {
        double posCnt = 0.0;

        if (longVariance != null) {
            posCnt += longVariance.getPosCnt();
        }

        if (shortVariance != null) {
            posCnt += shortVariance.getPosCnt();
        }

        return posCnt;
    }

    public static void add2ArmList(String facet, List arms,TVariance variance,TVariance shortVariance){
        double negCnt = getNegCnt(variance, shortVariance);
        double posCnt = getPosCnt(variance, shortVariance);
        double missCnt = negCnt - posCnt > 0 ? negCnt - posCnt : 0;
        Arm arm = new Arm(facet, posCnt + 1, missCnt + 1);
        arms.add(arm);
    }
}
