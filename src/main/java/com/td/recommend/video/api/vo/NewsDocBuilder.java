package com.td.recommend.video.api.vo;

import com.google.common.collect.ImmutableSet;
import com.td.data.profile.common.KeyConstants;
import com.td.data.profile.item.ItemDocumentData;
import com.td.data.profile.item.KeyItem;
import com.td.data.profile.item.VideoItem;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.commons.rerank.TaggedItem;
import com.td.recommend.commons.response.CallbackParams;
import com.td.recommend.commons.response.NewsDoc;
import com.td.recommend.commons.response.Tag;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.datasource.RecReasonsMap;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/26.
 */
public class NewsDocBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(NewsDocBuilder.class);

    private static volatile NewsDocBuilder instance = new NewsDocBuilder();
    private static final Set<String> invalidTagSet = ImmutableSet.of("搞笑", "娱乐", "拍人", "随手拍", "其他", "女性保健操", "唱歌", "儿童舞", "unknown");
    private static final Set<String> reason_41 = ImmutableSet.of("昨日最热入门美舞", "精品进阶美舞教程", "塑形美体最佳课程", "时下最热保健教程", "本城市正在流行的舞曲", "本地市民锻炼首选教程", "本地市民的养生首选");
    private static final Set<String> reason_42 = ImmutableSet.of("你关注的人正在学", "TA被10W人点赞过");

    public static NewsDocBuilder getInstance() {
        return instance;
    }


    public Optional<NewsDoc> build(TaggedItem<PredictItem<DocItem>> taggedItem,
                                   int pos,
                                   VideoRecommenderContext recommendContext) {
        try {
            Optional<ItemDocumentData> itemDocumentData = taggedItem.getItem().getItem().getNewsDocumentData();
            if (!itemDocumentData.isPresent()) {
                return Optional.empty();
            }

            PredictItem<DocItem> predictItem = taggedItem.getItem();

            ItemDocumentData ItemDocumentData = itemDocumentData.get();
            Optional<VideoItem> staticDocumentDataOpt = ItemDocumentData.getStaticDocumentData();
            if (!staticDocumentDataOpt.isPresent()) {
                return Optional.empty();
            }

            VideoItem staticDocumentData = staticDocumentDataOpt.get();
            NewsDoc newsDoc = new NewsDoc();
            newsDoc.setTags(buildTags(predictItem, recommendContext));
            newsDoc.setId(staticDocumentData.getId());
            newsDoc.setTitle(staticDocumentData.getTitle());
            newsDoc.setUid(String.valueOf(staticDocumentData.getUid()));
            newsDoc.setMp3url(staticDocumentData.getMp3url());
            newsDoc.setName(staticDocumentData.getUname());
            newsDoc.setDegree(staticDocumentData.getDegree());
            newsDoc.setDuration(staticDocumentData.getDuration());
            newsDoc.setHead_t(staticDocumentData.getHead_t());
            newsDoc.setCreatetime(staticDocumentData.getCreatetime());
            newsDoc.setCtime(staticDocumentData.getCtime());
            newsDoc.setCtype(staticDocumentData.getCtype());
            newsDoc.setEtime(staticDocumentData.getEtime());
            newsDoc.setCstage(staticDocumentData.getCstage());
            newsDoc.setDis_scene(staticDocumentData.getDis_scene());
            newsDoc.setRepeat(staticDocumentData.getRepeat());
            newsDoc.setRepeat_period(staticDocumentData.getRepeat_period());

            Map<String, String> algInfo = AlgInfoBuilder.buildAlgInfo(taggedItem, pos, recommendContext);

            newsDoc.setAlgInfo(algInfo);
            return Optional.of(newsDoc);
        } catch (Exception e) {
            LOG.error("build news doc failed!", e);
            return Optional.empty();
        }
    }

    public List<Tag> buildTags(PredictItem<DocItem> predictItem, VideoRecommenderContext recommendContext) {
        List<Tag> tags = new ArrayList<>();
        VideoItem staticDocumentData = predictItem.getItem().getNewsDocumentData().get().getStaticDocumentData().get();
        List<RetrieveKey> retrieveKeys = predictItem.getRetrieveKeys();
        List<String> reasons = retrieveKeys.stream().map(RetrieveKey::getReason).filter(r -> !r.isEmpty()).collect(Collectors.toList());
        if (DocProfileUtils.getCtype(predictItem.getItem()) == 301) {
            tags.add(new Tag(0, "专辑"));
        }else if(DocProfileUtils.getCtype(predictItem.getItem()) == 501){
            ;
        }else if (reasons.size() > 0) {
            int index = Math.abs(staticDocumentData.getId().hashCode()) % reasons.size();
            String name = reasons.get(index);
            tags.add(new Tag(0, name));
        }
        try {
            Optional<KeyItem> content_teach = staticDocumentData.getKeyItemByName(KeyConstants.content_teach);
            if (content_teach.isPresent()) {
                String name = content_teach.get().getName();
                int id = Integer.parseInt(content_teach.get().getId());
                if (null != name && id == 362 && name.equals("教学")) {
                    Tag tag = new Tag(1, name);
                    tags.add(tag);
                }
            }
        } catch (Exception ignore) {
        }
        try {
            Optional<KeyItem> virtualFirstCat = staticDocumentData.getKeyItemByName("virtual_firstcat");
            Optional<KeyItem> firstCat = staticDocumentData.getKeyItemByName(KeyConstants.firstcat);
            String cat = "";
            if (virtualFirstCat.isPresent()) {
                cat = virtualFirstCat.get().getId();
            } else if (firstCat.isPresent()) {
                cat = firstCat.get().getId();
            }
            if (cat.equals("264")) {
                Optional<KeyItem> content_dance = staticDocumentData.getKeyItemByName(KeyConstants.content_dance);
                if (content_dance.isPresent()) {
                    String name = content_dance.get().getName();
                    if (StringUtils.isNotBlank(name) && !invalidTagSet.contains(name)) {
                        Tag tag = new Tag(2, name);
                        tags.add(tag);
                    }
                }
            } else {
                String secCat = "";
                Optional<KeyItem> virtualSecondCat = staticDocumentData.getKeyItemByName("virtual_secondcat");
                Optional<KeyItem> secondCat = staticDocumentData.getKeyItemByName(KeyConstants.secondcat);
                if (virtualSecondCat.isPresent()) {
                    secCat = virtualSecondCat.get().getName();
                } else if (secondCat.isPresent()) {
                    secCat = secondCat.get().getName();
                }
                if (StringUtils.isNotBlank(secCat) && !invalidTagSet.contains(secCat)) {
                    Tag tag = new Tag(2, secCat);
                    tags.add(tag);
                }
            }
        } catch (Exception ignore) {
        }
        try {
            Optional<KeyItem> content_degree = staticDocumentData.getKeyItemByName(KeyConstants.content_degree);
            if (content_degree.isPresent()) {
                String name = content_degree.get().getName();
                if (StringUtils.isNotBlank(name) && !invalidTagSet.contains(name)) {
                    Tag tag = new Tag(3, name);
                    tags.add(tag);
                }
            }
        } catch (Exception ignore) {
        }

        try {
            Optional<String> reasonOpt = RecReasonsMap.getReasonById(staticDocumentData.getId());
            if (reasonOpt.isPresent()) {
                String name = reasonOpt.get();
                if (StringUtils.isNotBlank(name)) {
                    if(reason_41.contains(name)){
                        Tag tag = new Tag(4, getConvertReason(name, recommendContext),41);
                        tags.add(tag);
                    }
                    else if(reason_42.contains(name)){
                        Tag tag = new Tag(4, getConvertReason(name, recommendContext),42);
                        tags.add(tag);
                    }
                    else{
                        Tag tag = new Tag(4, getConvertReason(name, recommendContext));
                        tags.add(tag);
                    }

                }
            }
        } catch (Exception ex) {

        }


        return tags;
    }

    private static String getConvertReason(String name, VideoRecommenderContext context) {
        String city = context.getRecommendRequest().getCity();
        if (name.startsWith("本城市") || name.startsWith("本地市")) {
            if (!city.equals("unknown")) {
                String uname = name.startsWith("本城市") ? name.replaceFirst("本城市", city) : name.replaceFirst("本地市", city);
                return uname;
            }
        }
        return name;
    }

    private static Map<String, String> getCallbackParams(String docId, String token, String predictId, int pos, int type, RecommendRequest recommendRequest, String fromId) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(CallbackParams.newsid.name(), docId);
        paramMap.put(CallbackParams.token.name(), token);
        paramMap.put(CallbackParams.diu.name(), recommendRequest.getDiu());
        paramMap.put(CallbackParams.pos.name(), String.valueOf(pos));
        paramMap.put(CallbackParams.predictId.name(), predictId);
        paramMap.put(CallbackParams.appId.name(), recommendRequest.getAppId());
        paramMap.put(CallbackParams.pageNo.name(), String.valueOf(recommendRequest.getPageNo()));
        paramMap.put(CallbackParams.deviceId.name(), recommendRequest.getDeviceId());
        paramMap.put(CallbackParams.type.name(), String.valueOf(type));
        paramMap.put(CallbackParams.fromId.name(), fromId);

        return paramMap;
    }


