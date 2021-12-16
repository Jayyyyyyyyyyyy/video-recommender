package com.td.recommend.video.postprocessor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.td.data.profile.item.ItemDocumentData;
import com.td.data.profile.item.VideoItem;
import com.td.featurestore.item.ItemsProcessor;

import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.profile.TagUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class FastDiversifyProcessor implements ItemsProcessor<PredictItems<DocItem>> {

    private static final Logger logger = LoggerFactory.getLogger(FastDiversifyProcessor.class);

    private final EnumMap<DiversityFeatureType, Double> penaltyMap = new EnumMap<>(DiversityFeatureType.class);

    public FastDiversifyProcessor(double tagPenalty, double subCategoryPenalty, double categoryPenalty) {
        this.penaltyMap.put(DiversityFeatureType.Tag, tagPenalty);
        this.penaltyMap.put(DiversityFeatureType.SUBCATEGORY, subCategoryPenalty);
        this.penaltyMap.put(DiversityFeatureType.CATEGORY, categoryPenalty);
    }

    public FastDiversifyProcessor() {
        this(0.85, 0.9, 0.9);
    }

    @Override
    public PredictItems<DocItem> process(PredictItems<DocItem> items) {
        List<PredictItem<DocItem>> predictItemsList =  items.getItems();

        int expectedKeys = predictItemsList.size() * 20;
        int expectedValuesPerKey = 2;
        Multimap<DiversityFeatureTerm, ItemWithScore> termInvertedTable = HashMultimap.create(expectedKeys, expectedValuesPerKey);
        ItemWithScore[] heap = buildInvertedTableAndHeap(predictItemsList, termInvertedTable);

        List<ItemWithScore> adjustedHeap = penalize(termInvertedTable, heap);
        PredictItems<DocItem> result = new PredictItems<>();
        adjustedHeap.forEach(itemWithScore -> {
            PredictItem<DocItem> item = itemWithScore.item;
            item.setScore(itemWithScore.getScore());
            result.add(item);
        });

        result.sort();
        return result;
    }

    private List<ItemWithScore> penalize(Multimap<DiversityFeatureTerm, ItemWithScore> termInvertedTable, ItemWithScore[] heap) {
        List<ItemWithScore> chosen = Lists.newArrayListWithCapacity(heap.length - 1);
        int lastIndex = heap.length - 1;

        while (lastIndex > 0) {
            ItemWithScore bestChoice = heap[1];
            chosen.add(bestChoice);
            heap[1] = heap[lastIndex--];
            heap[1].setPosInHeap(1);
            adjustDown(heap, 1, lastIndex);

            penalizeByFeature(termInvertedTable, heap, bestChoice, lastIndex);
        }

        return chosen;
    }

    private void penalizeByFeature(Multimap<DiversityFeatureTerm, ItemWithScore> termInvertedTable, ItemWithScore[] heap,
                                   ItemWithScore bestChoice, int lastIndex) {

        Set<String> penalizedItems = new HashSet<>();
        for (DiversityFeatureTerm term : bestChoice.termPairs) {
            termInvertedTable.remove(term, bestChoice);
            for (ItemWithScore itemToBePenalize : termInvertedTable.get(term)) {
                if(penalizedItems.contains(itemToBePenalize.item.getId()))
                    continue;

                penalizedItems.add(itemToBePenalize.item.getId());

                double score = itemToBePenalize.getScore();
                double penalty = penaltyMap.get(term.type);
                double penalizedScore = score * penalty;
                itemToBePenalize.setScore(penalizedScore);
                adjustDown(heap, itemToBePenalize.getPosInHeap(), lastIndex);
            }
        }
    }

    private static void adjustDown(ItemWithScore[] heap, int posNow, int posLast) {
        int posChildLeft = posNow * 2;
        while (posChildLeft <= posLast) {
            int posChild = posChildLeft < posLast ?
                    (heap[posChildLeft].getScore() < heap[posChildLeft+1].getScore() ? posChildLeft+1 : posChildLeft) : posChildLeft;
            if (heap[posChild].getScore() <= heap[posNow].getScore()) {
                break;
            }

            ItemWithScore temp = heap[posNow];
            heap[posNow] = heap[posChild];
            heap[posChild] = temp;

            heap[posNow].setPosInHeap(posNow);
            heap[posChild].setPosInHeap(posChild);

            posNow = posChild;
            posChildLeft = posNow * 2;
        }
    }

    private ItemWithScore[] buildInvertedTableAndHeap(List<PredictItem<DocItem>> items, Multimap<DiversityFeatureTerm, ItemWithScore> termInvertedTable) {

        ItemWithScore[] heap = new ItemWithScore[items.size() + 1];
        heap[0] = ItemWithScore.SENTINEL;
        int heapIndex = 1;
        for (PredictItem<DocItem> item : items) {

            Optional<ItemDocumentData> ItemDocumentDataOpt = item.getItem().getNewsDocumentData();

            ItemWithScore itemWithScore = new ItemWithScore(item);
            heap[heapIndex++] = itemWithScore;

            if (!ItemDocumentDataOpt.isPresent()) {
                continue;
            }

            ItemDocumentData ItemDocumentData = ItemDocumentDataOpt.get();
            Optional<VideoItem> staticDocumentDataOpt = ItemDocumentData.getStaticDocumentData();
            if (!staticDocumentDataOpt.isPresent()) {
                continue;
            }

            VideoItem newsDocument = staticDocumentDataOpt.get();


            //Map<String, Double> tagsMap = newsDocument.getTagsMap();
            Map<String, Double> tagsMap = new HashMap<>();
            if (tagsMap != null && !tagsMap.isEmpty()) {


                List<String> penaltySet = getPenaltyFeatures(tagsMap);
                for (String penalty : penaltySet) {
                    DiversityFeatureTerm term = new DiversityFeatureTerm(penalty, DiversityFeatureType.Tag);
                    termInvertedTable.put(term, itemWithScore);
                    itemWithScore.addTerm(term);
                }
            }

            //Map<String, Double> catsMap = newsDocument.getCatsMap();
            Map<String, Double> catsMap = new HashMap<>();
            if (catsMap != null && !catsMap.isEmpty()) {
                String penalty = getPenaltyFeature(catsMap);
                DiversityFeatureTerm term = new DiversityFeatureTerm(penalty, DiversityFeatureType.CATEGORY);
                termInvertedTable.put(term, itemWithScore);
                itemWithScore.addTerm(term);
            }

            //Map<String, Double> secondCatsMap = newsDocument.getSecondCatsMap();
            Map<String, Double> secondCatsMap = new HashMap<>();

            if (secondCatsMap != null && !secondCatsMap.isEmpty()) {
                String penaltyFeature = getPenaltyFeature(secondCatsMap);
                DiversityFeatureTerm term = new DiversityFeatureTerm(penaltyFeature, DiversityFeatureType.SUBCATEGORY);
                termInvertedTable.put(term, itemWithScore);
                itemWithScore.addTerm(term);
            }
        }

        Arrays.sort(heap, 1, heap.length);
        for (int i = 1; i < heap.length; ++i) {
            heap[i].setPosInHeap(i);
        }
        return heap;
    }

    public String getPenaltyFeature(Map<String, Double> featureMap) {
        double max = Double.NEGATIVE_INFINITY;
        String feature = null;

        for (Map.Entry<String, Double> entry : featureMap.entrySet()) {
            if (max < entry.getValue()) {
                max = entry.getValue();
                feature = entry.getKey();
            }
        }

        return feature;
    }

    public List<String> getPenaltyFeatures(Map<String, Double> featureMap) {
        Set<String> seccatTags = TagUtils.getSeccatTags();
        return featureMap.entrySet().stream()
                .filter(entry -> !seccatTags.contains(entry.getKey()))
                .sorted((o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()))
                .map(entry -> entry.getKey())
                .limit(3)
                .collect(Collectors.toList());
    }

    private enum DiversityFeatureType {
        CATEGORY, Tag, SUBCATEGORY
    }

    private static class ItemWithScore implements Comparable<ItemWithScore> {
        public static final ItemWithScore SENTINEL = new ItemWithScore(null, Double.POSITIVE_INFINITY);
        protected final PredictItem<DocItem> item;
        private double score;
        private List<DiversityFeatureTerm> termPairs;
        private int posInHeap;

        public ItemWithScore(PredictItem<DocItem> item) {
            this(item, item.getScore());
        }

        public ItemWithScore(PredictItem<DocItem> item, double score) {
            this.item = item;
            this.score = score;
            this.termPairs = Lists.newArrayList();
        }

        public void addTerm(DiversityFeatureTerm term) {
            termPairs.add(term);
        }

        public void setPosInHeap(int posInHeap) {
            this.posInHeap = posInHeap;
        }

        public int getPosInHeap() {
            return posInHeap;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public double getScore() {
            return score;
        }

        @Override public int compareTo(ItemWithScore that) {
            return Double.compare(that.score, score);
        }

        @Override public String toString() {
            if(item == null)
                return "";
            String listStr = StringUtils.join(termPairs, "|");
            return "id:" + item.getId() + ", score:" + score + ", termPairs:" + listStr + ", posInHeap: " + posInHeap;
        }
    }

    private static class DiversityFeatureTerm {
        public final String name;
        public final DiversityFeatureType type;

        public DiversityFeatureTerm(String name, DiversityFeatureType type) {
            this.name = name;
            this.type = type;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            DiversityFeatureTerm that = (DiversityFeatureTerm) o;
            return Objects.equals(name, that.name) && type == that.type;
        }

        @Override public int hashCode() {
            return Objects.hash(name, type);
        }

        @Override public String toString() {
            return name + "::" + type;
        }
    }

//    public static void main(String[] args) {
//        Arrays.asList(1,2,3,4, 0).stream().sorted(Integer::compare).map(i -> i * i).limit(2).collect(Collectors.toList()).forEach(System.out::println);
//    }
}
