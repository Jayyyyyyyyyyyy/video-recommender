package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import java.util.Map;

import static com.td.recommend.video.retriever.keybuilder.utils.RetrieveKeyHelper.getScore;

/**
 * Created by admin on 2017/6/20.
 */
public class CatRetrieveKeyFeature {
    public String cat;
    public double cat_cs;
    public double cat_cs_negcnt;
    public double cat_cs_poscnt;

    public double vcat_cs;
    public double vcat_cs_negcnt;
    public double vcat_cs_poscnt;

    public double cat_st_cs;
    public double cat_st_cs_negcnt;
    public double cat_st_cs_poscnt;

    public double cat_ck;
    public double cat_ck_negcnt;
    public double cat_ck_poscnt;
    public double rawctr;

    public double vcat_ck;
    public double vcat_ck_negcnt;
    public double vcat_ck_poscnt;
    public double vrawctr;

    public double cat_st_ck;
    public double cat_st_ck_negcnt;
    public double cat_st_ck_poscnt;

    public double app_ct;
    public double app_ecat;

    public CatRetrieveKeyFeature(String cat, Map<String, Double> features) {
        this.cat = cat;
        cat_cs = getScore("cats_cs", features);
        cat_cs_negcnt = getScore("cats_cs_negcnt", features);
        cat_cs_poscnt = getScore("cats_cs_poscnt", features);

        vcat_cs = getScore("vcats_cs", features);
        vcat_cs_negcnt = getScore("vcats_cs_negcnt", features);
        vcat_cs_poscnt = getScore("vcats_cs_poscnt", features);

        cat_st_cs = getScore("st_ct_cs", features);
        cat_st_cs_negcnt = getScore("st_ct_cs_negcnt", features);
        cat_st_cs_poscnt = getScore("st_ct_cs_poscnt", features);


        cat_ck = getScore("cats_ck", features);
        cat_ck_negcnt = getScore("cats_ck_negcnt", features);
        cat_ck_poscnt = getScore("cats_ck_poscnt", features);

        vcat_ck = getScore("vcats_ck", features);
        vcat_ck_negcnt = getScore("vcats_ck_negcnt", features);
        vcat_ck_poscnt = getScore("vcats_ck_poscnt", features);

        cat_st_ck = getScore("st_ct_ck", features);
        cat_st_ck_negcnt = getScore("st_ct_ck_negcnt", features);
        cat_st_ck_poscnt = getScore("st_ct_ck_poscnt", features);

        app_ct = getScore("app_ct", features);
        app_ecat = getScore("app_ecat", features);

        rawctr = (0.10 + cat_ck_poscnt) / (cat_ck_negcnt + 1.0);
        vrawctr = (0.10 + vcat_ck_poscnt) / (vcat_ck_negcnt + 1.0);
    }
}
