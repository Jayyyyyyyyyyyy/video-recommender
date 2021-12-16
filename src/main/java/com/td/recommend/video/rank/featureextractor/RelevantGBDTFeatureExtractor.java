package com.td.recommend.video.rank.featureextractor;

import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.td.data.profile.TVariance;
import com.td.featurestore.feature.Feature;
import com.td.featurestore.feature.IFeature;
import com.td.featurestore.feature.IFeatures;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

import static org.apache.commons.lang3.tuple.ImmutablePair.of;

/**
 * Created by admin on 2017/6/23.
 */
public class RelevantGBDTFeatureExtractor implements GBDTFeatureExtractor {
    private static final RelevantGBDTFeatureExtractor instance = new RelevantGBDTFeatureExtractor();

    public static RelevantGBDTFeatureExtractor getInstance() {
        return instance;
    }

    private static final String[] USER_FEATURES = new String[]{
            "vcat_cs", "vcat_ck",
            "vsubcat_cs", "vsubcat_ck",
            "vtag_cs", "vtag_ck",
            "vdance_cs", "vdance_ck",
            "vlen_cs", "vlen_ck",
            "vheadt_cs", "vheadt_ck",
            "vdifficulty_cs", "vdifficulty_ck",
            "vteach_cs", "vteach_ck",
            "vauthor_uid_cs", "vauthor_uid_ck",
            "vauthor_type_cs", "vauthor_type_ck",
            "vauthor_level_cs", "vauthor_level_ck",
            "vteam_cs", "vteam_ck",
            "vteamrole_cs", "vteamrole_ck",
            "vteacher_cs", "vteacher_ck",
            "vgenre_cs", "vgenre_ck",
            "vmp3_cs", "vmp3_ck",
            "vtitle_len_cs", "vtitle_len_ck",
            "vauthor_in_title_cs", "vauthor_in_title_ck",
            "vmusic_in_title_cs", "vmusic_in_title_ck",
            "vdance_in_title_cs", "vdance_in_title_ck",

            "diu_level", "dance_role", "dance_level", "daren_level"
    };


    private static final String[] SHORT_TERM_USER_FEATURES = new String[]{
            "st_vcat_cs", "st_vcat_ck",
            "st_vsubcat_cs", "st_vsubcat_ck",
            "st_vtag_cs", "st_vtag_ck",
            "st_vdance_cs", "st_vdance_ck",
            "st_vlen_cs", "st_vlen_ck",
            "st_vheadt_cs", "st_vheadt_ck",
            "st_vdifficulty_cs", "st_vdifficulty_ck",
            "st_vteach_cs", "st_vteach_ck",
            "st_vauthor_uid_cs", "st_vauthor_uid_ck",
            "st_vauthor_type_cs", "st_vauthor_type_ck",
            "st_vauthor_level_cs", "st_vauthor_level_ck",
            "st_vteam_cs", "st_vteam_ck",
            "st_vteamrole_cs", "st_vteamrole_ck",
            "st_vteacher_cs", "st_vteacher_ck",
            "st_vgenre_cs", "st_vgenre_ck",
            "st_vmp3_cs", "st_vmp3_ck",
            "st_vtitle_len_cs", "st_vtitle_len_ck",
            "st_vauthor_in_title_cs", "st_vauthor_in_title_ck",
            "st_vmusic_in_title_cs", "st_vmusic_in_title_ck",
            "st_vdance_in_title_cs", "st_vdance_in_title_ck",
    };


    private static final String[] CONTEXT_FEATURES = new String[]{
            "os", "hour", "device"
    };


    private static final String[] DOC_FEATURES = new String[]{
            "vcat",
            "vsubcat",
            "vtag",
            "vdance",
            "vlen",
            "vheadt",
            "vdifficulty",
            "vteach",
            "vauthor_uid",
            "vauthor_type",
            "vauthor_level",
            "vteam",
            "vteamrole",
            "vteacher",
            "vgenre",
            "vmp3",
            "vtitle_len",
            "vauthor_in_title",
            "vmusic_in_title",
            "vdance_in_title",
            "vcodance",
            "vquality",
            "stats",
            "dy_1day",
            "dy_15day",
            "dy_7day"
    };

