package com.td.recommend.video.debug;

import com.td.data.profile.item.VideoItem;
import com.td.recommend.docstore.dao.DocItemDao;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.utils.RedisClientSingleton;
import com.td.recommend.video.utils.TrendUidInfo;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;


public class mydebug {

    public static void main(String[] args){

        //String trendvid = "20000000092206";
        String trendvid = "234sss";
        Optional<DocItem> docItemDao = new DocItemDao().get(trendvid);
        if(docItemDao.isPresent()){
            System.out.println("exist");
            DocItem docItem = docItemDao.get();
            if(docItem.getNewsDocumentData().isPresent()){
                System.out.println(docItem.getNewsDocumentData().get());

                Optional<VideoItem> videoItem = docItem.getNewsDocumentData().get().getStaticDocumentData();
                if(Optional.ofNullable(videoItem).isPresent()){
                    String statisVid = docItem.getNewsDocumentData().get().getStaticDocumentData().get().getId();
                    System.out.println("exist 123:"+ statisVid);
                }
                else{
                    System.out.println(" not exist 123: "+ trendvid);
                }
//                ;
//                if(docItem.getNewsDocumentData().get().getStaticDocumentData().isPresent()){
//                    String statisVid = docItem.getNewsDocumentData().get().getStaticDocumentData().get().getId();
//                    System.out.println("exist 123:"+ statisVid);
//                }
//                else{
//                    System.out.println(" not exist 123: "+ trendvid);
//                }


            }
            else{
                System.out.println("no exist 123");
            }
            System.out.println(docItemDao.get().getNewsDocumentData());
        }
        else{
            System.out.println("no exist");
        }
        String city = "襄阳市";
        System.out.println(city.length());
        System.out.println("123");
        HashMap<String, List<String>> mymap = new HashMap<>();
        mymap.put("xxx", null);
        mymap.put("tfollow", Arrays.asList("1","4","3","4","8", "9", "10"));
        mymap.put("vxfollow", Arrays.asList("2","6","8","1","19","14"));
        mymap.put("vhot", Arrays.asList("3","7", "9","41","7","0","2"));

        mymap.put("yyy", Arrays.asList());

        Map<String,Integer> filterTypes = new HashMap<String,Integer>();
        filterTypes.put("tfollow", 3);
        filterTypes.put("vxfollow", 2);
        filterTypes.put("vhot", 1);

        List<String> result = new ArrayList<String>();
        filterTypes.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).forEachOrdered(

                entry ->{
                    if(mymap.containsKey(entry.getKey()) && result.size()==0){
                        result.addAll(mymap.get(entry.getKey()).subList(0,Math.min(mymap.get(entry.getKey()).size(), 5)));
                        //System.out.println(mymap.get(entry.getKey()).subList(0,Math.min(mymap.get(entry.getKey()).size(), 5)));
                    }
                }
        );
        System.out.println(result.toString());

        System.out.println("debug2");

        Map<String, List<String>> uid2vidlist = new LinkedHashMap<>();
        Map<String, Map<String, List<String>>> type2uid2vidlist = new HashMap<>();
        String s1 = "sun jian";
        String s2 = "hu yuan chun";
        String s3 = "sun hao yang";
        String s4 = "sun hao chen";

        String s5 = "liu yin xuan";
        String s6 = "bian qian qian";
        String s7 = "liu mu xi";

        List<String> list1 = new ArrayList<>(Arrays.asList(s1.split(" ")));

        List<String> list2 = new ArrayList<>(Arrays.asList(s2.split(" ")));

        List<String> list3 = new ArrayList<>(Arrays.asList(s3.split(" ")));

        List<String> list4 = new ArrayList<>(Arrays.asList(s4.split(" ")));

        List<String> list5 = new ArrayList<>(Arrays.asList(s5.split(" ")));

        List<String> list6 = new ArrayList<>(Arrays.asList(s6.split(" ")));

