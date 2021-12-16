package com.td.recommend.video.utils;

import com.typesafe.config.Config;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by admin on 2018/1/19.
 */
public class Test {

    public static void main(String[] args) {
        Config userNewsConfig = UserVideoConfig.getInstance().getRootConfig();
        Config retrieverConfig = userNewsConfig.getConfig("retriever-engine");

        List<? extends Config> retrievers = retrieverConfig.getConfigList("retrievers");

        retrievers.stream().map(retriever -> {
            String name = retriever.getString("name");
            Config retrieveConfig = retriever.getConfig("config");
            Config transporterConfig = retrieveConfig.getConfig("transporter-config");
            String hosts = transporterConfig.getString("hosts");
            return Pair.of(name, hosts);
        }).collect(Collectors.groupingBy(Pair::getRight)).forEach((host, pairs) -> {
            String retrieveNames = pairs.stream().map(p -> p.getLeft()).collect(Collectors.joining("/"));
            System.out.printf("%s\t%s\n", host, retrieveNames);
        });
    }
}