    private final static List<ImmutablePair<String, String>> USER_DOC_CONJUNCTION_FEATURES = Arrays.asList(
            of("vcat_cs", "vcat"),
            of("vcat_ck", "vcat"),
            of("vsubcat_cs", "vsubcat"),
            of("vsubcat_ck", "vsubcat"),
            of("vtag_cs", "vtag"),
            of("vtag_ck", "vtag"),
            of("vdance_cs", "vdance"),
            of("vdance_ck", "vdance"),
            of("vlen_cs", "vlen"),
            of("vlen_ck", "vlen"),
            of("vheadt_cs", "vheadt"),
            of("vheadt_ck", "vheadt"),
            of("vdifficulty_cs", "vdifficulty"),
            of("vdifficulty_ck", "vdifficulty"),
            of("vteach_cs", "vteach"),
            of("vteach_ck", "vteach"),
            of("vauthor_uid_cs", "vauthor_uid"),
            of("vauthor_uid_ck", "vauthor_uid"),
            of("vauthor_type_cs", "vauthor_type"),
            of("vauthor_type_ck", "vauthor_type"),
            of("vauthor_level_cs", "vauthor_level"),
            of("vauthor_level_ck", "vauthor_level"),
            of("vteam_cs", "vteam"),
            of("vteam_ck", "vteam"),
            of("vteamrole_cs", "vteamrole"),
            of("vteamrole_ck", "vteamrole"),
            of("vteacher_cs", "vteacher"),
            of("vteacher_ck", "vteacher"),
            of("vgenre_cs", "vgenre"),
            of("vgenre_ck", "vgenre"),
            of("vmp3_cs", "vmp3"),
            of("vmp3_ck", "vmp3"),
            of("vtitle_len_cs", "vtitle_len"),
            of("vtitle_len_ck", "vtitle_len"),
            of("vauthor_in_title_cs", "vauthor_in_title"),
            of("vauthor_in_title_ck", "vauthor_in_title"),
            of("vmusic_in_title_cs", "vmusic_in_title"),
            of("vmusic_in_title_ck", "vmusic_in_title"),
            of("vdance_in_title_cs", "vdance_in_title"),
            of("vdance_in_title_ck", "vdance_in_title")
    );
    private final static List<ImmutablePair<String, String>> DOC_DOC_CONJUNCTION_FEATURES = Arrays.asList(
            of("vcat", "vcat"),
            of("vsubcat", "vsubcat"),
            of("vtag", "vtag"),
            of("vdance", "vdance"),
            of("vlen", "vlen"),
            of("vheadt", "vheadt"),
            of("vdifficulty", "vdifficulty"),
            of("vteach", "vteach"),
            of("vauthor_uid", "vauthor_uid"),
            of("vauthor_type", "vauthor_type"),
            of("vauthor_level", "vauthor_level"),
            of("vteam", "vteam"),
            of("vteamrole", "vteamrole"),
            of("vteacher", "vteacher"),
            of("vgenre", "vgenre"),
            of("vmp3", "vmp3"),
            of("vtitle_len", "vtitle_len"),
            of("vauthor_in_title", "vauthor_in_title"),
            of("vmusic_in_title", "vmusic_in_title"),
            of("vdance_in_title", "vdance_in_title"),
            of("vdance_in_title", "vdance_in_title")
    );
    private final static List<ImmutablePair<String, String>> SHORT_TERM_USER_DOC_CONJUNCTION_FEATURES = Arrays.asList(
            of("st_vcat_cs", "vcat"),
            of("st_vcat_ck", "vcat"),
            of("st_vsubcat_cs", "vsubcat"),
            of("st_vsubcat_ck", "vsubcat"),
            of("st_vtag_cs", "vtag"),
            of("st_vtag_ck", "vtag"),
            of("st_vdance_cs", "vdance"),
            of("st_vdance_ck", "vdance"),
            of("st_vlen_cs", "vlen"),
            of("st_vlen_ck", "vlen"),
            of("st_vheadt_cs", "vheadt"),
            of("st_vheadt_ck", "vheadt"),
            of("st_vdifficulty_cs", "vdifficulty"),
            of("st_vdifficulty_ck", "vdifficulty"),
            of("st_vteach_cs", "vteach"),
            of("st_vteach_ck", "vteach"),
            of("st_vauthor_uid_cs", "vauthor_uid"),
            of("st_vauthor_uid_ck", "vauthor_uid"),
            of("st_vauthor_type_cs", "vauthor_type"),
            of("st_vauthor_type_ck", "vauthor_type"),
            of("st_vauthor_level_cs", "vauthor_level"),
            of("st_vauthor_level_ck", "vauthor_level"),
            of("st_vteam_cs", "vteam"),
            of("st_vteam_ck", "vteam"),
            of("st_vteamrole_cs", "vteamrole"),
            of("st_vteamrole_ck", "vteamrole"),
            of("st_vteacher_cs", "vteacher"),
            of("st_vteacher_ck", "vteacher"),
            of("st_vgenre_cs", "vgenre"),
            of("st_vgenre_ck", "vgenre"),
            of("st_vmp3_cs", "vmp3"),
            of("st_vmp3_ck", "vmp3"),
            of("st_vtitle_len_cs", "vtitle_len"),
            of("st_vtitle_len_ck", "vtitle_len"),
            of("st_vauthor_in_title_cs", "vauthor_in_title"),
            of("st_vauthor_in_title_ck", "vauthor_in_title"),
            of("st_vmusic_in_title_cs", "vmusic_in_title"),
            of("st_vmusic_in_title_ck", "vmusic_in_title"),
            of("st_vdance_in_title_cs", "vdance_in_title"),
            of("st_vdance_in_title_ck", "vdance_in_title")
    );

