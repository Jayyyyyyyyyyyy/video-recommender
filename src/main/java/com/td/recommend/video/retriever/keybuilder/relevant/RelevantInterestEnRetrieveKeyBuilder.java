package com.td.recommend.video.retriever.keybuilder.relevant;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.td.data.profile.TVariance;
import com.td.data.profile.common.KeyConstants;
import com.td.data.profile.item.KeyItem;
import com.td.data.profile.item.VideoItem;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;
import com.td.recommend.video.utils.InterestUtils;
import jdk.nashorn.internal.runtime.options.Option;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.stream.Collectors;

public class RelevantInterestEnRetrieveKeyBuilder implements RetrieveKeyBuilder {

    private static final Map<String, String> targetInterestGroup = ImmutableMap.<String, String>builder()
            .put("1705","1705")
            .put("1007","1007")
            .build();
    private  final static ImmutablePair vcat = ImmutablePair.of("vcat_ck","vcat_cs");
    private  final static ImmutablePair vsubcat = ImmutablePair.of("vsubcat_ck","vsubcat_cs");
    private final static int maxCnt = 20;
    public VideoRecommenderContext recommendContext;
    public RelevantInterestEnRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }


    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<RetrieverType.RlvtInterestEn> ienList = Arrays.stream(RetrieverType.RlvtInterestEn.values()).collect(Collectors.toList());
        Set<RetrieveKey> retrieveKeys = generate(userItem, ienList);
        retrieveKeyContext.addRetrieveKeys(retrieveKeys);
    }

    private Set<RetrieveKey> generate(UserItem userItem, List<RetrieverType.RlvtInterestEn> ienList) {
        String vid = recommendContext.getRecommendRequest().getVid();
        Optional<VideoItem> videoItemOptional = Optional.empty();
        try {
            Optional<DocItem> docItem = UserVideoItemDataSource.getInstance().getCandidateDAO().get(vid);
            videoItemOptional = docItem.get()
                    .getNewsDocumentData().get()
                    .getStaticDocumentData();
        } catch (Exception e) {
            return Collections.emptySet();
        }
        if (!videoItemOptional.isPresent()) {
            return Collections.emptySet();
        }
        Set<RetrieveKey> retrieveKeys = new HashSet<>();
        for (RetrieverType.RlvtInterestEn candidate: ienList) {
            addRetrieveKey(retrieveKeys, candidate,videoItemOptional.get(), recommendContext);
        }
        return retrieveKeys;
    }

    private void addRetrieveKey(Set<RetrieveKey> retrieveKeys, RetrieverType.RlvtInterestEn canidate, VideoItem videoItem, VideoRecommenderContext context) {
        String key = canidate.getKey();
        if (canidate.getAlias().equals("exercise_body.tagid")) {
            List<KeyItem> exerciselist = videoItem.getExercise_body();
            for (KeyItem keyItem: exerciselist) {
                if (keyItem.getId().equals(key)) {
                    RetrieveKey retrieveKey = buildRetrieveKey(canidate, context);
                    retrieveKeys.add(retrieveKey);
                    break;
                }
            }
        } else if (canidate.getAlias().equals("secondcat.tagid")) {
            Optional<KeyItem> keyItemOptional = videoItem.getKeyItemByName(KeyConstants.secondcat);
            if (keyItemOptional.isPresent()) {
                KeyItem keyItem = keyItemOptional.get();
                if (keyItem.getId().equals(key)) {
                    RetrieveKey retrieveKey = buildRetrieveKey(canidate, context);
                    retrieveKeys.add(retrieveKey);
                }
            }
        }
    }


    private RetrieveKey buildRetrieveKey(RetrieverType.RlvtInterestEn canidate, VideoRecommenderContext context) {
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(canidate.name());
        retrieveKey.setKey(canidate.getKey());
        retrieveKey.setAlias(canidate.getAlias());
        retrieveKey.setScore(1.0);
        retrieveKey.addAttribute("maxCnt", maxCnt);
        if (context.hasBucket("relevant_interest-expreason")) {
            retrieveKey.setReason(canidate.getReason());
        }
        return  retrieveKey;
    }

}
