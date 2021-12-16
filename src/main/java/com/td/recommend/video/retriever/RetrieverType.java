package com.td.recommend.video.retriever;

import lombok.AllArgsConstructor;
import lombok.Getter;

public enum RetrieverType {
    vcat("firstcat.tagid"),
    svcat("firstcat.tagid"),
    svcat_chnl("firstcat.tagid"),
    vxcat("virtual_firstcat.tagid"),
    vxcat_st("virtual_firstcat.tagid"),
    svxcat("firstcat.tagid"),
    vcat_ev("firstcat.tagid"),
    svcat_ev("firstcat.tagid"),
    vxcat_ev("virtual_firstcat.tagid"),
    vxcat_en("virtual_firstcat.tagid"),
    vxcat_eu("virtual_firstcat.tagid"),
    vxcat_fr("virtual_firstcat.tagid"),
    vcat_en("firstcat.tagid"),
    vcat_eu("firstcat.tagid"),
    vcat_st("firstcat.tagid"),
    vcat_fr("firstcat.tagid"),
    vcat_f3("firstcat.tagid"),
    vcat_rc("firstcat.tagid"),
    svcat_st("firstcat.tagid"),
    vcat_chnl("firstcat.tagid"),
    vcat_rlvt("firstcat.tagid"),
    svcat_rlvt("firstcat.tagid"),
    vcat_init("firstcat.tagid"),
    vcat_ext("firstcat.tagid"),
    vcat_live("firstcat.tagid"),

    vsubcat("secondcat.tagid"),
    vsubcat_chnl("secondcat.tagid"),
    vsubcat_tr("secondcat.tagid"),
    vsubcat_op("secondcat.tagid"),
    svsubcat("secondcat.tagid"),
    vxsubcat("virtual_secondcat.tagid"),
    vxsubcat_st("virtual_secondcat.tagid"),
    svxsubcat("secondcat.tagid"),
    vsubcat_ev("secondcat.tagid"),
    svsubcat_ev("secondcat.tagid"),
    vxsubcat_ev("virtual_secondcat.tagid"),
    vxsubcat_en("virtual_secondcat.tagid"),
    vxsubcat_eu("virtual_secondcat.tagid"),
    vxsubcat_fr("virtual_secondcat.tagid"),
    vxsubcat_tr("virtual_secondcat.tagid"),
    vsubcat_en("secondcat.tagid"),
    vsubcat_eu("secondcat.tagid"),
    vsubcat_st("secondcat.tagid"),
    vsubcat_fr("secondcat.tagid"),
    vsubcat_f3("secondcat.tagid"),
    vsubcat_rc("secondcat.tagid"),
    svsubcat_st("secondcat.tagid"),
    vsubcat_rlvt("secondcat.tagid"),
    svsubcat_rlvt("secondcat.tagid"),
    vsubcat_init("secondcat.tagid"),
    vsubcat_ext("secondcat.tagid"),
    vsubcat_live("secondcat.tagid"),

    vtag("content_tag.tagid"),
    vtag_eu("content_tag.tagid"),
    svtag("content_tag.tagid"),
    vxtag("virtual_content_tag.tagid"),
    vxtag_st("virtual_content_tag.tagid"),
    svxtag("content_tag.tagid"),
    vtag_ev("content_tag.tagid"),
    vtag_rc("content_tag.tagid"),
    svtag_ev("content_tag.tagid"),
    vxtag_ev("virtual_content_tag.tagid"),
    vxtag_en("virtual_content_tag.tagid"),
    vxtag_fr("virtual_content_tag.tagid"),
    vtag_en("content_tag.tagid"),
    vtag_st("content_tag.tagid"),
    vtag_fr("content_tag.tagid"),
    vtag_f3("content_tag.tagid"),
    svtag_st("content_tag.tagid"),
    vtag_chnl("content_tag.tagid"),
    vtag_rlvt("content_tag.tagid"),
    svtag_rlvt("content_tag.tagid"),
    vtag_init("content_tag.tagid"),
    vtag_op("content_tag.tagid"),
    vtag_live("content_tag.tagid"),