    private final static List<ImmutablePair<String, String>> USER_DOC_CTR_FEATURES = Arrays.asList(
            of("vcat_ck", "vcat"),
            of("vsubcat_ck", "vsubcat"),
            of("vtag_ck", "vtag"),
            of("vdance_ck", "vdance"),
            of("vlen_ck", "vlen"),
            of("vheadt_ck", "vheadt"),
            of("vdifficulty_ck", "vdifficulty"),
            of("vteach_ck", "vteach"),
            of("vauthor_uid_ck", "vauthor_uid"),
            of("vauthor_type_ck", "vauthor_type"),
            of("vauthor_level_ck", "vauthor_level"),
            of("vteam_ck", "vteam"),
            of("vteamrole_ck", "vteamrole"),
            of("vteacher_ck", "vteacher"),
            of("vgenre_ck", "vgenre"),
            of("vmp3_ck", "vmp3"),
            of("vtitle_len_ck", "vtitle_len"),
            of("vauthor_in_title_ck", "vauthor_in_title"),
            of("vmusic_in_title_ck", "vmusic_in_title"),
            of("vdance_in_title_ck", "vdance_in_title")
    );

    private final static List<ImmutablePair<String, String>> STUSER_DOC_CTR_FEATURES = Arrays.asList(
            of("st_vcat_ck", "vcat"),
            of("st_vsubcat_ck", "vsubcat"),
            of("st_vtag_ck", "vtag"),
            of("st_vdance_ck", "vdance"),
            of("st_vlen_ck", "vlen"),
            of("st_vheadt_ck", "vheadt"),
            of("st_vdifficulty_ck", "vdifficulty"),
            of("st_vteach_ck", "vteach"),
            of("st_vauthor_uid_ck", "vauthor_uid"),
            of("st_vauthor_type_ck", "vauthor_type"),
            of("st_vauthor_level_ck", "vauthor_level"),
            of("st_vteam_ck", "vteam"),
            of("st_vteamrole_ck", "vteamrole"),
            of("st_vteacher_ck", "vteacher"),
            of("st_vgenre_ck", "vgenre"),
            of("st_vmp3_ck", "vmp3"),
            of("st_vtitle_len_ck", "vtitle_len"),
            of("st_vauthor_in_title_ck", "vauthor_in_title"),
            of("st_vmusic_in_title_ck", "vmusic_in_title"),
            of("st_vdance_in_title_ck", "vdance_in_title")
    );

