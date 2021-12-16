package com.td.recommend.video.retriever.explorer;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Arm {
    private String name;
    private double win;
    private double loose;
    private double score;
    private double variance;

    public Arm(String name, double win, double loose) {
        this.name = name;
        this.win = win;
        this.loose = loose;
    }
}