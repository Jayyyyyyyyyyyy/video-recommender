package com.td.recommend.video.debug;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.td.recommend.commons.retriever.RetrieveKey;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class RerankDebugItem {
    private String id;
    private String title;
    private Integer initPos;
    private int pos;
    private double score;
    private double predictScore;
    private String hitRules;
    private String tags;
    private Integer lastHitRule;
    private Character status;
    private List<RetrieveKey> retrieveKeyList;
}