    @Override
    public List<IFeature> extract(PredictItem<DocItem> predictItem, Items queryItems) {
        List<IFeature> features = new ArrayList<>();
        DocItem targetDocItem = (DocItem)queryItems.get(ItemKey.doc).get();
        UserItem userItem = UserProfileUtils.getUserItem(queryItems);
        extractDocFeatures(predictItem, features);
        extractTargetDocFeatures(targetDocItem, features);
        extractUserFeatures(userItem, features);
        extractUserDocConjunctFeatures(userItem, predictItem, features);
        extractDocDocConjunctFeatures(targetDocItem, predictItem, features);
        extractUserDocCtrFeatures(userItem, predictItem.getItem(), USER_DOC_CTR_FEATURES, "ud", features);


        extractShortTermUserFeatures(userItem, features);
        extractShortTermUserDocConjunctFeatures(userItem, predictItem, features);
        extractUserDocCtrFeatures(userItem, predictItem.getItem(), STUSER_DOC_CTR_FEATURES, "sud", features);


        Optional<IItem> contextItemOpt = queryItems.get(ItemKey.context);
        if (contextItemOpt.isPresent()) {
            IItem contextItem = contextItemOpt.get();
            extractContextFeatures(contextItem, features);
        }

        return features;
    }

    private void extractDocDocConjunctFeatures(DocItem docItem,
                                               PredictItem<DocItem> predictItem, List<IFeature> features) {
        for (ImmutablePair<String, String> featurePair : DOC_DOC_CONJUNCTION_FEATURES) {
            crossDocFeature(docItem, predictItem.getItem(), featurePair.getLeft(), featurePair.getRight(), features);
        }
    }
    private void extractShortTermUserFeatures(UserItem userItem, List<IFeature> features) {
        for (String shortTermUserFeature : SHORT_TERM_USER_FEATURES) {
            extractUserFeatures(userItem, shortTermUserFeature, features);
        }
    }

    private void extractContextFeatures(IItem contextItem, List<IFeature> features) {
        for (String contextFeature : CONTEXT_FEATURES) {
            extractContextFeature(contextItem, contextFeature, features);
        }
    }

    private void extractContextFeature(IItem contextItem, String facetName, List<IFeature> features) {
        Optional<IFeatures> featsOpt = contextItem.getFeatures(facetName);
        if (featsOpt.isPresent()) {
            IFeatures feats = featsOpt.get();
            for (IFeature feat : feats) {
                String featureName = "" + facetName + "_" + feat.getName();
                features.add(new Feature(featureName, feat.getValue()));
            }
        }
    }


    private void extractUserFeatures(UserItem userItem, List<IFeature> features) {
        for (String userFeature : USER_FEATURES) {
            extractUserFeatures(userItem, userFeature, features);
        }
    }

    private void extractDocFeatures(PredictItem<DocItem> predictItem, List<IFeature> features) {
        DocItem docItem = predictItem.getItem();
        for (String docFeature : DOC_FEATURES) {
            extractDocFeatures(docItem, docFeature, features);
        }

        extractDocRetrieveFeature(predictItem, features);
    }
    private void extractTargetDocFeatures(DocItem targetDocItem, List<IFeature> features) {
        for (String docFeature : DOC_FEATURES) {
            extractTargetDocFeatures(targetDocItem, docFeature, features);
        }
    }

