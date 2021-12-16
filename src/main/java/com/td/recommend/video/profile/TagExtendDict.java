package com.td.recommend.video.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Liujikun on 2019/09/04.
 */
public class TagExtendDict {
    private static final Logger LOG = LoggerFactory.getLogger(TagExtendDict.class);

    private static final Map<String, List<String>> tagExtendDict = new HashMap<>();

    static {
        String dictFileName = "tag-extend.txt";
        ClassLoader classLoader = TagExtendDict.class.getClassLoader();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(dictFileName), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(" ");
                if (fields.length >= 2) {
                    String tag = fields[0];
                    List<String> extTags = Arrays.asList(fields[1].split(","));
                    tagExtendDict.put(tag, extTags); }
            }
        } catch (IOException e) {
            LOG.error("Read tag extends from file={} failed!", dictFileName, e);
        }
    }

    public static List<String> get(String type, String key) {
        return tagExtendDict.getOrDefault(type + "_" + key, Collections.emptyList());
    }
}
