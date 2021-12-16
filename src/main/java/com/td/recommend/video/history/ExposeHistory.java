package com.td.recommend.video.history;

import com.td.recommend.FilterInviewServer;
import com.td.recommend.FilterServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by admin on 2017/6/22.
 */
public class ExposeHistory {
    private static final Logger LOG = LoggerFactory.getLogger(ExposeHistory.class);

    public static ExposeHistory instance = new ExposeHistory();

    public static ExposeHistory getInstance() {
        return instance;
    }

    private final FilterServer filterServer=new FilterServer();
    private final FilterInviewServer filterInviewServer = new FilterInviewServer();
    public ExposeHistory() {
    }

    public Set<String> get(String appId, String userId) {
        return new HashSet<>(getDays(appId, userId));
    }

    public List<String> getOneDay(String appId, String userId) {
        List<String> exposeList = filterServer.getOneDayFilterListOrderNew(appId, userId);
        if (userId.startsWith("debug_")) {
            String realUserId = userId.replaceFirst("debug_", "");
            List<String> realExposeList = filterServer.getOneDayFilterListOrderNew(appId, realUserId);
            exposeList.addAll(realExposeList);
        }
        return exposeList;
    }

    public List<String> getDays(String appId, String userId) {
        List<String> exposeList = filterServer.getFilterListOrderNew(appId, userId);
        if (userId.startsWith("debug_")) {
            String realUserId = userId.replaceFirst("debug_", "");
            List<String> realExposeList = filterServer.getFilterListOrderNew(appId, realUserId);
            exposeList.addAll(realExposeList);
        }
        return exposeList;
    }
    public List<String> getExposes(String appId, String userId) {
        List<String> exposeList = filterInviewServer.getFilterListOrderNew(appId, userId);
        if (userId.startsWith("debug_")) {
            String realUserId = userId.replaceFirst("debug_", "");
            List<String> realExposeList = filterInviewServer.getFilterListOrderNew(appId, realUserId);
            exposeList.addAll(realExposeList);
        }
        return exposeList;
    }
    public void removeHistory(String appId, String userId) throws Exception {
        filterServer.clearByUserId(appId, userId);
    }
    public static void main(String[] args) {
        //System.out.println("ljk" + ExposeHistory.getInstance().getExposes("t01", "6AA72E12-EC5D-46FF-8DA3-1B11FC756201"));

        //System.out.println("ljk" + ExposeHistory.getInstance().getExposes("t01", "6304c0101ec2e8f4"));
        //tongsy apple
        //List<String> idlist = ExposeHistory.getInstance().getDays("t01","9765945C-F775-4C71-AF2D-B226201955F3");
        //tongsy android
        //List<String> idlist = ExposeHistory.getInstance().getExposes("t01","860156042698794");
        // sunjian android  860156042698794
        try{
            ExposeHistory.getInstance().removeHistory("t01","66916496a54c8bc5");
        }catch (Exception e){
            System.out.print(e);
        }

        List<String> idlist = ExposeHistory.getInstance().getDays("t01","66916496a54c8bc5");

        idlist.forEach(
                id->{
                    System.out.println(id);
                }
        );
        System.out.println("size:"+idlist.size());
    }
}