    private void extractDocRetrieveFeature(PredictItem<DocItem> predictItem, List<IFeature> features) {
        List<RetrieveKey> retrieveKeys = predictItem.getRetrieveKeys();
        DocItem item = predictItem.getItem();

        for (RetrieveKey retrieveKey : retrieveKeys) {
            features.add(new Feature("d_retrieve_" + retrieveKey.getType(), 1.0));
            features.add(new Feature("d_retrieve_" + retrieveKey.getType() + "_" + retrieveKey.getKey(), 1.0));

            for (String tag : retrieveKey.getTags()) {
                features.add(new Feature("d_retrieve_" + retrieveKey.getType() + "_" + tag, 1.0));
            }

//            extractDocRetrieveMatchFeature(item, retrieveKey, "retrieveView", "d:rstatview", features);
//            extractDocRetrieveMatchFeature(item, retrieveKey, "retrieveClick", "d:rstatclick", features);
//            extractDocRetrieveMatchFeature(item, retrieveKey, "retrieveCtr", "d:rstatctr", features);
        }

        extractDocRetrieveStatFeature(item, retrieveKeys, "retrieveView", "d_rsview", features);
        extractDocRetrieveStatFeature(item, retrieveKeys, "retrieveClick", "d_rsclk", features);
        extractDocRetrieveStatFeature(item, retrieveKeys, "retrieveCtr", "d_rsctr", features);

    }

    private void extractDocRetrieveStatFeature(DocItem item, List<RetrieveKey> retrieveKeys, String facet,
                                               String namespace, List<IFeature> features) {
        double min = Double.MAX_VALUE;
        double max = Double.NEGATIVE_INFINITY;
        String maxType = null;
        String minType = null;

        double avg = 0.0;

        int count = 0;

        List<Double> matchedFeatureValues = new ArrayList<>();
        for (RetrieveKey retrieveKey : retrieveKeys) {
            String statKey = retrieveKey.getType() + "_" + retrieveKey.getKey();
            Optional<IFeatures> featuresOpt = item.getFeatures(facet);
            if (!featuresOpt.isPresent()) {
                continue;
            }

            IFeatures iFeatures = featuresOpt.get();
            Optional<IFeature> iFeatureOpt = iFeatures.get(statKey);
            if (iFeatureOpt.isPresent()) {
                IFeature feature = iFeatureOpt.get();
                double value = feature.getValue();
                matchedFeatureValues.add(value);
                if (min > value) {
                    min = value;
                    minType = retrieveKey.getType();
                }

                if (max < value) {
                    max = value;
                    maxType = retrieveKey.getType();
                }

                avg += value;
                ++count;
            }
        }


        if (count != 0) {
            avg /= count;

            features.add(new Feature(namespace + "max", max));
            features.add(new Feature(namespace + "min", min));
            features.add(new Feature(namespace + "avg", avg));

            if (maxType != null) {
                features.add(new Feature(namespace + "max" + maxType, 1.0));
            }

            if (minType != null) {
                features.add(new Feature(namespace + "min" + minType, 1.0));
            }

            double std = 0;
            if (count > 1) {
                for (Double value : matchedFeatureValues) {
                    std += (value - avg) * (value - avg);
                }
                std = Math.sqrt(1.0 / count * std);
            }

            features.add(new Feature(namespace + "std", std));
        }

    }

