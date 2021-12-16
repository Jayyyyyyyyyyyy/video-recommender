package com.td.recommend.video.retriever.explorer;

import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MultiArmedBandit {

    public static List<Arm> thompsonSampling(List<Arm> arms) {

        for (Arm arm : arms) {
            BetaDistribution betaDistribution = new BetaDistribution(arm.getWin(), arm.getLoose());
            double score = betaDistribution.sample();
            double variance = betaDistribution.getNumericalVariance();
            arm.setScore(score);
            arm.setVariance(variance);
        }

        return arms.stream()
                .sorted(Comparator.comparing(Arm::getScore).reversed())
                .collect(Collectors.toList());
    }

    public static void main(String[] argv) {
        BetaDistribution betaDistribution = new BetaDistribution(3,21);
        double score = betaDistribution.sample();
        double varicance = betaDistribution.getNumericalVariance();
        System.out.println(score+ " "+varicance);
    }
}
