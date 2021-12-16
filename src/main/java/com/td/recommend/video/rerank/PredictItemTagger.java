package com.td.recommend.video.rerank;

import com.td.data.profile.TVariance;
import com.td.data.profile.common.KeyConstants;
import com.td.data.profile.item.KeyItem;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.recommend.commons.date.TimeUtils;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.rerank.RuleTag;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.rerank.core.ItemTagger;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.HomeworkVids;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.utils.TalentUids;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/12/13.
 */
public class PredictItemTagger implements ItemTagger<PredictItem<DocItem>> {
    private static final Set<String> INTEREST_RETRIEVER_TYPES = new HashSet<>();
    private static final Set<String> ST_RETRIEVER_TYPES = new HashSet<>();
    private static final Set<String> EXT_RETRIEVER_TRPES = new HashSet<>();
    private static final Set<Integer> SMALL_VIDEO = new HashSet<>();
    private static final Set<String> EXPLICIT_ST_TYPES = new HashSet<>();
    private static final Set<String> IMPLICIT_TYPES = new HashSet<>();
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean isNonActiveUser = false;

    static {
        INTEREST_RETRIEVER_TYPES.addAll(Arrays.asList(
                RetrieverType.vcat.name(),
                RetrieverType.vsubcat.name(),
                RetrieverType.vtag.name(),
                RetrieverType.vauthor.name(),
                RetrieverType.vmp3.name(),
                RetrieverType.vxauthor.name(),
                RetrieverType.vxcat.name(),
                RetrieverType.vxmp3.name(),
                RetrieverType.vxsubcat.name(),
                RetrieverType.vxtag.name(),
                RetrieverType.vusercf.name(),
                RetrieverType.vbpr.name(),
                RetrieverType.vyoutubednn.name(),
                RetrieverType.vsearch.name(),
                RetrieverType.vcatrank.name(),
                RetrieverType.vsubcatrank.name(),
                RetrieverType.vtagrank.name(),
                RetrieverType.vmp3rank.name()

        ));

        ST_RETRIEVER_TYPES.addAll(Arrays.asList(
                RetrieverType.vauthor_st.name(),
                RetrieverType.vcat_st.name(),
                RetrieverType.vmp3_st.name(),
                RetrieverType.vsubcat_st.name(),
                RetrieverType.vtag_st.name(),
                RetrieverType.vitemcf.name(),
                RetrieverType.vitemcfv2.name(),
                RetrieverType.vgem.name(),
                RetrieverType.vrealtimesearch.name(),
                RetrieverType.vbert.name(),
                RetrieverType.sevmp3.name(),
                RetrieverType.vgenre_st.name(),
                RetrieverType.sevteacher.name(),
                RetrieverType.vauthorrank_st.name(),
                RetrieverType.vcatrank_st.name(),
                RetrieverType.vmp3rank_st.name(),
                RetrieverType.vsubcatrank_st.name(),
                RetrieverType.vtagrank_st.name()
        ));

        IMPLICIT_TYPES.addAll(Arrays.asList(
                RetrieverType.vitemcf.name(),
                RetrieverType.vitemcfv2.name(),
                RetrieverType.vitemcftrend.name(),
                RetrieverType.tfollowwatch.name(),
                RetrieverType.vusercf.name(),
                RetrieverType.vnmfv3.name(),
                RetrieverType.vbpr.name(),
                RetrieverType.vminet.name(),
                RetrieverType.vgem.name(),
                RetrieverType.vbert.name(),
                RetrieverType.vcluster.name()
        ));

        EXPLICIT_ST_TYPES.addAll(Arrays.asList(
                RetrieverType.vauthor_st.name(),
                RetrieverType.vcat_st.name(),
                RetrieverType.vmp3_st.name(),
                RetrieverType.vsubcat_st.name(),
                RetrieverType.vtag_st.name(),
                RetrieverType.sevmp3.name(),
                RetrieverType.vgenre_st.name(),
                RetrieverType.sevteacher.name(),
                RetrieverType.vauthorrank_st.name(),
                RetrieverType.vcatrank_st.name(),
                RetrieverType.vmp3rank_st.name(),
                RetrieverType.vsubcatrank_st.name(),
                RetrieverType.vtagrank_st.name()
                )
        );

        EXT_RETRIEVER_TRPES.addAll(Arrays.asList(
                RetrieverType.vauthor_ext.name(),
                RetrieverType.vmp3_ext.name(),
                RetrieverType.vsubcat_ext.name(),
                RetrieverType.vcat_ext.name()
        ));
        SMALL_VIDEO.addAll(Arrays.asList(105, 106, 107, 108));
    }