    private void extractUserDocCtrFeatures(UserItem userItem, DocItem docItem, List<ImmutablePair<String, String>> featurePairs
            , String namespace, List<IFeature> features) {
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Timer.Context time = taggedMetricRegistry.timer("uservideo.extract.userdocfeatures").time();

        double minCtr = Double.MAX_VALUE;
        double maxCtr = Double.NEGATIVE_INFINITY;
        double sum = 0.0;
        int matchCount = 0;

        Map<String, Map<String, TVariance>> varianceMap = UserProfileUtils.getVarianceMap(userItem);

        List<Double> ctrList = new ArrayList<>();
        for (ImmutablePair<String, String> userDocCtrFeature : featurePairs) {
            String userFacet = userDocCtrFeature.getLeft();
            String docFacet = userDocCtrFeature.getRight();

            Map<String, TVariance> uFeatureMap = varianceMap.get(userFacet);

            if (uFeatureMap == null || uFeatureMap.isEmpty()) {
                continue;
            }

            Optional<IFeatures> featuresOpt = docItem.getFeatures(docFacet);
            if (!featuresOpt.isPresent()) {
                continue;
            }

            IFeatures ifeatures = featuresOpt.get();

            for (IFeature ifeature : ifeatures) {
                TVariance variance = uFeatureMap.get(ifeature.getName());
                if (variance == null) {
                    continue;
                }

                double posCnt = variance.getPosCnt();
                double negCnt = variance.getNegCnt();
                double ctr = (posCnt + 0.14) / (negCnt + 1);
                ctrList.add(ctr);

                sum += ctr;
                if (minCtr > ctr) {
                    minCtr = ctr;
                }

                if (maxCtr < ctr) {
                    maxCtr = ctr;
                }
                ++matchCount;
            }
        }

        if (matchCount > 0) {
            features.add(new Feature(namespace + "_ctrmin", minCtr));
            features.add(new Feature(namespace + "_ctrmax", maxCtr));
            double avg = sum / matchCount;
            features.add(new Feature(namespace + "_ctravg", avg));

            double std = 0;
            if (matchCount > 1) {
                for (Double value : ctrList) {
                    std += (value - avg) * (value - avg);
                }
                std = Math.sqrt(1.0 / matchCount * std);
            }

            features.add(new Feature(namespace + "_ctrstd", std));
        }

        time.stop();
    }

//    private void extractDocRetrieveTypeStatFeature(DocItem item, List<RetrieveKey> retrieveKeys, String facet,
//                                                   String namespace, List<IFeature> features) {
//
//        Map<String, List<RetrieveKey>> typeRetrieveKeysMap = new HashMap<>();
//        for (RetrieveKey retrieveKey : retrieveKeys) {
//            List<RetrieveKey> typeKeys = typeRetrieveKeysMap.get(retrieveKey.getType());
//            if (typeKeys == null) {
//                typeKeys = new ArrayList<>();
//                typeRetrieveKeysMap.put(retrieveKey.getType(), typeKeys);
//            }
//            typeKeys.add(retrieveKey);
//        }
//
//        for (Map.Entry<String, List<RetrieveKey>> entry : typeRetrieveKeysMap.entrySet()) {
//            extractDocRetrieveStatFeature(item, entry.getValue(), facet, namespace + entry.getKey(), features, false);
//        }
//    }

    private void extractDocRetrieveMatchFeature(DocItem item, RetrieveKey retrieveKey, String facetName,
                                                String namespace, List<IFeature> features) {
        String statKey = retrieveKey.getType() + "_" + retrieveKey.getKey();
        Optional<IFeatures> retrieveViewOpt = item.getFeatures(facetName);

        if (retrieveViewOpt.isPresent()) {
            IFeatures iFeatures = retrieveViewOpt.get();
            Optional<IFeature> iFeatureOpt = iFeatures.get(statKey);
            if (iFeatureOpt.isPresent()) {
                IFeature iFeature = iFeatureOpt.get();
                features.add(new Feature(namespace, iFeature.getValue()));
                features.add(new Feature(namespace + iFeature.getName(), iFeature.getValue()));
                features.add(new Feature(namespace + retrieveKey.getType(), iFeature.getValue()));
            }
        }
    }

    private void extractUserDocConjunctFeatures(UserItem userItem, PredictItem<DocItem> predictItem, List<IFeature> features) {
        DocItem docItem = predictItem.getItem();
        for (ImmutablePair<String, String> featurePair : USER_DOC_CONJUNCTION_FEATURES) {
            crossStatsFeature(userItem, featurePair.getLeft(), docItem, featurePair.getRight(), features);
        }
    }