    vmp3("content_mp3.tagname"),
    svmp3("content_mp3.tagname"),
    vxmp3("content_mp3.tagname"),
    svxmp3("content_mp3.tagname"),
    vmp3_ev("content_mp3.tagname"),
    svmp3_ev("content_mp3.tagname"),
    vxmp3_ev("content_mp3.tagname"),
    vmp3_en("content_mp3.tagname"),
    vmp3_st("content_mp3.tagname"),
    vmp3_fr("content_mp3.tagname"),
    svmp3_st("content_mp3.tagname"),
    vmp3_ext("content_mp3.tagname"),
    vmp3_rlvt("content_mp3.tagname"),
    svmp3_rlvt("content_mp3.tagname"),
    vtalentmp3_rlvt("content_mp3.tagname"),
    vmp3_ext_rlvt("content_mp3.tagname"),
    sevmp3("content_mp3.tagname"),
    hotsearchvmp3("content_mp3.tagname"),

    vbody("exercise_body.tagid"),
    vbody_ev("exercise_body.tagid"),
    vbody_fr("exercise_body.tagid"),
    vbody_st("exercise_body.tagid"),
    vbodyrank("exercise_body.tagid"),
    vbodyrank_st("exercise_body.tagid"),

    sevteacher("content_teacher.tagid"),
    hotsearchvteacher("content_teacher.tagid"),
    hotsearchvdance("content_dance.tagid"),

    vdegree("content_degree.tagid"),
    vdegree_init("content_degree.tagid"),

    vteam("uid_team_id"),
    vofalbum("album.tagid"),
    vofalbum_rlvt("album.tagid"),

    vauthor("uid"),
    svauthor("uid"),
    vxauthor("uid"),
    svxauthor("uid"),
    vauthor_ev("uid"),
    svauthor_ev("uid"),
    vxauthor_ev("uid"),
    vauthor_en("uid"),
    vauthor_st("uid"),
    vauthor_fr("uid"),
    vxfollow_f3("uid"),
    svauthor_st("uid"),
    vauthor_ext("uid"),
    vauthor_rlvt("uid"),
    svauthor_rlvt("uid"),
    vauthorfresh_rlvt("uid"),
    svauthorfresh_rlvt("uid"),
    vxfollow("uid"),
    vifollow("uid"),
    tfollow("uid"),
    tfollow_ev("uid"),
    t_hot_7day(""),
    t_hot_24hour(""),
    t_self_7day("uid"),
    vfollow("uid"),
    vfollow_st("uid"),
    vfollow_ext("uid"),
    vxfollow_rlvt("uid"),
    svxfollow_rlvt("uid"),
    vxfollow_live("uid"),
    vlive("uid"),
    vself("uid"),
    svauthorsupport("uid"),
    svauthorsupport_rlvt("uid"),


    vgenre("content_genre.tagid"),
    svgenre("content_genre.tagid"),
    vxgenre("content_genre.tagid"),
    svxgenre("content_genre.tagid"),
    vgenre_st("content_genre.tagid"),
    vgenre_fr("content_genre.tagid"),
    vgenre_f3("content_genre.tagid"),
    svgenre_st("content_genre.tagid"),
    vgenre_en("content_genre.tagid"),
    vgenre_ev("content_genre.tagid"),
    svgenre_ev("content_genre.tagid"),
    vxgenre_ev("content_genre.tagid"),
    vgenre_rlvt("content_genre.tagid"),
    svgenre_rlvt("content_genre.tagid"),

