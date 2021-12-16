package com.td.recommend.video.retriever.keybuilder.keyfeatures;

import com.td.recommend.commons.retriever.RetrieveKey;

import java.util.Map;

import static com.td.recommend.video.retriever.keybuilder.utils.RetrieveKeyHelper.getScore;

/**
 * Created by admin on 2017/7/15.
 */
public class TopicRetrieveKeyFeature {
    public String topic;

    public double topic_cs;
    public double topic_cs_poscnt;
    public double topic_cs_negcnt;

    public double vtopic_cs;
    public double vtopic_cs_poscnt;
    public double vtopic_cs_negcnt;

    public double topic_st_cs;
    public double topic_st_cs_poscnt;
    public double topic_st_cs_negcnt;

    public double topic_ck;
    public double topic_ck_poscnt;
    public double topic_ck_negcnt;

    public double vtopic_ck;
    public double vtopic_ck_poscnt;
    public double vtopic_ck_negcnt;

    public double topic_st_ck;
    public double topic_st_ck_poscnt;
    public double topic_st_ck_negcnt;

    public double rawctr;
    public double vrawctr;
    public double st_raw_ctr;

    public TopicRetrieveKeyFeature(RetrieveKey retrieveKey, String topicType, Map<String, Double> feature) {
        topic = retrieveKey.getKey();

        topic_cs = getScore(topicType + "_cs", feature);
        topic_cs_poscnt = getScore(topicType + "_cs_poscnt", feature);
        topic_cs_negcnt = getScore(topicType + "_cs_negcnt", feature);

        vtopic_cs = getScore(topicType + "_cs", feature);
        vtopic_cs_poscnt = getScore("v" + topicType + "_cs_poscnt", feature);
        vtopic_cs_negcnt = getScore("v" + topicType + "_cs_negcnt", feature);

        topic_st_cs = getScore("st_" + topicType + "_cs", feature);
        topic_st_cs_poscnt = getScore("st_" + topicType + "_cs_poscnt", feature);
        topic_st_cs_negcnt = getScore("st_" + topicType + "_cs_negcnt", feature);

        topic_ck = getScore(topicType + "_ck", feature);
        topic_ck_poscnt = getScore(topicType + "_ck_poscnt", feature);
        topic_ck_negcnt = getScore(topicType + "_ck_negcnt", feature);

        vtopic_ck = getScore("v" + topicType + "_ck", feature);
        vtopic_ck_poscnt = getScore("v" + topicType + "_ck_poscnt", feature);
        vtopic_ck_negcnt = getScore("v" + topicType + "_ck_negcnt", feature);

        topic_st_ck = getScore("st_" + topicType + "_ck", feature);
        topic_st_ck_negcnt = getScore("st_" + topicType + "_ck_negcnt", feature);
        topic_st_ck_poscnt = getScore("st_" + topicType + "_ck_poscnt", feature);

        rawctr = (0.14 + topic_ck_poscnt) / (topic_ck_negcnt + 1);
        vrawctr = (0.14 + vtopic_ck_poscnt) / (vtopic_ck_negcnt + 1);
        st_raw_ctr = (0.14 + topic_st_ck_poscnt) / (topic_st_ck_negcnt + 1);
    }
}