    private void extractShortTermUserDocConjunctFeatures(UserItem userItem,
                                                         PredictItem<DocItem> predictItem, List<IFeature> features) {
        DocItem docItem = predictItem.getItem();
        for (ImmutablePair<String, String> featurePair : SHORT_TERM_USER_DOC_CONJUNCTION_FEATURES) {
            crossStatsFeature(userItem, featurePair.getLeft(), docItem, featurePair.getRight(), features);
        }
    }

    private void extractUserFeatures(UserItem userItem, String facetName, List<IFeature> resultList) {
        String featurePrefix = "u_" + facetName;
        Optional<IFeatures> featuresOpt = userItem.getFeatures(facetName);

        extractPredictFeatures(featuresOpt, featurePrefix, resultList);
    }

    private void extractDocFeatures(DocItem docItem, String facetName, List<IFeature> resultList) {
        String featurePrefix = "d_" + facetName;
        Optional<IFeatures> features = docItem.getFeatures(facetName);
        extractPredictFeatures(features, featurePrefix, resultList);
    }
    private void extractTargetDocFeatures(DocItem docItem, String facetName, List<IFeature> resultList) {
        String featurePrefix = "t_" + facetName;
        Optional<IFeatures> features = docItem.getFeatures(facetName);
        extractPredictFeatures(features, featurePrefix, resultList);
    }
    private void crossStatsFeature(UserItem userItem, String userFacet, DocItem docItem, String docFacet, List<IFeature> resultList) {
        Optional<IFeatures> userFeaturesOpt = userItem.getFeatures(userFacet);
        if (!userFeaturesOpt.isPresent()) {
            return;
        }

        Optional<IFeatures> docFeaturesOpt = docItem.getFeatures(docFacet);
        if (!docFeaturesOpt.isPresent()) {
            return;
        }

        IFeatures userFeatures = userFeaturesOpt.get();
        IFeatures docFeatures = docFeaturesOpt.get();


        double max = Double.NEGATIVE_INFINITY;
        double min = Double.MAX_VALUE;
        double dotProd = 0.0;
        int matchCount = 0;
        int posCount = 0;

        List<Double> matchValues = new ArrayList<>();

        for (IFeature feature : docFeatures) {
            Optional<IFeature> userFeatureOpt = userFeatures.get(feature.getName());
            if (userFeatureOpt.isPresent()) {
                IFeature userFeature = userFeatureOpt.get();
                double value = feature.getValue() * userFeature.getValue();
                matchValues.add(value);

                dotProd += value;

                if (value > 0) {
                    ++posCount;
                }

                if (max < value) {
                    max = value;
                }

                if (min > value) {
                    min = value;
                }

                ++matchCount;
            }
        }

        double userL2Norm = 0.0;

        for (IFeature userFeature : userFeatures) {
            userL2Norm += userFeature.getValue() * userFeature.getValue();
        }

        userL2Norm = Math.sqrt(userL2Norm);

        double docL2Norm = 0.0;
        for (IFeature docFeature : docFeatures) {
            docL2Norm += docFeature.getValue() * docFeature.getValue();
        }

        docL2Norm = Math.sqrt(docL2Norm);

        if (userL2Norm != 0.0 && docL2Norm != 0.0) {
            double similarity = dotProd / (userL2Norm * docL2Norm);
            String featureName = "sim_u_" + userFacet + "-d_" + docFacet + "";
            resultList.add(new Feature(featureName, similarity));
        }

        if (matchCount > 0) {
            String maxFeatureKey = "max_u_" + userFacet + "-d_" + docFacet + "";
            resultList.add(new Feature(maxFeatureKey, max));

            double avg = dotProd / matchCount;
            String avgFeatureKey = "avg_u_" + userFacet + "-d_" + docFacet + "";
            resultList.add(new Feature(avgFeatureKey, avg));

            String minFeatureKey = "min_u_" + userFacet + "-d_" + docFacet + "";
            resultList.add(new Feature(minFeatureKey, min));

            String posCountFeatureKey = "posCnt_u_" + userFacet + "-d_" + docFacet + "";
            resultList.add(new Feature(posCountFeatureKey, posCount));

            double std = 0.0;
            if (matchCount > 1) {
                for (Double matchValue : matchValues) {
                    std += (matchValue - avg) * (matchValue - avg);
                }
                std = Math.sqrt(1.0 / matchCount * std);
            }

            String stdFeatureKey = "std_u_" + userFacet + "-d_" + docFacet + "";
            resultList.add(new Feature(stdFeatureKey, std));
        }
    }

