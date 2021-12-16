package com.td.recommend.video.utils;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;

public class WilsonInterval {
    private static NormalDistribution normalDistribution = new NormalDistribution();

    public static double lowerBound(double positive, double total, double confidenceLevel) {
        if (positive <= 0) {
            return 0.0;
        }
        total = Math.max(positive, total);
        double alpha = (1.0D - confidenceLevel) / 2.0D;
        double z = normalDistribution.inverseCumulativeProbability(1.0D - alpha);
        double zSquared = FastMath.pow(z, 2);
        double mean = positive / total;
        double factor = 1.0D / (1.0D + 1.0D / total * zSquared);
        double modifiedSuccessRatio = mean + 1.0D / (2 * total) * zSquared;
        double difference = z * FastMath.sqrt(1.0D / total * mean * (1.0D - mean) + 1.0D / (4.0D * FastMath.pow(total, 2)) * zSquared);
        return factor * (modifiedSuccessRatio - difference);
    }

    public static void main(String[] args) {
        System.out.println(WilsonInterval.lowerBound(1, 7, 0.1));
    }
}