    private final VideoRecommenderContext recommendContext;

    public PredictItemTagger(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
        isNonActiveUser = isNonActiveUser(recommendContext);
    }

    @Override
    public TaggedItem<PredictItem<DocItem>> tag(PredictItem<DocItem> item) {
        TaggedItem<PredictItem<DocItem>> taggedItem = new TaggedItem<>(item);
        boolean isNewUser = recommendContext.getUserType().name().startsWith("new_");

        addRetrieverTags(taggedItem);
        addFistCatTags(taggedItem);
        addSecondCatTags(taggedItem);
        addContentTagTags(taggedItem);
        addQualityTags(taggedItem);
        addUidTags(taggedItem);
        addMp3Tags(taggedItem);
        addDanceTags(taggedItem);
        addCtypeTags(taggedItem);
        addTalentTags(taggedItem);
        addTeachTags(taggedItem);
        addTalentStarTags(taggedItem);
        addDegreeTags(taggedItem);
        addDurationTags(taggedItem);
        addGroupTag(taggedItem);
        addPlayVerticalTags(taggedItem);
        addContentRawTags(taggedItem);
        addHomeworkTag(taggedItem);
        addUserInterestTags(taggedItem);
        addAlbumIdTags(taggedItem);
        addTrendTags(taggedItem);
        return taggedItem;
    }