    vshow_rlvt("vshow_rlvt"),
    svhot("svhot"),
    vhot("vhot"),
    vathome("vathome"),
    valbum("valbum"),
    vblast("vblast_no_latent_negative_feedback"),//see: com.td.recommend.video.preprocessor.LatentFeedbackPreprocessor
    vblast_rlvt("vblast_rlvt"),
    vbasic("vbasic"),
    vhot_raw("firstcat.tagid"),
    vxhot_raw("virtual_firstcat.tagid"),
    vtalentfresh("vtalentfresh"),
    vtalent_raw("firstcat.tagid"),
    vxtalent_raw("virtual_firstcat.tagid"),
    vtalent("vtalent"),
    random_en("firstcat.tagid"),
    vxrandom_en("virtual_firstcat.tagid"),
    svrandom_en("firstcat.tagid"),
    vitemcf("vitemcf"),
    vitemcfv2("vitemcfv2"),
    vitemcftrend("vitemcftrend"),
    vitemcf_chnl("vitemcf"),
    vitemcf_rlvt("vitemcf"),
    vhighctr("vhighctr"),
    vhighctr_rlvt("vhighctr"),
    vtalentfresh_rlvt("talentfresh"),
    svtalentfresh_rlvt("talentfresh"),
    vbpr("vbpr"),
    vbpr_chnl("vbpr"),
    vbpr_rlvt("vbpr"),
    vusercf("vusercf"),
    vusercf_chnl("vusercf"),
    vusercf_rlvt("vusercf"),
    vusercfv3("vusercfv3"),
    vusercfv2boost("vusercfv2boost"),
    vheadpool("vheadpool"),
    vnce("vnce"),
    vfrom("vfrom"),
    vsearch("vsearch"),
    vteacher("vteacher"),
    vrealtimesearch("vrealtimesearch"),
    vsmall("vsmall"),
    vyoutubednn("vyoutubednn"),
    vyoutubednn_rlvt("vyoutubednn"),
    vgem("vgem"),
    vgem_rlvt("vgem"),
    vteaching("vteaching"),
    vpopular("vpopular"),
    vbert("vbert"),
    vbert_rlvt("vbert"),
    vbert64("vbert64"),
    vbert64_rlvt("vbert64"),
    vorigin_rlvt("vorigin"),
    vhotmp3("vhotmp3"),
    vnewhotmp3("vnewhotmp3"),
    vcity("vcity"),
    vdistrict("vdistrict"),
    vimmunity_album("vimmunity_album"),
    vrepeat_seen("vrepeat_seen"),
    vrepeatv1_seen("vrepeatv1_seen"),
    vrepeatv2_seen("vrepeatv2_seen"),
    vrt_repeat_seen("vrt_repeat_seen"),
    vfitness_seen("vfitness_seen"),
    vnmfv2("vnmfv2"),
    vnmfv3("vnmfv3"),
    vteachingresearch_rlvt("vteachingresearch"),
    vusercf_nmf("vusercf_nmf"),
    vquality("vquality"),
    vquality_classic("vv2quality_classic"),
    vimmersive_hot("vimmersive_hot"),
    vtitle_tr("secondcat.tagid"),
    valike_ev("valike_ev"),
    voperator_hot("voperator_hot"),
    vrecome("vrecome"),

