package com.td.recommend.video.api.thrift;

//import com.td.recommend.thrift.RecommendClient;
//import com.td.recommend.thrift.api.RecommendItem;
//import com.td.recommend.thrift.api.RecommendRequest;
//import com.td.recommend.thrift.api.RecommendResponse;

/**
 * Created by admin on 2017/12/28.
 */
public class VideoRecommendServerTest {
//    private RecommendClient recommendClient;
//    private UserItem userItem;
//    @Before
//    public void setUp() {
//        Config config = mock(Config.class);
//        when(config.getInt("client-pool.maxTotal")).thenReturn(10);
//        when(config.getInt("client-pool.maxIdle")).thenReturn(10);
//        when(config.getInt("client-pool.minIdle")).thenReturn(4);
//        when(config.getString("server.host")).thenReturn("10.19.121.73");
//        when(config.getInt("server.port")).thenReturn(8090);
//        when(config.getInt("recommend.timeout.ms")).thenReturn(5000);
//
//        recommendClient = new RecommendClient(config);
//
//        UserItemDao userItemDao = UserVideoItemDataSource.getInstance().getUserItemDao();
//        Optional<UserItem> userItemOpt = userItemDao.get("ed412df5cc3d17f7");
//        userItem = userItemOpt.get();
//    }
//
////    @Test
//    public void testRequest() {
//        String query = "screenHeight=1280&appId=A0008&serialId=2e5fccae04144ccb8803495c4f9553c7&chanId=xiaomi&ssl=1&lng=113.471776&screenDensity=2.0&bSsid=6c:59:40:6f:0b:82&ssid=MERCURY_0B82&coordType=b&deviceVersion=HM NOTE 1LTE&verCode=3131&uhid=a0000000000000000000000000000001&lat=30.672877&deviceVendor=Xiaomi&limit=6&template=100_101_102_103_104_105_106_107_108_109_110_111_112_113_114_115_116&os=android&osVersion=4.4.4&netModel=w&imei=866445026634329&sign=f3d44952db86d95aa95021d9ae84efee&screenWidth=720&pageNo=0&verName=4.2.11&channel=1&openId=vjtstkunosi82f0fcab8755825868e2f&alg=ftrl&deviceId=ed412df5cc3d17f7&highEnd=true";
//        Map<String, String> requestParam = Stream.of(query.split("&")).map(q -> q.split("=")).collect(Collectors.toMap(q_arr -> q_arr[0], q_arr -> q_arr[1]));
//
//        Optional<UserRawData> userRawDataOpt = userItem.getUserRawData();
//        UserRawData userRawData = userRawDataOpt.get();
//
//        TUserItem userItem = new TUserItem(userRawData.getTL2EntryMap(), false, false);
//        RecommendRequest recommendRequest = new RecommendRequest(requestParam, userItem);
//        Optional<RecommendResponse> responseOpt = recommendClient.recommend(recommendRequest);
//        if (responseOpt.isPresent()) {
//            RecommendResponse response = responseOpt.get();
//            List<RecommendItem> recommendItems = response.getRecommendItems();
//            for (RecommendItem recommendItem : recommendItems) {
//                System.out.printf("%s\t%s\n", recommendItem.getId(), recommendItem.getTags().stream().map(tag -> tag.getId() + "\t" + tag.getName()).collect(Collectors.joining(",")));
//            }
////            System.out.println(response.getRecommendItems().size());
//        }
//    }
}