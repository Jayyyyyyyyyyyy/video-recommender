package com.td.recommend.video.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {
    private static final Logger LOG = LoggerFactory.getLogger(TimeHelper.class);

    public static String dateFormat(String pattern){
        if (StringUtils.isBlank(pattern)){
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }

    public static int caculateTotalTime(String startTime, String endTime) {
        SimpleDateFormat formatter =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        Date date1=null;
        Date date = null;
        Long l = 0L;
        try {
            date = formatter.parse(startTime);
            long ts = date.getTime();
            date1 =  formatter.parse(endTime);
            long ts1 = date1.getTime();
            l = (ts - ts1) / (1000 * 60 * 60 * 24);
        } catch (Exception e) {
            LOG.error("video_age time parse error", e);
        }
        return l.intValue();
    }
}
