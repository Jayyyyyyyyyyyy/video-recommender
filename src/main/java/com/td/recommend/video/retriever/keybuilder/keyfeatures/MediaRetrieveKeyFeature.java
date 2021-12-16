package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import java.util.Map;

import static com.td.recommend.video.retriever.keybuilder.utils.RetrieveKeyHelper.getScore;

/**
 * Created by admin on 2017/10/11.
 */
public class MediaRetrieveKeyFeature {
    public String media_name;
    public double media_name_cs;
    public double media_name_cs_negcnt;
    public double media_name_cs_poscnt;

    public double inner_source_cs;
    public double inner_source_cs_negcnt;
    public double inner_source_cs_poscnt;

    public double media_name_ck;
    public double media_name_ck_negcnt;
    public double media_name_ck_poscnt;

    public double inner_source_ck;
    public double inner_source_ck_negcnt;
    public double inner_source_ck_poscnt;


    public double media_name_rawctr;
    public double inner_source_rawctr;

    public MediaRetrieveKeyFeature(String mediaName, Map<String, Double> features) {
        this.media_name = mediaName;

        this.media_name_cs = getScore("vmedia_name_cs", features);
        this.media_name_cs_negcnt = getScore("vmedia_name_cs_negcnt", features);
        this.media_name_cs_poscnt = getScore("vmedia_name_cs_poscnt", features);

        this.media_name_ck = getScore("vmedia_name_ck", features);
        this.media_name_ck_negcnt = getScore("vmedia_name_ck_negcnt", features);
        this.media_name_ck_poscnt = getScore("vmedia_name_ck_poscnt", features);

        this.media_name_rawctr = (this.media_name_ck_poscnt + 0.14) / (this.media_name_ck_negcnt + 1);


        this.inner_source_cs = getScore("vinner_source_cs", features);
        this.inner_source_cs_negcnt = getScore("vinner_source_cs_negcnt", features);
        this.inner_source_cs_poscnt = getScore("vinner_source_ck_poscnt", features);

        this.inner_source_ck = getScore("vinner_source_ck", features);
        this.inner_source_ck_poscnt = getScore("vinner_source_ck_poscnt", features);
        this.inner_source_ck_negcnt = getScore("vinner_source_ck_negcnt", features);

        inner_source_rawctr = (this.inner_source_ck_poscnt + 0.14) / (this.inner_source_ck_negcnt + 1);
    }

}