    gbert("gbert"),
    gbpr("gbpr"),
    gfollow("uid"),
    ggem("ggem"),
    gitemcf("gitemcf"),
    gnmf("gnmf"),
    gusercf("gusercf"),
    grepeat_seen("grepeat_seen"),
    gcat("firstcat.tagid"),
    gsubcat("secondcat.tagid"),
    gtag("content_tag.tagid"),
    ggenre("content_genre.tagid"),
    vtagrank("content_tag.tagid"),
    vcatrank("firstcat.tagid"),
    vsubcatrank("secondcat.tagid"),
    vnewuserhot("vnewuserhot"),
    vphrase("content_phrase.tagid"),
    vphrase_op("content_phrase.tagid"),
    vphrase_rc("content_phrase.tagid"),
    vmp3rank("content_mp3.tagname"),
    vmp3rank_st("content_mp3.tagname"),
    vgenrerank("content_genre.tagid"),
    vgenrerank_st("content_genre.tagid"),
    vtagrank_st("content_tag.tagid"),
    vsubcatrank_st("secondcat.tagid"),
    vcatrank_st("firstcat.tagid"),
    vauthorrank_st("uid"),
    vusercfrank("vusercfrank"),
    vginterest_top("ginterest_top"),
    vitem2vec("vitem2vec"),
    vcluster("vcluster"),
    vminet("vminet"),
    vsplitflow("vsplitflow"),
    ttopic_rlvt("topic.tagid"),
    tfollow_rlvt("uid"),
    ttalentfresh_rlvt("talentfresh"),
    ttalentfresh("vtalentfresh"),
    thot("thot"),
    tuidfollow("uid"),//社区首页指定uid 召回
    tfollowwatch("uid"),
    tfollowworks("uid"),
    vtalentcluster("uid"),
    vtalentclusterv2("uid"),
    vlocation_city("video_city.tagname"),
    trend_ev(""),
    trandom(""),
    ;
    private String alias;

    RetrieverType(String alias) {
        this.alias = alias;
    }

    public String alias() {
        return alias;
    }


    @Getter
    @AllArgsConstructor
    public enum GTop {
        gfitness_top("group_subcat", "1007", "secondcat.tagid", "精品减肥操"),
        g32step_top("group_tag", "268", "content_tag.tagid", "精品32步"),
        ;

        private String facet;
        private String key;
        private String alias;
        private String reason;
    }

    @Getter
    @AllArgsConstructor
    public enum GEu {
        vfitness_eu("vsubcat_ck", "1007", "secondcat.tagid", ""),
        vhealth_eu("vsubcat_ck", "1009", "secondcat.tagid", ""),
        v32step_eu("vtag_ck", "268", "content_tag.tagid", ""),
        v64step_eu("vtag_ck", "1519", "content_tag.tagid", ""),
        gethnic_eu("vsubcat_ck", "285", "secondcat.tagid", ""),
        gshape_eu("vsubcat_ck", "300", "secondcat.tagid", ""),
        ;
        private String facet;
        private String key;
        private String alias;
        private String reason;
    }

    @Getter
    @AllArgsConstructor
    public enum Top {//走圈层业务画像
        vfitness_top("group_subcat", "1007", "secondcat.tagid", "精品减肥操"),
        v32step_top("group_tag", "268", "content_tag.tagid", "精品32步"),
        vhealth_top("group_subcat", "1009", "secondcat.tagid", "精品保健操"),
        vethnic_top("group_subcat", "285", "secondcat.tagid", "精品民族舞"),
        vshape_top("group_subcat", "300", "secondcat.tagid", "精品形体舞"),
        v64step_top("group_tag", "1519", "content_tag.tagid", "精品64步"),
        vbelly_top("group_tag", "1583", "content_tag.tagid", "精品瘦肚子"),
        vneck_top("group_tag", "1592", "content_tag.tagid", "精品颈椎操"),
        ;

        private String facet;
        private String key;
        private String alias;
        private String reason;
    }


    @Getter
    @AllArgsConstructor
    public enum NewTop {
//        vfitness_top("group_subcat", "1007", "secondcat.tagid", "精品减肥操"),
        vhealth_top("group_subcat", "1009", "secondcat.tagid", "精品保健操"),
//        vfashion_top("group_subcat", "312", "secondcat.tagid", "流行舞"),
//        vshuffle_top("group_subcat", "305", "secondcat.tagid", "鬼步舞"),
//        vmarine_top("group_subcat", "306", "secondcat.tagid", "水兵舞"),
        ;

        private String facet;
        private String key;
        private String alias;
        private String reason;
    }