        List<String> list7 = new ArrayList<>(Arrays.asList(s7.split(" ")));


        uid2vidlist.computeIfAbsent("daren", k-> new ArrayList<>()).addAll(list1);
        System.out.println(uid2vidlist);
        uid2vidlist.computeIfAbsent("daren", k-> new ArrayList<>()).addAll(list2);
        //uid2vidlist.computeIfAbsent("xiaohai", k-> new ArrayList<>()).addAll(list3);
        //uid2vidlist.computeIfAbsent("xiaohai", k-> new ArrayList<>(list3)).addAll(list4);

        System.out.println(uid2vidlist);
//
        type2uid2vidlist.computeIfAbsent("A", k-> new LinkedHashMap<>()).computeIfAbsent("daren",k-> new ArrayList<>(list1)).addAll(list2);
        System.out.println(type2uid2vidlist);
        type2uid2vidlist.computeIfAbsent("A", k-> new LinkedHashMap<>()).computeIfAbsent("xiaohai",k-> new ArrayList<>(list3)).addAll(list4);
        type2uid2vidlist.computeIfAbsent("B", k-> new LinkedHashMap<>()).computeIfAbsent("daren",k-> new ArrayList<>(list5)).addAll(list6);
        type2uid2vidlist.computeIfAbsent("B", k-> new LinkedHashMap<>()).computeIfAbsent("xiaohai",k-> new ArrayList<>(list7));
        System.out.println(type2uid2vidlist);
//
//        type2uid2vidlist.forEach((key, value) -> {
//            System.out.println(key);
//            System.out.println(value);
//
//        });
        System.out.println("debug3");

        Map<String, String> featureMap_diu = Collections.emptyMap();
        List<String> valuelist = featureMap_diu.entrySet().stream().map(key -> key.getValue()).collect(Collectors.toList());
        if(valuelist.isEmpty()){
            System.out.println("valuelist empty");
        }
        System.out.println(valuelist.size());


        Map<String, List<String>> cfmap = new HashMap<>();
        cfmap.computeIfAbsent("daren", k->new ArrayList<>()).addAll(list1);
        cfmap.computeIfAbsent("daren", k->new ArrayList<>()).addAll(list2);
        cfmap.computeIfAbsent("xiaohai", k->new ArrayList<>()).addAll(list3);
        cfmap.computeIfAbsent("xiaohai", k->new ArrayList<>()).addAll(list4);

        System.out.println(cfmap.values());
        Map<String, Long> sameMap = cfmap.values().stream().
                flatMap(Collection::stream).collect(Collectors.groupingBy(x-> x, Collectors.counting()));

        System.out.println(sameMap);

        Map<String, Long> countGrouped = cfmap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.groupingBy(x-> x, Collectors.counting()));
        System.out.println(countGrouped);

        Map<String, Long> descendingSorted= countGrouped.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a,b) -> { throw new AssertionError(); }, LinkedHashMap::new));

        System.out.println(descendingSorted);

        Map<Long, List<String>> groups = countGrouped.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.groupingBy(Map.Entry::getValue, LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())));


        System.out.println("grouped " + groups);


        ArrayList<String> uidlist = new ArrayList<>();
        //uidlist.add("123");

        for(String cuid: uidlist.subList(0,Math.min(5,uidlist.size()))){
            System.out.println("cuid:"+cuid);
        }
        System.out.println("end");


        RedisClientSingleton instance = RedisClientSingleton.boost;
        List<String> res = instance.lrange("sb",0,-1);
        if(res!=null){
            System.out.println(res.size());
            System.out.println(res.isEmpty());
        }

        //LinkedHashMap<Integer, List<String>> ageMap = personsSort.stream().sorted(Comparator.comparingInt(Person::getAge)).collect(Collectors.groupingBy(String::getAge, LinkedHashMap::new, Collectors.toList()));



    }
}