//    private Template buildTemplate(VideoItem doc) {
//        boolean hasBigImg = hasBigImg(doc);
//        int imgSize = imageSize(doc);
//        if (hasBigImg && imgSize > 0) {
//            return Template.BIG_IMG;
//        }
//        if (imgSize == 0) {
//            return Template.NO_IMG;
//        } else if (imgSize == 1 || imgSize == 2) {
//            return Template.SMALL_IMG;
//        } else {
//            return Template.MULTI_IMG;
//        }
//    }

    private List<Tag> generateUserDislikeTags(String userId) {
        List<String> reasons = new ArrayList<>(Arrays.asList("看过了", "标题夸张", "低俗色情", "内容太水"));
        List<Tag> result = new ArrayList<>();
        int hashSize = reasons.size();

        while (hashSize > 1) {
            int index = Math.abs(userId.hashCode()) % hashSize--;
            int id = result.size() + 1;
            result.add(new Tag(id, reasons.get(index)));
            reasons.remove(index);
        }
        return result;
    }

    boolean shouldMarkMorning(PredictItem<DocItem> predictItem, VideoRecommenderContext recommendContext) {
        int duration = DocProfileUtils.getDuration(predictItem.getItem());
        String cat = DocProfileUtils.getSecondCat(predictItem.getItem());
        int hour = LocalDateTime.now().getHour();
        return duration >= 600
                && hour >= 5 && hour <= 8
                && cat.equals("1030")
                && recommendContext != null
                && recommendContext.getBuckets().contains("feed_morning-yes");
    }


