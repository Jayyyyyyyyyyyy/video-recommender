package com.td.recommend.video.retriever.keybuilder;


import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.ItemBasedRetrieveKeyBuilder;
import com.td.recommend.commons.retriever.RetrieveKeyBuilderPipeline;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.abtest.BucketConstants;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.keybuilder.smallvideo.*;
import org.apache.commons.lang3.StringUtils;
import com.td.recommend.video.retriever.keybuilder.trend.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/7/14.
 */
public class VideoFeedRetrieveKeyBuilderFactory implements RetrieveKeyBuilderFactory {
    private static VideoFeedRetrieveKeyBuilderFactory instance = new VideoFeedRetrieveKeyBuilderFactory();

    public static VideoFeedRetrieveKeyBuilderFactory getInstance() {
        return instance;
    }

    public VideoRetrieveKeyBuilder create(VideoRecommenderContext context) {
        List<ItemBasedRetrieveKeyBuilder<UserItem>> retrieveKeyBuilders = new ArrayList<>();

        retrieveKeyBuilders.addAll(buildNormalUserKeys(context));

        RetrieveKeyBuilderPipeline<UserItem> pipeline = new RetrieveKeyBuilderPipeline<>(retrieveKeyBuilders);

        return new VideoRetrieveKeyBuilder(pipeline, context);
    }

    private List<ItemBasedRetrieveKeyBuilder<UserItem>> buildNormalUserKeys(VideoRecommenderContext context) {
        int ihf = context.getRecommendRequest().getIhf();
        List<ItemBasedRetrieveKeyBuilder<UserItem>> retrieveKeyBuilders = new ArrayList<>();
        if (ihf == Ihf.VFITPACK_FEED.id()) {
            retrieveKeyBuilders.add(new FitPackSubcatRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new ItemCFRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new GemV2RetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new BertV2RetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new UserCFRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new NmfRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new BprRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new RealTimeSearchRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new RepeatSeenRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new SearchRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new SelfRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new XFollowRetrieveKeyBuilder(context));
        } else if (ihf == Ihf.VSMALL_FEED.id()) {
            retrieveKeyBuilders.add(new SmallHotRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new SmallRandomEnsureRetrieveKeyBuilder());
            retrieveKeyBuilders.add(new SmallCatRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new SmallSubcatRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new SmallTagRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new SmallAuthorRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new SmallMp3RetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new SmallGenreRetrieveKeyBuilder(context));
        } else {
            if (context.getUserType() == UserProfileUtils.UserType.new_interest) {
                if (context.hasBucket("newuser_strategy-exp")) {
                    retrieveKeyBuilders.add(new NewUserFreshMp3RetrieveKeyBuilder(context));
                }
                retrieveKeyBuilders.add(new NewUserRecomeRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new NewUserHotRetrieveKeyBuilder(context));

            } else if (context.getUserType() == UserProfileUtils.UserType.new_bare) {
                if (context.hasBucket("newuser_strategy-exp")) {
                    retrieveKeyBuilders.add(new NewUserFreshMp3RetrieveKeyBuilder(context));
                }
                retrieveKeyBuilders.add(new NewUserRecomeRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new NewUserHotRetrieveKeyBuilder(context));

            } else if (context.getUserType() == UserProfileUtils.UserType.old_bare) {
                retrieveKeyBuilders.add(new PopularRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new TalentRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new TalentFreshRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new HotRetrieveKeyBuilder(context));
                /*explore*/
                if(!Ihf.isTrend(ihf)){
                    retrieveKeyBuilders.add(new EuRetrieveKeyBuilder(context));
                }


            } else {
                retrieveKeyBuilders.add(new RandomEnsureRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new BertV2RetrieveKeyBuilder(context));
                /*explore*/
                if(!Ihf.isTrend(ihf)) {
                    retrieveKeyBuilders.add(new EuRetrieveKeyBuilder(context));
                }
                retrieveKeyBuilders.add(new TopRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new HorseRetrieveKeyBuilder(context));
            }

            /*interests*/
            retrieveKeyBuilders.add(new AuthorRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new CatRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new TagRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new SubcatRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new PhraseRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new GenreRetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new Mp3RetrieveKeyBuilder(context));
            retrieveKeyBuilders.add(new ExerciseBodyRetrieveKeyBuilder(context));
            if (context.hasBucket("include-yes")) {
                retrieveKeyBuilders.add(new IncludeRetrieveKeyBuilder(context));
            }
            /*u2i - app only*/
            if (context.getRecommendRequest().getAppId().equals("t01")) {
                retrieveKeyBuilders.add(new UserCFRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new NmfRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new BprRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new RealTimeSearchRetrieveKeyBuilder(context));
                String ab = context.getRecommendRequest().getAb();
                if ((StringUtils.isEmpty(ab) || !BucketConstants.abSet.contains(ab) ) && !Ihf.isTrend(ihf)) {
                    retrieveKeyBuilders.add(new RepeatSeenRetrieveKeyBuilder(context));
                }
                retrieveKeyBuilders.add(new SearchRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new SelfRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new XFollowRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new ClusterRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new MinetRetrieveKeyBuilder(context));
                if (context.hasBucket("vsplitflow-exp")) {
                    retrieveKeyBuilders.add(new SplitFlowRetrieveKeyBuilder(context));
                }
            } else if (context.getRecommendRequest().getAppId().equals("t02")) {
                if (context.hasBucket("wxapp_implicit_recall-yes")) {
                    retrieveKeyBuilders.add(new NmfRetrieveKeyBuilder(context));
                    retrieveKeyBuilders.add(new UserCFRetrieveKeyBuilder(context));
                }
            }

            retrieveKeyBuilders.add(new GemV2RetrieveKeyBuilder(context));

            if(ihf == Ihf.VSHOWDANCE_FEED.id()){
                retrieveKeyBuilders.add(new OfAlbumRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new TrendRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new SelfRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new TalentFreshRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new ItemCFTrendRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new THotRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new TuidRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new FollowWatchRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new FollowWorksRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new LocationRetrieveKeyBuilder(context));

            }
            else{
                /*i2i*/
                retrieveKeyBuilders.add(new ItemCFRetrieveKeyBuilder(context));
                if (context.hasBucket("itemcfv2-yes")) {
                    retrieveKeyBuilders.add(new ItemCFV2RetrieveKeyBuilder(context));
                }
            }
            //only for app feed with fp=1
            if(ihf ==Ihf.VMIX_FEED.id() && context.getRecommendRequest().getFp()==1){
                retrieveKeyBuilders.add(new TrendRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new XFollowRetrieveKeyBuilder(context));
                retrieveKeyBuilders.add(new THotRetrieveKeyBuilder(context));
            }

            /*others*/
            if (context.hasBucket("blast-yes")) {
                retrieveKeyBuilders.add(new BlastRetrieveKeyBuilder(context));
            }
            //talentcluster
            if(!Ihf.isTrend(ihf) &&  context.hasBucket("talentcluster-yes")){
                retrieveKeyBuilders.add(new TalentClusterRetrieveKeyBuilder(context));
            }
            //talentclusterv2
            if(! Ihf.isTrend(ihf) && context.hasBucket("talentclusterv2-yes")){
                retrieveKeyBuilders.add(new TalentClusterV2RetrieveKeyBuilder(context));
            }
        }
        return retrieveKeyBuilders;
    }

}
