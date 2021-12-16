package com.td.recommend.video.api.vo;

/**
 * Created by admin on 2018/1/3.
 */
public class VideoInfoBuilder {
//  private static final Logger LOG = LoggerFactory.getLogger(VideoInfoBuilder.class);
//
//  private final static String START_FORMAT = "";
//  private final static String END_FORMAT = "";
//
//  public static Optional<VideoInfo> build(String deviceId, VideoItem staticDocumentData, TaggedItem<PredictItem<DocItem>> taggedItem) {
//    try {
////            PredictItem<DocItem> predictItem = taggedItem.getItem();
//
//      VideoInfo videoInfo = new VideoInfo();
//      videoInfo.setDura(staticDocumentData.getVideoTime() * 1000);
//      videoInfo.setSrc(staticDocumentData.getUrlLocal());
//      videoInfo.setPublisher(staticDocumentData.getPublisher());
//      videoInfo.setSize(staticDocumentData.getVideosize());
//      String playCount = getFakePlayCount(staticDocumentData);
//
//      videoInfo.setPlayCnt(playCount);
////            videoInfo.setStart(Arrays.asList(getUrl(START_FORMAT, deviceId, predictItem.getId(), predictItem.getPredictId())));
////            videoInfo.setEnd(Arrays.asList(getUrl(END_FORMAT, deviceId, predictItem.getId(), predictItem.getPredictId())));
//
//      return Optional.of(videoInfo);
//    } catch (Exception e) {
//      LOG.error("build video info failed!", e);
//      return Optional.empty();
//    }
//  }
//
//  private static String getFakePlayCount(VideoItem staticDocumentData) {
//    int playCnt = staticDocumentData.getPlaycnt();
//
//    playCnt += 10000 + Math.round(10000);
//
//    return String.format("%.2f万", playCnt / 10000.0);
//  }
//
//  public static String getUrl(String format, String deviceId, String docId, String predictId) {
//    return String.format(format, deviceId, docId, predictId);
//  }
}
