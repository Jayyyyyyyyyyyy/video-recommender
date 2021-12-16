package com.td.recommend.video.retriever;

import com.google.common.collect.ImmutableSet;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.Id;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.blender.Queue;
import com.td.recommend.core.recommender.RecommendContext;
import com.td.recommend.core.retriever.IRetriever;
import com.td.recommend.core.validator.IValidator;
import com.td.recommend.docstore.dao.DocItemDao;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.retriever.engine.core.IRetrieveContextBuilder;
import com.td.recommend.retriever.engine.core.RetrieveContext;
import com.td.recommend.retriever.engine.core.RetrieveEngine;
import com.td.recommend.retriever.engine.core.RetrieveResultMap;
import com.td.recommend.retriever.engine.retriever.RetrieveResult;
import com.td.recommend.validator.IBatchValidator;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.utils.TrendUidInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sunjian on 2021/09/06.
 */

public class WithTrendRetrieverEngineBasedRetriever implements IRetriever {
    private static final Logger LOG = LoggerFactory.getLogger(WithTrendRetrieverEngineBasedRetriever.class);
    private RetrieveEngine<IItem> retrieveEngine;
    private IRetrieveContextBuilder retrieveContextBuilder;
    private IBatchValidator<IItem> batchValidator;
    //private RecommendContext<? extends IItem> recommendContext;
    private VideoRecommenderContext videoRecommendContext;
    private static final Set<String> multiRecall = ImmutableSet.of("tfollow_ev", "thot");


    public WithTrendRetrieverEngineBasedRetriever(RetrieveEngine<IItem> retrieveEngine, IRetrieveContextBuilder retrieveContextBuilder, VideoRecommenderContext recommendContext, IBatchValidator<IItem> batchValidator) {
        this.retrieveEngine = retrieveEngine;
        this.retrieveContextBuilder = retrieveContextBuilder;
        this.batchValidator = batchValidator;
        this.videoRecommendContext = recommendContext;
    }

    public WithTrendRetrieverEngineBasedRetriever(RetrieveEngine<IItem> retrieveEngine, IRetrieveContextBuilder retrieveContextBuilder, VideoRecommenderContext recommendContext) {
        this(retrieveEngine, retrieveContextBuilder, recommendContext, (IBatchValidator)null);
    }

