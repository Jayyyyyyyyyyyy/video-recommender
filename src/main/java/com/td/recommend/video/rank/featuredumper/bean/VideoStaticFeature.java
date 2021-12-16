package com.td.recommend.video.rank.featuredumper.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class VideoStaticFeature {
    Map<String, Integer> content_genre;
    Map<String, Integer> content_teach;
    Map<String, Integer>secondcat;
    Map<String, Integer> firstcat;
    Map<String, Integer> content_teacher;
    Map<String, String> content_mp3;
    List<Map<String, Integer>> content_tag;
    List<Map<String, Integer>> exercise_body;
    Integer uid;
    Integer duration;
    Long ctime;
    Integer title_len;
    Integer talentstar;
}