    private void crossDocFeature(DocItem docItem, DocItem recItem, String docFacet, String recFacet, List<IFeature> resultList) {
        Optional<IFeatures> docFeaturesOpt = docItem.getFeatures(docFacet);
        if (!docFeaturesOpt.isPresent()) {
            return;
        }
        IFeatures docFeatures = docFeaturesOpt.get();
        Optional<IFeatures> recFeaturesOpt = recItem.getFeatures(recFacet);
        if (!recFeaturesOpt.isPresent()) {
            return;
        }
        IFeatures recFeatures = recFeaturesOpt.get();

        double max = Double.NEGATIVE_INFINITY;
        double min = Double.MAX_VALUE;
        double dotProd = 0.0;
        int matchCount = 0;
        int posCount = 0;

        List<Double> matchValues = new ArrayList<>();

        for (IFeature docFeature : docFeatures) {
            Optional<IFeature> recFeatureOpt = recFeatures.get(docFeature.getName());
            if (recFeatureOpt.isPresent()) {
                IFeature recFeature = recFeatureOpt.get();
                double value = docFeature.getValue() * recFeature.getValue();
                matchValues.add(value);

                dotProd += value;

                if (value > 0) {
                    ++posCount;
                }

                if (max < value) {
                    max = value;
                }

                if (min > value) {
                    min = value;
                }

                ++matchCount;
            }
        }

        double recL2Norm = 0.0;

        for (IFeature recFeature : recFeatures) {
            recL2Norm += recFeature.getValue() * recFeature.getValue();
        }

        recL2Norm = Math.sqrt(recL2Norm);

        double docL2Norm = 0.0;
        for (IFeature docFeature : docFeatures) {
            docL2Norm += docFeature.getValue() * docFeature.getValue();
        }

        docL2Norm = Math.sqrt(docL2Norm);

        if (recL2Norm != 0.0 && docL2Norm != 0.0) {
            double similarity = dotProd / (recL2Norm * docL2Norm);
            String featureName = "sim_t_" + docFacet + "-d_" + recFacet + "";
            resultList.add(new Feature(featureName, similarity));
        }

        if (matchCount > 0) {
            String maxFeatureKey = "max_t_" + docFacet + "-d_" + recFacet + "";
            resultList.add(new Feature(maxFeatureKey, max));

            double avg = dotProd / matchCount;
            String avgFeatureKey = "avg_t_" + docFacet + "-d_" + recFacet + "";
            resultList.add(new Feature(avgFeatureKey, avg));

            String minFeatureKey = "min_t_" + docFacet + "-d_" + recFacet + "";
            resultList.add(new Feature(minFeatureKey, min));

            String posCountFeatureKey = "posCnt_t_" + docFacet + "-d_" + recFacet + "";
            resultList.add(new Feature(posCountFeatureKey,posCount));

            double std = 0.0;
            if (matchCount > 1) {
                for (Double matchValue : matchValues) {
                    std += (matchValue - avg) * (matchValue - avg);
                }
                std = Math.sqrt(1.0 / matchCount * std);
            }

            String stdFeatureKey = "std_t_" + docFacet + "-d_" + recFacet + "";
            resultList.add(new Feature(stdFeatureKey, std));
        }

    }


    private void extractPredictFeatures(Optional<IFeatures> featuresOpt, String featurePrefix, List<IFeature> resultList) {
        if (featuresOpt.isPresent()) {
            IFeatures features = featuresOpt.get();
            for (IFeature feature : features) {
                String featureName = featurePrefix + "_" + feature.getName();
                resultList.add(new Feature(featureName, feature.getValue()));
            }
        }
    }
}