    public Map<RetrieveKey, Queue<? extends IItem>> retrieve(Collection<RetrieveKey> retrieveKeys, IValidator<IItem> validator) {
        RetrieveContext retrieveContext = this.retrieveContextBuilder.build(retrieveKeys, new HashMap());
        HashMap resultQueueMap = new HashMap();
        Map<String, Map<String,List<String>>> filterQueueMap = new HashMap<>();
        int ihf = this.videoRecommendContext.getRecommendRequest().getIhf();
        Map<String,Integer> filterTypes = TrendUidInfo.getFilterTypes(ihf);

        try {
            RetrieveResultMap<IItem> retrieveResultMap = this.retrieveEngine.apply(retrieveContext);
            Map<String, RetrieveResult<?, IItem>> resultMap = retrieveResultMap.getResultMap();
            Collection<RetrieveResult<?, IItem>> retrieveResults = resultMap.values();
            List<IItem> items = (List)retrieveResults.stream().flatMap((i) -> {
                return i.getItemMap().values().stream().flatMap(Collection::stream);
            }).collect(Collectors.toList());
            List<String> exposes = this.videoRecommendContext.getOneDayExposes();
            HashSet<String> recentExposes = new HashSet(exposes.subList(0, Math.min(exposes.size(), 60)));
            HashSet globalValidItems;

            List<String> exposesHistory = this.videoRecommendContext.getExposes();
            HashSet<String> recentExposesHisotry = new HashSet<>(exposesHistory.subList(0, Math.min(exposesHistory.size(), 1000) ));

            if (this.batchValidator != null) {
                globalValidItems = new HashSet(this.batchValidator.filter(items));
            } else {
                globalValidItems = new HashSet(items);
            }

            Iterator var12 = retrieveResults.iterator();

            while(var12.hasNext()) {
                RetrieveResult<?, IItem> retrieveResult = (RetrieveResult)var12.next();
                Map<?, List<IItem>> itemMap = retrieveResult.getItemMap();
                Set<? extends Entry<?, List<IItem>>> itemEntries = itemMap.entrySet();
                Iterator var16 = itemEntries.iterator();


                while(var16.hasNext()) {
                    Entry<?, List<IItem>> itemEntry = (Entry)var16.next();
                    RetrieveKey retrieveKey = (RetrieveKey)itemEntry.getKey();
                    String type = retrieveKey.getType();
//                    if(ihf==Ihf.VSHOWDANCE_FEED.id()){
//                        if(type.equals("tuidfollow")){
//                            LOG.info("Debug_trend_vid.Step1: has type={},reqeust.trendUid={},ihf={},diu={}",
//                                    type,
//                                    this.videoRecommendContext.getRecommendRequest().getTrendUid(),
//                                    this.videoRecommendContext.getRecommendRequest().getIhf(),
//                                    this.videoRecommendContext.getRecommendRequest().getDiu());
//                        }
//                    }

                    List validItems;
                    if (retrieveKey.getType().endsWith("_seen") || retrieveKey.getType().equals("tuidfollow")) {
                        validItems = (List)((List<IItem>)itemEntry.getValue()).stream().filter((i) -> {
                            return !recentExposes.contains(i.getId());
                        }).collect(Collectors.toList());
                    } else {
                        validItems = (List) IntStream.range(0, ((List)itemEntry.getValue()).size()).peek((pos) -> {
                            ((IItem)((List)itemEntry.getValue()).get(pos)).addTag("pos", Integer.toString(pos));
                        }).filter((pos) -> {
                            return globalValidItems.contains(((List)itemEntry.getValue()).get(pos));
                        }).mapToObj((pos) -> {
                            return (IItem)((List)itemEntry.getValue()).get(pos);
                        }).collect(Collectors.toList());
                    }

                    Queue<? extends IItem> queue = new Queue(validItems, validator);

                    if(ihf== Ihf.VMIX_FEED.id() &&
                            !multiRecall.contains(type) ){
                        resultQueueMap.put(retrieveKey, queue);
                    }
                    else{
                        resultQueueMap.put(retrieveKey, queue);
                    }

                    if(filterTypes.containsKey(type)){
                        String uid = retrieveKey.getKey();

                        List idlist = queue.getItems().stream().map(Id::getId).collect(Collectors.toList());

//                        List idlist = (List)(queue.getItems().stream().filter((i) -> {
//                            if(type.equals("tuidfollow")){
//                                return true;
//                            }
//                            else{
//                                return !recentExposesHisotry.contains(i.getId());
//                            }
//
//                        }).map(Id::getId).collect(Collectors.toList()));
//
//                        if(ihf==Ihf.VSHOWDANCE_FEED.id()) {
//                            if (type.equals("tuidfollow")) {
//                                String idlist_string = queue.getItems().stream().limit(5).map(Id::getId).collect(Collectors.joining(","));
//
//                                LOG.info("Debug_trend_vid.Step2: type={},before.idlist={}, aftersize={},reqeust.trendUid={},ihf={},diu={}",
//                                        type,
//                                        idlist_string,
//                                        idlist.size(),
//                                        this.videoRecommendContext.getRecommendRequest().getTrendUid(),
//                                        this.videoRecommendContext.getRecommendRequest().getIhf(),
//                                        this.videoRecommendContext.getRecommendRequest().getDiu());
//                            }
//                        }

                        if(idlist.size()>0){
                            filterQueueMap.
                                    computeIfAbsent(type, k -> new LinkedHashMap<>()).
                                    computeIfAbsent(uid, k -> new ArrayList<>()).
                                    addAll(idlist);
                        }
                    }
                }
            }

            if(filterQueueMap.size()>0){
                Map<String, String> uid2vid = new HashMap<>();
                int sizemax = 10;
                filterTypes.entrySet().stream().filter(entry ->{return filterQueueMap.containsKey(entry.getKey());}).forEachOrdered( entry ->{

//                    LOG.info("Debug_trend_vid.Step3:filterQueueMap.getKey.size={},entry.key={},ihf={},diu={}",
//                            filterQueueMap.get(entry.getKey()).size(),
//                            entry.getKey(),
//                            this.videoRecommendContext.getRecommendRequest().getIhf(),
//                            this.videoRecommendContext.getRecommendRequest().getDiu());

                    filterQueueMap.get(entry.getKey()).forEach((uid, vidlist) -> {
                        //debug
//                                if(entry.getKey().equals("tuidfollow") && ihf==Ihf.VSHOWDANCE_FEED.id()){
//                                    LOG.info("Debug_trend_vid.Step4:uid={},vidlistsize={},top5vidstring={},ihf={},diu={}",
//                                            uid,
//                                            vidlist.size(),
//                                            String.join(",",vidlist.subList(0,Math.min(vidlist.size(),5))),
//                                            this.videoRecommendContext.getRecommendRequest().getIhf(),
//                                            this.videoRecommendContext.getRecommendRequest().getDiu());
//
//                                }

                        if (vidlist.size() > 0) {
                            if (ihf == Ihf.VSHOWDANCE_FEED.id()) { //tuidfololow recall
                                if (StringUtils.isBlank(this.videoRecommendContext.getTrendVidStrings())) {
                                    this.videoRecommendContext.setTrendUid(uid);
                                    this.videoRecommendContext.setTrendVidStrings(vidlist.get(0));
                                    if (!uid.equals(this.videoRecommendContext.getRecommendRequest().getTrendUid())) {
                                        LOG.error("trend_uid get uid error:uid{},trendUid{}", uid,
                                                this.videoRecommendContext.getRecommendRequest().getTrendUid());
                                    }
                                }

                            } else {
                                if (StringUtils.isNumeric(uid)) {
                                    if (uid2vid.size() < sizemax) {
                                        if (!uid2vid.containsKey(uid)) {
                                            uid2vid.put(uid, uid + "," + vidlist.get(0) + "," + entry.getKey() + "," + getUidInfo());
                                        }
                                    }
                                } else {
                                    for (int i = 0; i < vidlist.size(); i++) {
                                        if (uid2vid.size() < sizemax) {
                                            String vid = vidlist.get(i);
                                            DocItem vid2docItem = new DocItemDao().get(vid).get();
                                            String vid2uid = DocProfileUtils.getUid(vid2docItem);
                                            if (!(vid2uid.equals("-1"))) {
                                                if (!uid2vid.containsKey(vid2uid)) {
                                                    uid2vid.put(vid2uid, vid2uid + "," + vid + "," + entry.getKey() + "," + getUidInfo());
                                                }
                                            }
                                        }
                                        if (i >= sizemax * 2 || uid2vid.size() >= sizemax) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    });
                    if(ihf!=Ihf.VSHOWDANCE_FEED.id()){
                        if(uid2vid.size()>0){
                            List<String> valuelist = new ArrayList<>(uid2vid.values());
                            Collections.shuffle(valuelist);
                            String[] tokens = valuelist.get(0).split(",");
                            if(tokens.length==4){
                                this.videoRecommendContext.setTrendUid(tokens[0]);
                                this.videoRecommendContext.setTrendVidStrings(tokens[1]);
                                this.videoRecommendContext.setTrendRecall(tokens[2]);
                                this.videoRecommendContext.setTrendUidInfo(tokens[3]);
                            }
                        }
                        else{
                            LOG.error("trend_uid get error:size{}",uid2vid.size());
                        }
                    }
                });
            }
        } catch (Exception var21) {
            LOG.error("retrieve engine failed!", var21);
        }

        LOG.info("trend_ RetrieveTypestring:{},filterQueueMap.size:{},uid:{},vid:{},recall:{},reqeust.trendUid:{},trendVid:{},ihf:{},diu:{},",
                String.join(",", filterQueueMap.keySet()),
                filterQueueMap.size(),
                this.videoRecommendContext.getTrendUid(),
                this.videoRecommendContext.getTrendVidStrings(),
                this.videoRecommendContext.getTrendRecall(),
                this.videoRecommendContext.getRecommendRequest().getTrendUid(),
                this.videoRecommendContext.getRecommendRequest().getTrendVidString(),
                this.videoRecommendContext.getRecommendRequest().getIhf(),
                this.videoRecommendContext.getRecommendRequest().getDiu());

        return resultQueueMap;
    }
    private String getUidInfo(){
        String uidInfo = "";
        ArrayList<String> list = new ArrayList<String>(TrendUidInfo.get());
        Collections.shuffle(list);

        for (String onetext : list) {
            if(StringUtils.isNotBlank(onetext)){
                uidInfo = onetext;
                break;
            }
        }
        if(StringUtils.isBlank(uidInfo)){
            uidInfo = "在秀舞";
        }
        return uidInfo;
    }
}