    private void addTrendTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        try {
            int ctype = DocProfileUtils.getCtype(taggedItem.getItem().getItem());
            if(ctype==501||ctype==502||ctype==503){
                taggedItem.addTag(new RuleTag("trend", "ctype"));
            }
        } catch (Exception ignore) {
        }

    }

    private void addFistCatTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        String id, cat;
        cat = KeyConstants.firstcat;
        try {
            id = taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(cat).get()
                    .getId();
        } catch (Exception e) {
            id = "0";
        }
        taggedItem.addTag(new RuleTag(cat, cat), new RuleTag(id, cat));
        taggedItem.addTag(new RuleTag(id, cat));

    }

    private void addPlayVerticalTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        String playVertical = DocProfileUtils.playVertical(taggedItem.getItem().getItem());
        taggedItem.addTag(new RuleTag(playVertical, "play_vertical"));
    }

    private void addContentRawTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        String id, raw;
        raw = KeyConstants.content_raw;
        try {
            id = taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(raw).get()
                    .getId();
        } catch (Exception e) {
            id = "0";
        }
        taggedItem.addTag(new RuleTag(id, raw));
    }

    private void addSecondCatTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        String id, cat;
        cat = KeyConstants.secondcat;
        try {
            id = taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(cat).get()
                    .getId();
        } catch (Exception e) {
            id = "0";
        }
        taggedItem.addTag(new RuleTag(cat, cat), new RuleTag(id, cat));
        taggedItem.addTag(new RuleTag(id, cat));
    }

    private void addGroupTag(TaggedItem<PredictItem<DocItem>> taggedItem) {
        TVariance variance = UserProfileUtils.getVarianceFeatureMap(recommendContext.getUserItem(), "group_subcat").get("1007");
        String secondCat = DocProfileUtils.getSecondCat(taggedItem.getItem().getItem());
        if (variance != null && variance.mean > 0.5 && secondCat.equals("1007")) {
            taggedItem.addTag(new RuleTag("top", "retrieve"));
        }
    }

    private void addHomeworkTag(TaggedItem<PredictItem<DocItem>> taggedItem) {
        String vid = taggedItem.getItem().getId();
        if (HomeworkVids.contains(vid) && recommendContext.hasBucket("homework_boost-yes")) {
            taggedItem.addTag(new RuleTag("homework", "retrieve"));
        }
    }

    private void addRetrieverTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        for (RetrieveKey retrieveKey : taggedItem.getItem().getRetrieveKeys()) {
            String retrieveType = retrieveKey.getType();
            taggedItem.addTag(new RuleTag(retrieveType, "retrieve"));
            if (ST_RETRIEVER_TYPES.contains(retrieveType)) {
                taggedItem.addTag(new RuleTag("short_interest", "retrieve"));
            }
            if (EXT_RETRIEVER_TRPES.contains(retrieveType)) {
                taggedItem.addTag(new RuleTag("ext_interest", "retrieve"));
            }
            if (INTEREST_RETRIEVER_TYPES.contains(retrieveType)) {
                taggedItem.addTag(new RuleTag("long_interest", "retrieve"));
            }
            if (EXPLICIT_ST_TYPES.contains(retrieveType)) {
                taggedItem.addTag(new RuleTag("explicit_st_interest", "retrieve"));
            }
            if (IMPLICIT_TYPES.contains(retrieveType)){
                taggedItem.addTag(new RuleTag("implicit_interest", "retrieve"));
            }
            if(retrieveType.equals("tfollow") || retrieveType.equals("tfollow_ev") ||retrieveType.equals("vxfollow")){
                taggedItem.addTag(new RuleTag("showdance_follow", "retrieve"));
            }
            if(retrieveType.startsWith("t_hot")){
                taggedItem.addTag(new RuleTag("showdance_ev", "retrieve"));
            }
            if (retrieveType.endsWith("_ev")) {
                if (DocProfileUtils.getFirstCat(taggedItem.getItem().getItem()).equals("264")) {
                    taggedItem.addTag(new RuleTag("dance_ev", "retrieve"));
                } else {
                    taggedItem.addTag(new RuleTag("nondance_ev", "retrieve"));
                }
                taggedItem.addTag(new RuleTag("ev", "retrieve"));
            }
            if (retrieveType.endsWith("_en")) {
                taggedItem.addTag(new RuleTag("en", "retrieve"));
            }
            if (retrieveType.endsWith("_eu")) {
                taggedItem.addTag(new RuleTag("eu", "retrieve"));
            }
            if (retrieveType.endsWith("_seen")) {
                taggedItem.addTag(new RuleTag("seen", "retrieve"));
            }
            if (retrieveType.endsWith("vhealth_top")) {
                taggedItem.addTag(new RuleTag("top", "retrieve"));
            }
            if (retrieveType.endsWith("_eu_rlvt")) {
                taggedItem.addTag(new RuleTag("eu_rlvt", "retrieve"));
            }
            if (retrieveType.endsWith("rlvt")) {
                taggedItem.addTag(new RuleTag("rlvt", "retrieve"));
            }
            if (retrieveType.endsWith("_live")) {
                taggedItem.addTag(new RuleTag("live", "retrieve"));
            }
            if (retrieveType.endsWith("_f3")) {
                taggedItem.addTag(new RuleTag("f3", "retrieve"));
            }
        }
    }

    private void addUidTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        String uidstr;
        try {
            int uid = taggedItem.getItem().getItem().getNewsDocumentData().get().getStaticDocumentData().get().getUid();
            if (uid > 0) {
                uidstr = String.valueOf(uid);
                RuleTag parentRuleTag = new RuleTag("uid", "uid");
                taggedItem.addTag(parentRuleTag, new RuleTag(uidstr, "uid"));
            }
        } catch (Exception ignore) {
        }
    }

    private void addAlbumIdTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        try {
            List<String> albumIds = taggedItem.getItem().getItem().getNewsDocumentData().get().getStaticDocumentData().get()
                    .getAlbum().stream().map(KeyItem::getId).collect(Collectors.toList());
            RuleTag parentRuleTag = new RuleTag("albumId", "albumId");
            for (String albumId : albumIds) {
                taggedItem.addTag(parentRuleTag, new RuleTag(albumId, "albumId"));
            }

        } catch (Exception ignore) {
        }
    }

    private void addTeachTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        try {
            String teach = taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(KeyConstants.content_teach).get()
                    .getName();
            taggedItem.addTag(new RuleTag(teach, "content_teach"));
        } catch (Exception ignore) {
        }
    }

    private void addTalentStarTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        try {
            int star = taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getTalentstar();
            String highstar = star > 4 ? "yes" : "no";
            taggedItem.addTag(new RuleTag(highstar, "highstar"));
            if(star >3){
                taggedItem.addTag(new RuleTag("talentstar_gt3", "talentstar_gt3"));
            }
        } catch (Exception ignore) {
        }
    }

    private void addMp3Tags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        try {
            Optional<KeyItem> keyItemOpt = taggedItem.getItem().getItem().getNewsDocumentData().get()
                    .getStaticDocumentData().get().getKeyItemByName("content_mp3");
            if (keyItemOpt.isPresent()) {
                KeyItem item = keyItemOpt.get();
                if (!item.name.equals("unknown")) {
                    RuleTag parentRuleTag = new RuleTag("mp3", "mp3");
                    taggedItem.addTag(parentRuleTag, new RuleTag(item.name, "mp3"));
                }
            }
        } catch (Exception ignore) {
        }
    }

    private void addDanceTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        try {
            Map<String, KeyItem> keyItemMap = taggedItem.getItem().getItem().getNewsDocumentData().get().getStaticDocumentData().get().getKeyItemMap();
            Optional<KeyItem> keyItemOpt = taggedItem.getItem().getItem().getNewsDocumentData().get()
                    .getStaticDocumentData().get().getKeyItemByName("content_dance");
            if (keyItemOpt.isPresent()) {
                KeyItem item = keyItemOpt.get();
                if (!item.id.equals("0")) {
                    RuleTag parentRuleTag = new RuleTag("dance", "dance");
                    taggedItem.addTag(parentRuleTag, new RuleTag(item.id, "dance"));
                }

            }
        } catch (Exception ignore) {
        }
    }

    private void addDurationTags(TaggedItem<PredictItem<DocItem>> taggedItem) {

        String durationType;
        try {
            int duration = taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getDuration();
            if (duration < 60) {
                durationType = "short";
            } else {
                durationType = "long";
            }

        } catch (Exception e) {
            durationType = "short";
        }
        taggedItem.addTag(new RuleTag(durationType, "duration"));

    }

    private void addContentTagTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        try {
            String secondCat = DocProfileUtils.getSecondCat(taggedItem.getItem().getItem());
            taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getContent_tag()
                    .forEach(v -> {
                        RuleTag parentRuleTag = new RuleTag("content_tag", "content_tag");
                        taggedItem.addTag(parentRuleTag, new RuleTag(v.id, "content_tag"));
                        taggedItem.addTag(new RuleTag(v.id, "content_tag"));
                        if (secondCat.equals("265") && v.name.matches("\\d+步")) {
                            taggedItem.addTag(new RuleTag("step", "content_tag"));
                        }
                    });
        } catch (Exception ignore) {
        }
    }

    private void addCtypeTags(TaggedItem<PredictItem<DocItem>> taggedItem) {

        try {
            int ctype = taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getCtype();
            if (SMALL_VIDEO.contains(ctype)) {
                taggedItem.addTag(new RuleTag("small_video", "retrieve"));
            }
            RuleTag parentRuleTag = new RuleTag("ctype", "ctype");
            taggedItem.addTag(parentRuleTag, new RuleTag(String.valueOf(ctype), "ctype"));

            taggedItem.addTag(new RuleTag(String.valueOf(ctype), "ctype"));
        } catch (Exception ignore) {
        }

    }

    private void addQualityTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        String quality;
        try {
            quality = Double.valueOf(
                    taggedItem.getItem().getItem()
                            .getNewsDocumentData().get()
                            .getStaticDocumentData().get()
                            .getKeyItemByName(KeyConstants.text_quality).get()
                            .getValue()
            ).toString();

        } catch (Exception e) {
            quality = "1.0";//1.0,bad; 2.0,good
        }
        taggedItem.addTag(new RuleTag(quality, "quality"));
    }

    private void addDegreeTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        String degree;
        try {
            degree = taggedItem.getItem().getItem()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData().get()
                    .getKeyItemByName(KeyConstants.content_degree).get()
                    .getId();// 零基础:351，简单:352，适中:353，稍难:354, 其他:355
        } catch (Exception e) {
            degree = "355";
        }
        if (Integer.parseInt(degree) < 353) {
            taggedItem.addTag(new RuleTag("easy", "degree"));
        } else {
            taggedItem.addTag(new RuleTag("hard", "degree"));
        }

    }

    private void addTalentTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        List<String> talentUids = TalentUids.get();
        //Map<String,String> talent2cluster = TalentUids.getTalent2Cluster();
        try {
            int uid = taggedItem.getItem().getItem().getNewsDocumentData().get().getStaticDocumentData().get().getUid();
            long pubTimestamp = taggedItem.getItem().getItem().getNewsDocumentData().get().getStaticDocumentData().get().getCtime();
            long freshTime = System.currentTimeMillis() - TimeUtils.ONE_DAY_IN_MILLS * 7;
            if (talentUids.contains(String.valueOf(uid)) && pubTimestamp > freshTime) {
                taggedItem.addTag(new RuleTag("talent_fresh", "talent_fresh"));
            }
//            if(talent2cluster.containsKey(String.valueOf(uid))  && pubTimestamp > freshTime){
//                taggedItem.addTag(new RuleTag("talent_cluster", "talent_cluster" + talent2cluster.get(String.valueOf(uid))));
//            }
        } catch (Exception ignore) {
        }
    }

    private void addUserInterestTags(TaggedItem<PredictItem<DocItem>> taggedItem) {
        DocItem recommendItem = taggedItem.getItem().getItem();
        String teaserId = recommendContext.getRecommendRequest().getSecondCatId();//推广素材类别id，可能是一二级类或tag
        Optional<IItem> teaserItem = recommendContext.getQueryItems().get(ItemKey.interest);//广告素材vid画像
        String teaserSecondCat = teaserItem.map(teaser -> DocProfileUtils.getSecondCat((DocItem) teaser)).orElse("");
        if (DocProfileUtils.getFirstCat(recommendItem).equals(teaserId)
                || DocProfileUtils.getSecondCat(recommendItem).equals(teaserId)
                || DocProfileUtils.getTags(recommendItem).contains(teaserId)) {
            taggedItem.addTag(new RuleTag("same", "teaser"));
        } else if (DocProfileUtils.getSecondCat(recommendItem).equals(teaserSecondCat)) {
            taggedItem.addTag(new RuleTag("similar", "teaser"));
        }
        String userFirstCat = recommendContext.getUserInterest();
        if (DocProfileUtils.getFirstCat(recommendItem).equals(userFirstCat)) {
            taggedItem.addTag(new RuleTag("interest", "firstcat"));
        }
    }

    private static boolean isNonActiveUser(VideoRecommenderContext context) {
        UserItem userItem = context.getUserItem();
        Double click = UserProfileUtils.getValueFeaturesMap(userItem, "vuser_stats").get("feedClick");
        return click == null || click < 2;
    }

}
