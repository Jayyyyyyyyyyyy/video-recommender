package com.td.recommend.video.api.vo;

import com.td.data.profile.common.KeyConstants;
import com.td.data.profile.item.ItemDocumentData;
import com.td.data.profile.item.KeyItem;
import com.td.data.profile.item.VideoItem;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.json.JsonUtils;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.docstore.dao.DocItemDao;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.rank.featuredumper.bean.VideoStaticFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VideoStaticFeatureBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(VideoStaticFeatureBuilder.class);
    public VideoStaticFeatureBuilder(){}
    public VideoStaticFeatureBuilder getInstance() {
        return Handler.instance;
    }

    private static class Handler{
        private static VideoStaticFeatureBuilder instance = new VideoStaticFeatureBuilder();
    }

    public Optional<VideoStaticFeature> build(DocItem docItem) {
        try {
            Optional<ItemDocumentData> itemDocumentData = docItem.getNewsDocumentData();
            if (!itemDocumentData.isPresent()) {
                return Optional.empty();
            }
            ItemDocumentData ItemDocumentData = itemDocumentData.get();
            Optional<VideoItem> staticDocumentDataOpt = ItemDocumentData.getStaticDocumentData();
            if (!staticDocumentDataOpt.isPresent()) {
                return Optional.empty();
            }


            VideoItem staticDocumentData = staticDocumentDataOpt.get();
            VideoStaticFeature itemFeature = new VideoStaticFeature();
            Map<String, Integer> tempMap;
            itemFeature.setTalentstar(DocProfileUtils.getTalentStar(docItem));
            try {
                tempMap = getFeatrueMap(staticDocumentData,KeyConstants.content_genre);
                itemFeature.setContent_genre(tempMap);
            }catch (Exception e){
            }
            try {
                tempMap = getFeatrueMap(staticDocumentData,KeyConstants.content_teach);
                itemFeature.setContent_teach(tempMap);
            }catch (Exception e){
            }
            try {
                tempMap = getFeatrueMap(staticDocumentData,KeyConstants.secondcat);
                itemFeature.setSecondcat(tempMap);
            }catch (Exception e){
            }
            try {
                tempMap = getFeatrueMap(staticDocumentData,KeyConstants.firstcat);
                itemFeature.setFirstcat(tempMap);
            }catch (Exception e){
            }
            try {
                tempMap = getFeatrueMap(staticDocumentData,KeyConstants.content_teacher);
                itemFeature.setContent_teacher(tempMap);
            }catch (Exception e){
            }
            try {
                Map<String,String> content_mp3 = new HashMap<>();
                String mp3Name = staticDocumentData.getKeyItemByName(KeyConstants.content_mp3).get().getName();
                content_mp3.put("tagname",mp3Name);
                itemFeature.setContent_mp3(content_mp3);
            }catch (Exception e){
            }
            try {
                List<Map<String, Integer>> exerciseBody = new ArrayList<>();
                List<KeyItem> bodyList = staticDocumentData.getExercise_body();
                for(int i=0;i<bodyList.size();i++){
                    Map<String, Integer> tagMap = new HashMap<>();
                    String id = bodyList.get(i).getId();
                    if("0".equals(id)){
                        continue;
                    }
                    Integer tagId = Integer.valueOf(id);
                    tagMap.put("tagid",tagId);
                    exerciseBody.add(tagMap);
                }
                if(exerciseBody.size()>0) {
                    itemFeature.setExercise_body(exerciseBody);
                }
            }catch (Exception e){
            }
            try {
                List<Map<String, Integer>> content_tag = new ArrayList<>();
                Map<String, Integer> tagMap;
                List<KeyItem> tagList = staticDocumentData.getContent_tag();
                for(int i=0;i<tagList.size();i++){
                    tagMap = new HashMap<>();
                    String id = tagList.get(i).getId();
                    if("0".equals(id)){
                        continue;
                    }
                    Integer tagId = Integer.valueOf(id);
                    tagMap.put("tagid",tagId);
                    content_tag.add(tagMap);
                }
                if(content_tag.size()>0) {
                    itemFeature.setContent_tag(content_tag);
                }
            }catch (Exception e){

            }
            try {
                String ctime = staticDocumentData.getCreatetime();
                itemFeature.setCtime(Long.valueOf(ctime));
            }catch (Exception e){
            }
            try {
                itemFeature.setDuration(staticDocumentData.getDuration());
            }catch (Exception e){
            }
            try {
                itemFeature.setUid(staticDocumentData.getUid());
            }catch (Exception e){
            }
            try {
                itemFeature.setTitle_len(staticDocumentData.getTitle_len());
            }catch (Exception e){
            }
            try {
                String createtime = staticDocumentData.getCreatetime();
                SimpleDateFormat formatter =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
                Long ctime = formatter.parse(createtime).getTime();
                itemFeature.setCtime(ctime);
            }catch (ParseException e){}
            return Optional.of(itemFeature);
        } catch (Exception e) {
            LOG.error("build news doc failed!", e);
            return Optional.empty();
        }
    }

    public Map<String, Integer> getFeatrueMap(VideoItem staticDocumentData,String fieldName){
        Map<String, Integer> tempMap = new HashMap<>();
        String fea = staticDocumentData.getKeyItemByName(fieldName).get().getId();
        if("0".equals(fea)){
            return null;
        }
        tempMap.put("tagid",Integer.valueOf(fea));
        return tempMap;
    }

    public static void main(String[] args) {//1500664260689
        Optional<DocItem> docItem = new DocItemDao().get("1500672393648");
        PredictItem<DocItem> predictItem = new PredictItem<>();
        predictItem.setItem(docItem.get());
        Optional<VideoStaticFeature> itemp = new VideoStaticFeatureBuilder().build(predictItem.getItem());
        Optional<String> dumpJsonOpt = JsonUtils.toJson(itemp.get());
        if (dumpJsonOpt.isPresent()) {
            System.out.println(dumpJsonOpt.get());
        }
        String createtime = docItem.get().getNewsDocumentData().get().getStaticDocumentData().get().getCreatetime();
        System.out.println();
    }
}
