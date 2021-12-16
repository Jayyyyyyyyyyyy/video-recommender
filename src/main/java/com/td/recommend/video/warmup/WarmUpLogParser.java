package com.td.recommend.video.warmup;

import java.util.Optional;

/**
 * Created by admin on 2017/8/18.
 */
public class WarmUpLogParser {
    public static Optional<String> parseURL(String logLine) {
        String[] fields = logLine.split(" ");
        if (fields.length < 7) {
            return Optional.empty();
        } else {
            return Optional.of(fields[6]);
        }
    }

    public static Optional<String> parseThriftURL(String logLine) {
        return Optional.of(logLine);
//        String[] fields = logLine.split(" ");
//        if (fields.length < 8) {
//            return Optional.empty();
//        } else {
//            return Optional.of(fields[7]);
//        }
    }
}