//    private List<Tag> buildDislikeTags(VideoItem doc, String userId) {
//        List<Tag> tags = new ArrayList<>();
//        tags.addAll(generateUserDislikeTags(userId));
//        String from = doc.getUid();
//        if (StringUtils.isNotEmpty(from)) {
//            tags.add(new Tag(tags.size() + 1, "屏蔽来源:" + from));
//        }
//
//        Map<String, Double> tagsMap = doc.getTagsMap();
//        if (!tagsMap.isEmpty()) {
//            Set<Map.Entry<String, Double>> entries = tagsMap.entrySet();
//
//            if (tagsMap.size() == 1) {
//                Iterator<Map.Entry<String, Double>> iterator = entries.iterator();
//                Map.Entry<String, Double> entry = iterator.next();
//                tags.add(new Tag(tags.size() + 1, "不想看:" + entry.getKey()));
//            } else {
//                List<Map.Entry<String, Double>> tagEntries = entries.stream().collect(Collectors.toList());
//
//                tagEntries.sort((e1, e2) -> -Double.compare(e1.getValue(), e2.getValue()));
//
//                for (int i = 0; i < 2; ++i) {
//                    Map.Entry<String, Double> tagEntry = tagEntries.get(i);
//                    tags.add(new Tag(tags.size() + 1, "不想看:" + tagEntry.getKey()));
//                }
//            }
//        }
//        return tags;
//    }
//    private List<Tag> buildDislikeTags(VideoItem doc) {
//        List<Tag> tags = new ArrayList<>();
//        tags.add(new Tag(1, "重复旧闻"));
//        tags.add(new Tag(2, "内容质量差"));
//        String from = doc.getFrom();
//        if (StringUtils.isNotEmpty(from)) {
//            tags.add(new Tag(3, "来源:" + from));
//        }
//
//        Map<String, Double> tagsMap = doc.getTagsMap();
//        if (!tagsMap.isEmpty()) {
//            Set<Map.Entry<String, Double>> entries = tagsMap.entrySet();
//
//            if (tagsMap.size() == 1) {
//                Iterator<Map.Entry<String, Double>> iterator = entries.iterator();
//                Map.Entry<String, Double> entry = iterator.next();
//                tags.add(new Tag(4, entry.getKey()));
//            } else {
//                List<Map.Entry<String, Double>> tagEntries = entries.stream().collect(Collectors.toList());
//
//                tagEntries.sort((e1, e2) -> -Double.compare(e1.getValue(), e2.getValue()));
//
//                int id = 4;
//                for (int i = 0; i < 2; ++i) {
//                    Map.Entry<String, Double> tagEntry = tagEntries.get(i);
//                    tags.add(new Tag(id++, tagEntry.getKey()));
//                }
//            }
//        }
//        return tags;
//    }


