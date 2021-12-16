//package com.td.recommend.video.retriever.keybuilder;
//
//import com.td.data.profile.TVariance;
//import com.td.recommend.commons.profile.UserProfileUtils;
//import com.td.recommend.commons.retriever.RetrieveKey;
//import com.td.recommend.commons.retriever.RetrieveKeyContext;
//import com.td.recommend.userstore.data.UserItem;
//import com.td.recommend.video.recommender.VideoRecommenderContext;
//import com.td.recommend.video.retriever.RetrieverType;
//import com.td.recommend.video.retriever.explorer.Arm;
//import com.td.recommend.video.retriever.explorer.MultiArmedBandit;
//import org.apache.commons.lang3.tuple.ImmutablePair;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static org.apache.commons.lang3.tuple.ImmutablePair.of;
//
//public class SimEuRetrieveKeyBuilder implements RetrieveKeyBuilder {
//    private static final Logger LOG = LoggerFactory.getLogger(PriorityEuRetrieveKeyBuilder.class);
//    private VideoRecommenderContext recommendContext;
//    private static final int maxSize = 1;
//    private double minVar = 0.01;
//    private static Map<RetrieverType.Eu, Map<String, Double>> interestSimMap = new HashMap<>();
//    private static Double varLimit = 0.9;
//    private static Double meanLimit = 0.15;
//    private final static List<ImmutablePair<RetrieverType.Eu, String>> euSimList = Arrays.asList(
//            of(RetrieverType.Eu.vfitness_eu, "1009:0.594937 267:0.286411 1586:0.224121 1588:0.137613 1621:0.027156 1897:0.018231 1905:0.018014"),
//            of(RetrieverType.Eu.vhealth_eu, "1007:0.594937 267:0.278024 1588:0.205754 1586:0.130112 1621:0.038751 1897:0.033064 1905:0.030740"),
//            of(RetrieverType.Eu.v8step_eu, "1897:0.096747 1905:0.090882 267:0.083755 1588:0.057030 1586:0.043174 1009:0.038751 1007:0.027156"),
//            of(RetrieverType.Eu.v16step_eu, "1007:0.286411 1009:0.278024 1621:0.083755 1588:0.074754 1586:0.066535 1897:0.051903 1905:0.045347"),
//            of(RetrieverType.Eu.vneck2_eu, "1009:0.205754 1586:0.151711 1007:0.137613 267:0.074754 1621:0.057030 1897:0.052310 1905:0.052254"),
//            of(RetrieverType.Eu.vshape0_eu, "1897:0.194366 1621:0.090882 1588:0.052254 267:0.045347 1586:0.030972 1009:0.030740 1007:0.018014"),
//            of(RetrieverType.Eu.vethnic0_eu, "1905:0.194366 1621:0.096747 1588:0.052310 267:0.051903 1009:0.033064 1586:0.032718 1007:0.018231"),
//            of(RetrieverType.Eu.vlazyman_eu, "1007:0.224121 1588:0.151711 1009:0.130112 267:0.066535 1621:0.043174 1897:0.032718 1905:0.030972")
//    );
//
//    static {
//        for (ImmutablePair<RetrieverType.Eu, String> interestSim : euSimList) {
//            RetrieverType.Eu interest = interestSim.left;
//            String[] tagSims = interestSim.right.split("\\s");
//            Map<String, Double> map = new HashMap<>();
//            for (String tagSim : tagSims) {
//                String[] tagAndSim = tagSim.split(":");
//                map.put(tagAndSim[0], Double.valueOf(tagAndSim[1]));
//            }
//            interestSimMap.put(interest, map);
//        }
//    }
//
//    public SimEuRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
//        this.recommendContext = recommendContext;
//    }
//
//    @Override
//    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
//        List<RetrieverType.Eu> euList = Arrays.stream(RetrieverType.Eu.values()).collect(Collectors.toList());
//        Set<RetrieveKey> retrieveKeys = generate(userItem, euList);
//        retrieveKeyContext.addRetrieveKeys(retrieveKeys);
//    }
//
//    private Set<RetrieveKey> generate(UserItem userItem, List<RetrieverType.Eu> euList) {
//        Map<String, RetrieverType.Eu> keyToType = euList.stream().collect(Collectors.toMap(RetrieverType.Eu::getKey, k -> k));
//        Map<String, Double> euTagSimMap = generateEuTagSimMap(userItem, euList);
//        List<Arm> arms = new ArrayList<>();
//        for (String key : euTagSimMap.keySet()) {
//            RetrieverType.Eu candidate = keyToType.get(key);
//            TVariance variance = UserProfileUtils.getVarianceFeatureMap(userItem, candidate.getFacet()).get(candidate.getKey());
//            TVariance shortVariance = UserProfileUtils.getVarianceFeatureMap(userItem, "st_" + candidate.getFacet()).get(candidate.getKey());
//            double negCnt = getNegCnt(variance, shortVariance);
//            double posCnt = getPosCnt(variance, shortVariance);
//            double missCnt = negCnt - posCnt > 0 ? negCnt - posCnt : 0;
//            Arm arm = new Arm(candidate.getKey(), posCnt + 1, missCnt + 1);
//            arms.add(arm);
//
//        }
//        List<Arm> topArms = MultiArmedBandit.thompsonSampling(arms);
//        Set<RetrieveKey> keys = topArms.stream()
//                .filter(arm -> {
//                    if (arm.getVariance() > minVar) {
//                        return true;
//                    } else {
//                        LOG.info("eu candidate:{} removed because win:{}, loose:{}, diu:{}", arm.getName(), arm.getWin(), arm.getLoose(), userItem.getId());
//                        return false;
//                    }
//                })
//                .peek(arm -> {
//                    if (arm.getName().equals("1007")) {
//                        arm.setScore(10000);
//                    } else {
//                        arm.setScore(euTagSimMap.getOrDefault(arm.getName(), 0.0));
//                    }
//                })
//                .sorted(Comparator.comparing(Arm::getScore).reversed())
//                .limit(maxSize)
//                .map(arm -> {
//                    RetrieveKey retrieveKey = new RetrieveKey();
//                    retrieveKey.setType(keyToType.get(arm.getName()).name())
//                            .setAlias(keyToType.get(arm.getName()).getAlias())
//                            .setKey(arm.getName())
//                            .addAttribute("maxCnt", 5);
//                    return retrieveKey;
//                }).collect(Collectors.toSet());
//        return keys;
//
//    }
//
//    private Map<String, Double> generateEuTagSimMap(UserItem userItem, List<RetrieverType.Eu> euList) {
//        Set<RetrieverType.Eu> strongInterestSet = new HashSet<>();
//        Map<String, Double> euTagSimMap = new HashMap<>(interestSimMap.get(RetrieverType.Eu.vlazyman_eu));
//        for (RetrieverType.Eu candidate : euList) {
//            TVariance ck = UserProfileUtils.getVarianceFeatureMap(userItem, candidate.getFacet()).get(candidate.getKey());
//            TVariance cs = UserProfileUtils.getVarianceFeatureMap(userItem, candidate.getFacet().replaceAll("_ck", "_cs")).get(candidate.getKey());
//            if ((cs != null && cs.getVariance() < varLimit && cs.getMean() > meanLimit) ||
//                    (ck != null && ck.getVariance() < varLimit && ck.getMean() > meanLimit)) { //过滤强兴趣
//                strongInterestSet.add(candidate);
//                Map<String, Double> tagSimMap = interestSimMap.get(candidate);
//                if (tagSimMap != null) {
//                    tagSimMap.forEach((k, v) -> euTagSimMap.merge(k, v, Double::sum));
//                }
//            } else if (!interestSimMap.containsKey(candidate)) {
//                euTagSimMap.put(candidate.getKey(), 0.0);
//            }
//        }
//        for (RetrieverType.Eu key : strongInterestSet) {
//            euTagSimMap.remove(key.getKey());
//        }
//        return euTagSimMap;
//    }
//
//    private static double getNegCnt(TVariance longVariance, TVariance shortVariance) {
//        double negCnt = 0.0;
//
//        if (longVariance != null) {
//            negCnt += longVariance.getNegCnt();
//        }
//
//        if (shortVariance != null) {
//            negCnt += shortVariance.getNegCnt();
//        }
//
//        return negCnt;
//    }
//
//    private static double getPosCnt(TVariance longVariance, TVariance shortVariance) {
//        double posCnt = 0.0;
//
//        if (longVariance != null) {
//            posCnt += longVariance.getPosCnt();
//        }
//
//        if (shortVariance != null) {
//            posCnt += shortVariance.getPosCnt();
//        }
//
//        return posCnt;
//    }
//}
