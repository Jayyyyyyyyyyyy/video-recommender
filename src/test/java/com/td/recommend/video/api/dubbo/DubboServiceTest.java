package com.td.recommend.video.api.dubbo;

import com.alibaba.fastjson.JSON;
import com.td.recommend.commons.request.RecommendRequest;
import com.td.recommend.video.api.VideoRecommend;
import org.apache.dubbo.config.*;

public class DubboServiceTest {
    public static void main(String[] args) {
        // 当前应用配置
        ApplicationConfig application = new ApplicationConfig();
        application.setName("videoRecommend");

        // 连接注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("nacos://10.10.111.129:8848");

        MetadataReportConfig metadataReport = new MetadataReportConfig();
        metadataReport.setAddress("nacos://10.10.111.129:8848");

        ConfigCenterConfig configCenter = new ConfigCenterConfig();
        configCenter.setAddress("nacos://10.10.111.129:8848");

        // 注意：ReferenceConfig为重对象，内部封装了与注册中心的连接，以及与服务提供方的连接

        // 引用远程服务
        ReferenceConfig<VideoRecommend> reference = new ReferenceConfig<>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        reference.setApplication(application);
        reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
        reference.setMetadataReportConfig(metadataReport);
        reference.setConfigCenter(configCenter);
        reference.setInterface(VideoRecommend.class);
        reference.setVersion("1.0.0");
        reference.setGroup("dubbo");
        reference.setTimeout(2000);
        reference.setRetries(0);
        reference.setLoadbalance("consistenthash");


        // 和本地bean一样使用xxxService
        VideoRecommend videoRecommend = reference.get(); // 注意：此代理对象内部封装了所有通讯细节，对象较重，请缓存复用
        RecommendRequest recommendRequest = new RecommendRequest();
        String diu = "5EA88918-E916-436C-819D";
        recommendRequest.setAlg("ftrl");
        recommendRequest.setAppId("t01");
        recommendRequest.setAppStore("realease");
        recommendRequest.setChannel("vivo");
        recommendRequest.setCid("70000");
        recommendRequest.setCity("宿州市");
        recommendRequest.setCityCode(0);
        recommendRequest.setClient("2");
        recommendRequest.setDeviceId("vivo_Y66L-Android:6.0.1");
        recommendRequest.setDiu(diu);
        recommendRequest.setHighEnd(false);
        recommendRequest.setIhf(1);
        recommendRequest.setImei("22ssdf33333333");
        recommendRequest.setLat(33.654027);
        recommendRequest.setLon(117.556342);
        recommendRequest.setManufacture("vivo");
        recommendRequest.setModel("vivo_Y66L");
        recommendRequest.setNetType("WIFI");
        recommendRequest.setNew_user(0);
        recommendRequest.setNewstype(-1);
        recommendRequest.setNid("0");
        recommendRequest.setNum(20);
        recommendRequest.setPageNo(1);
        recommendRequest.setProvince("安徽省");
        recommendRequest.setRuleType("empty");
        recommendRequest.setScreenHeight(1280);
        recommendRequest.setScreenWidth(720);
        recommendRequest.setSex(0);
        recommendRequest.setSsl(0);
        recommendRequest.setUid("4513406");
        recommendRequest.setVersion("6.9.0");
        recommendRequest.setVid("1500672982888");
        System.out.println(JSON.toJSONString(recommendRequest));

//        CRC32 crc32 = new CRC32();
//        crc32.update(recommendRequest.getDiu().getBytes());
//        long tagNum = crc32.getValue()%1;
//        RpcContext.getContext().setAttachment(Constants.TAG_KEY, "tag"+tagNum);

        String result = videoRecommend.recommend(diu, recommendRequest, false);
        System.err.println(result);
    }
}
