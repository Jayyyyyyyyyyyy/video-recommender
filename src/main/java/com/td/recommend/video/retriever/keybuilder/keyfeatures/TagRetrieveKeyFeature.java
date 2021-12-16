package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import java.util.Map;

import static com.td.recommend.video.retriever.keybuilder.utils.RetrieveKeyHelper.getScore;

/**
 * Created by admin on 2017/6/20.
 */
public class TagRetrieveKeyFeature {
    public String tag;

    public double tag_cs;
    public double tag_cs_negcnt;
    public double tag_cs_poscnt;

    public double vtag_cs;
    public double vtag_cs_negcnt;
    public double vtag_cs_poscnt;

    public double tag_st_cs;
    public double tag_st_cs_negcnt;
    public double tag_st_cs_poscnt;

    public double tag_ck;
    public double tag_ck_negcnt;
    public double tag_ck_poscnt;

    public double vtag_ck;
    public double vtag_ck_negcnt;
    public double vtag_ck_poscnt;

    public double tag_st_ck;
    public double tag_st_ck_negcnt;
    public double tag_st_ck_poscnt;

    public double app_etag;
    public double vrawctr;
    public double rawctr;


    public TagRetrieveKeyFeature (String tag, Map<String, Double> features) {
        this.tag = tag;
        tag_cs = getScore("tags_cs", features);
        tag_cs_negcnt = getScore("tags_cs_negcnt", features);
        tag_cs_poscnt = getScore("tags_cs_poscnt", features);
        vtag_cs = getScore("vtags_cs", features);
        vtag_cs_negcnt = getScore("vtags_cs_negcnt", features);
        vtag_cs_poscnt = getScore("vtags_cs_poscnt", features);

        tag_st_cs = getScore("st_tags_cs", features);
        tag_st_cs_negcnt = getScore("st_tags_cs_negcnt", features);
        tag_st_cs_poscnt = getScore("st_tags_cs_poscnt", features);

        this.tag_ck = getScore("tags_ck", features);
        this.tag_ck_negcnt = getScore("tags_ck_negcnt", features);
        this.tag_ck_poscnt = getScore("tags_ck_poscnt", features);

        this.vtag_ck = getScore("vtags_ck", features);
        this.vtag_ck_negcnt = getScore("vtags_ck_negcnt", features);
        this.vtag_ck_poscnt = getScore("vtags_ck_poscnt", features);

        tag_st_ck = getScore("st_tags_ck", features);
        tag_st_ck_negcnt = getScore("st_tags_ck_negcnt", features);
        tag_st_ck_poscnt = getScore("st_tags_ck_poscnt", features);

        app_etag = getScore("app_etag", features);

        this.rawctr = (0.14 + tag_ck_poscnt) / (tag_ck_negcnt + 1.0);
        this.vrawctr = (0.14 + vtag_ck_poscnt) / (vtag_ck_negcnt + 1.0);
    }

}
