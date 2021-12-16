package com.td.recommend.video.abtest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2017/12/8.
 */
public class BucketConstants {
    public static final String MODEL_FTRL = "ftrl";
    public static final String MODEL_GBDT = "gbdt";
    public static final String MODEL_DNN = "model-dnn";
    public static final String MODEL_EXP = "model-exp";
    public static final String MODEL_BASE = "model-base";
    public static final String HIGHCTR_EXP = "highctr-exp";
    public static final String VHIGHCTR_EXP = "vhighctr-exp";

    public static final String NCE_EXP = "vnce-exp";

    public static final String DNNI2I_EXP = "vdnni2i-exp";

    public static final String VMFU2I_EXP = "vmfu2i-exp";

    public static final String VINTEREST_EXP = "vinterest-exp";
    public static final String VITEMCF_EXP = "vitemcf-exp";

    public static final String BOBO_EXP = "bobo-exp";

    public static String HIGHEND_EXCLUDE_EXP = "highendexclude-exp";
    public static String VUSERCF_EXP = "vusercf-exp";

    public static String VIDEO_DEGRADE_EXP = "videodegrade-exp";

    public static String PUBLISH_FIRSH_EXP = "publishfirst-exp";

    public static String HOTSEARCH_EXP = "hotsearch-exp";

    public static String MODEL_GBDT_LOCAL= "model-gbdtlocal";
    public static String MODEL_BACKUP= "model-backup";
    public static String MODEL_GBDT_NEW = "model-gbdtnew";

    public static String SCATTER_RATION_RULE_BASE = "scatterrule-base";
    public static String SCATTER_RATION_RULE_EXP2 = "scatterrule-exp2";
    public static String SCATTER_RATION_RULE_EXP4 = "scatterrule-exp4";


    public static String ST_RATION_RULE_EXP = "strule-exp";
    public static String EXT_RATION_RULE_EXP = "extrule-exp";

    public static String JPHIGHCTR_EXP = "jphighctr-exp";

    public static String POPULAR_EXP = "popular-exp";

    public static String INVERTSORT_EXP = "invert-exp";

    public static String RULETYPE_EXP = "newrule-exp";


    //命中业务ab实验
    public static final Set<String> abSet = ImmutableSet.<String>builder()
            .add("feed_add_module1-new1")
            .add("feed_add_module1-new2")
            .add("feed_add_module1-new3")
            .add("feed_add_module2-new")
            .add("feed_add_module3-new1")
            .add("feed_add_module3-new2")
            .add("feed_add_module3-new3")
            .build();


}
