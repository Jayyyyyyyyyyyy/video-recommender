package com.td.recommend.video.profile;

import com.typesafe.config.Config;
import com.td.recommend.video.utils.UserVideoConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by admin on 2017/9/18.
 */
public class TagUtils {
    private static Set<String> seccatTags = new HashSet<>();

    static {
        Config userNewsConfig = UserVideoConfig.getInstance().getAppConfig();
        List<String> tempTags = userNewsConfig.getStringList("seccat-tags");
        seccatTags.addAll(tempTags);
    }

    public static Set<String> getSeccatTags() {
        return seccatTags;
    }

    public static void main(String[] args) {
        System.out.println(getSeccatTags());
    }
}
