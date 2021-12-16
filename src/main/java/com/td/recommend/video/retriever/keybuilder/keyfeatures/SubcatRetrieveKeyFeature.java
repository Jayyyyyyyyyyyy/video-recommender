package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import java.util.Map;

import static com.td.recommend.video.retriever.keybuilder.utils.RetrieveKeyHelper.getScore;

/**
 * Created by admin on 2017/6/20.
 */
public class SubcatRetrieveKeyFeature {
    public String subcat;

    public double subcat_cs;
    public double subcat_cs_negcnt;
    public double subcat_cs_poscnt;

    public double vsubcat_cs;
    public double vsubcat_cs_negcnt;
    public double vsubcat_cs_poscnt;

    public double subcat_st_cs;
    public double subcat_st_cs_negcnt;
    public double subcat_st_cs_poscnt;

    public double subcat_ck;
    public double subcat_ck_negcnt;
    public double subcat_ck_poscnt;

    public double vsubcat_ck;
    public double vsubcat_ck_negcnt;
    public double vsubcat_ck_poscnt;

    public double subcat_st_ck;
    public double subcat_st_ck_negcnt;
    public double subcat_st_ck_poscnt;

    public double app_esubcat;
    public double rawctr;
    public double vrawctr;
    public double st_raw_ctr;

    public SubcatRetrieveKeyFeature(String subcat, Map<String, Double> features) {
        this.subcat = subcat;
        subcat_cs = getScore("subcats_cs", features);
        subcat_cs_negcnt = getScore("subcats_cs_negcnt", features);
        subcat_cs_poscnt = getScore("subcats_cs_poscnt", features);

        vsubcat_cs = getScore("vsubcats_cs", features);
        vsubcat_cs_negcnt = getScore("vsubcats_cs_negcnt", features);
        vsubcat_cs_poscnt = getScore("vsubcats_cs_poscnt", features);


        subcat_st_cs = getScore("st_sct_cs", features);
        subcat_st_cs_negcnt = getScore("st_sct_cs_negcnt", features);
        subcat_st_cs_poscnt = getScore("st_sct_cs_poscnt", features);

        subcat_ck = getScore("subcats_ck", features);
        subcat_ck_negcnt = getScore("subcats_ck_negcnt", features);
        subcat_ck_poscnt = getScore("subcats_ck_poscnt", features);

        vsubcat_ck = getScore("subcats_ck", features);
        vsubcat_ck_negcnt = getScore("subcats_ck_negcnt", features);
        vsubcat_ck_poscnt = getScore("subcats_ck_poscnt", features);


        subcat_st_ck = getScore("st_sct_ck", features);
        subcat_st_ck_negcnt = getScore("st_sct_ck_negcnt", features);
        subcat_st_ck_poscnt = getScore("st_sct_ck_poscnt", features);

        app_esubcat = getScore("app_esubcat", features);
        rawctr = (0.14 + subcat_ck_poscnt) / (subcat_ck_negcnt + 1.0);
        vrawctr = (0.14 + vsubcat_ck_poscnt) / (vsubcat_ck_negcnt + 1.0);

        st_raw_ctr = (0.14 + subcat_st_ck_poscnt) / (subcat_st_ck_negcnt + 1.0);

    }
}