    @Getter
    @AllArgsConstructor
    public enum Eu {
        vfitness_eu("vsubcat_ck", "1007", "secondcat.tagid", ""),
        vhealth_eu("vsubcat_ck", "1009", "secondcat.tagid", ""),
        vhealthdiet_eu("vsubcat_ck", "1053", "secondcat.tagid", ""),//养生饮食
        vneckvertebra_eu("vexercise_body_ck", "1705", "exercise_body.tagid", ""),//颈椎操

        vskin_eu("vsubcat_ck", "69", "secondcat.tagid", "exp"),
        vhair_eu("vsubcat_ck", "70", "secondcat.tagid", "exp"),
        vfasion_eu("vsubcat_ck", "71", "secondcat.tagid", "exp"),
        vmakeup_eu("vsubcat_ck", "73", "secondcat.tagid", "exp"),
        vcare_eu("vsubcat_ck", "4644", "secondcat.tagid", "exp"),

        vshoulder_eu("vexercise_body_ck", "1711", "exercise_body.tagid", ""),
        vknee_eu("vexercise_body_ck", "1706", "exercise_body.tagid", ""),
        vlumbar_eu("vexercise_body_ck", "1710", "exercise_body.tagid", ""),
        vwalk_eu("vtag_ck", "4621", "content_tag.tagid", ""),

        ;
        private String facet;
        private String key;
        private String alias;
        private String bucket;
    }

    @Getter
    @AllArgsConstructor
    public enum Recome {
        //        vsubcat_recome("", "1007,285,1009,300,312,265", "secondcat.tagid", "为您精选"),
//        vrecome_eu("", "s1007,s265,t280,s1009,s306,s285,s300,s312", "", ""),
        vsubcat_recome("", "1007,285,1009,300,312,265", "secondcat.tagid", "为您精选"),
        vrecome_eu("", "s1007,s265,s1009,s306,s285,s300,s312,s305", "", ""),

        ;
        private String facet;
        private String key;
        private String alias;
        private String reason;
    }

    @Getter
    @AllArgsConstructor
    public enum FreshMp3 {
        vfresh_mp3("", "264", "firstcat.tagid", "新歌新舞"),
        ;
        private String facet;
        private String key;
        private String alias;
        private String reason;
    }

    @Getter
    @AllArgsConstructor
    public enum Hot {
        vsubcat_hot("", "1007,285,1009,300,312,265", "secondcat.tagid", ""),
        vhot_eu("", "s1007,s265,s1009,s306,s285,s300,s312,s305", "", ""),
        ;
        private String facet;
        private String key;
        private String alias;
        private String reason;
    }

    @Getter
    @AllArgsConstructor
    public enum OperatorHot {
        voperator_hot("", "1007,285,1009,268,280,1592,300,1621,267,1519,306,312,305,273,266", "", ""),
        voperator_eu("", "1007,280,268,1009,285,300,1519,312,273,293,267", "", ""),
        ;
        private String facet;
        private String key;
        private String alias;
        private String reason;
    }

    @Getter
    @AllArgsConstructor
    public enum RlvtInterestEn {
        vneckvertebra_ien_rlvt("vexercise_body_ck", "1705", "exercise_body.tagid", "vexercise_body", "你可能喜欢"),
        vfitness_ien_rlvt("vsubcat_ck", "1007", "secondcat.tagid", "vsubcat", "你可能喜欢");

        private String facet;
        private String key;
        private String alias;
        private String target_facet_prefix;
        private String reason;
    }

    @Getter
    @AllArgsConstructor
    public enum RlvtEu {
        vneckvertebra_eu_rlvt("eurlvt_vexercise_body_ck", "1705", "exercise_body.tagid", "vsubcat", ""),//颈椎操
        vfitness_eu_rlvt("eurlvt_vsubcat_ck", "1007", "secondcat.tagid", "", ""), //减肥
        vhealth_eu_rlvt("eurlvt_vsubcat_ck", "1009", "secondcat.tagid", "", ""),
        ;

        private String facet;
        private String key;
        private String alias;
        private String target_facet_prefix;
        private String reason;
    }


}