//    private boolean hasBigImg(VideoItem doc) {
//        return doc.getBigImg() != null && !doc.getBigImg().isEmpty();
//    }
//
//    public int imageSize(VideoItem doc) {
//        List<String> imgs = doc.getImgs();
//        if (imgs == null || imgs.isEmpty()) {
//            return 0;
//        }
//        return imgs.size();
//    }

    public static void main(String[] args) {

        RecommendRequest recommendRequest = new RecommendRequest();
        recommendRequest.setCity("北京市");
        VideoRecommenderContext videoRecommenderContext = new VideoRecommenderContext(null, null, null, new Items(), false);
        videoRecommenderContext.setRecommendRequest(recommendRequest);
        Map<String, String> vidmap = RecReasonsMap.getVidToReasonMap();
        Map<String, String> tmp = new HashMap<>();
        for (String key : vidmap.keySet()) {
            String value = vidmap.get(key);
            String tmpvalue = getConvertReason(value, videoRecommenderContext);
            System.out.println(tmpvalue);
        }

//        List<String> idlist = Arrays.asList("1","2", "3");
//        idlist.stream().forEach(x->System.out.println(x));
//        List<String> result = Arrays.asList(null, null, null);
//        Map<String, String> idmap = IntStream.range(0, idlist.size())
//                .mapToObj(i->new Pair<String, String>(idlist.get(i), result.get(i)))
//                .filter(x->x.getValue() != null)
//                .collect(Collectors.toMap(o->o.getKey(),o->o.getValue()));
//        System.out.println(idmap.size());
//        for (String key: idmap.keySet()) {
//            System.out.println("key:"+key+" value:"+idmap.get(key));
//        }

//        VideoItem staticDocumentData = new VideoItem();
//        staticDocumentData.setFrom("五道口");
//        HashMap<String, Double> objectObjectHashMap = new HashMap<>();
//        objectObjectHashMap.put("小米", 0.1);
//        objectObjectHashMap.put("华为", 0.4);
//
//        staticDocumentData.setTagsMap(objectObjectHashMap);
//        NewsDocBuilder.getInstance().buildDislikeTags(staticDocumentData, "abc");
//        NewsDocBuilder.getInstance().buildDislikeTags(staticDocumentData, "abc");
//
//        NewsDocBuilder.getInstance().buildDislikeTags(staticDocumentData, "def");
//        NewsDocBuilder.getInstance().buildDislikeTags(staticDocumentData, "def");
//        NewsDocBuilder.getInstance().buildDislikeTags(staticDocumentData, "adsfasdfasdf");
//        NewsDocBuilder.getInstance().buildDislikeTags(staticDocumentData, "fesz");

    }
}
