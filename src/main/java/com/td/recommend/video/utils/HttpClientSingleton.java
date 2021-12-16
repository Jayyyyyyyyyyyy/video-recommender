package com.td.recommend.video.utils;

import com.typesafe.config.Config;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

public class HttpClientSingleton {
    private static final Logger log = LoggerFactory.getLogger(HttpClientSingleton.class);
    public static HttpClientSingleton instance = new HttpClientSingleton();
    private RestTemplate restTemplate;

    public static HttpClientSingleton getInstance() {
        return instance;
    }

    public HttpClientSingleton() {
        Config httpServerConf = UserVideoConfig.getInstance().getRootConfig().getConfig("http-param");
        int maxperout = httpServerConf.getInt("http.maxperroute");
        int readtimeout = httpServerConf.getInt("http.readtimeout");
        int connesttimeout = httpServerConf.getInt("http.connecttimeout");

        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager();
        connMgr.setDefaultMaxPerRoute(maxperout);
        BasicHeader header = new BasicHeader(HttpHeaders.CONNECTION, "close");
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connMgr)
                .setDefaultHeaders(Collections.singleton(header))
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readtimeout);
        requestFactory.setConnectTimeout(connesttimeout);

        restTemplate = new RestTemplate(requestFactory);
    }

    public <T> T request(String url, Class<T> entity) {
        return restTemplate.getForObject(url, entity);

    }
}
