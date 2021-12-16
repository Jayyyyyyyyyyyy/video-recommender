package com.td.recommend.video.rerank.model;

import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.Timer;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import com.td.rerank.dnn.felib.FeatureConfig;
import com.td.rerank.dnn.felib.FeatureConfigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

/**
 * @author zhanghongtao
 */
public class RerankDNNModel implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RerankDNNModel.class);
    private SavedModelBundle savedModelBundle;
    private Session session;

    public RerankDNNModel(String modelFile, String tags) {
        this.savedModelBundle = SavedModelBundle.load(modelFile, tags);
        this.session = this.savedModelBundle.session();
    }

    public float[][] predict(List<JSONObject> videos, JSONObject docIrrelevantIdFeatures, FeatureConfig featureConfig, int modelTaskNum, int rawVideoSize, List<Integer> posList, List<Integer> rankIndexList, List<Integer> clickRankIndexList) {
        if (videos == null || videos.isEmpty()){
            return new float[0][0];
        }
        int videoSize = videos.size();
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance().getTaggedMetricRegistry();
        Timer.Context createTensorTimer = taggedMetricRegistry.timer("video-recommend.rerank.dnn.createtensor.latency").time();
        Session.Runner runner = session.runner();
        JSONObject featureConfigObj = featureConfig.features();
        Map<String, Tensor> tensorMap = Maps.newHashMap();
        for (String index : featureConfigObj.keySet()) {
            FeatureConfigItem featureConfigItem = featureConfigObj.getObject(index, FeatureConfigItem.class);
            if (!featureConfigItem.isFedIntoModel()) {
                continue;
            }
            if (featureConfigItem.docRelevant() == 1) {
                for (int i = 0; i < videoSize; i++) {
                    String indKey = String.format("%s_%d_indice", index, i);
                    String valueIds = String.format("%s_%d_value_ids", index, i);
                    String valueWgt = String.format("%s_%d_value_wgt", index, i);
                    String shpKey = String.format("%s_%d_shape", index, i);

                    JSONObject feats;
                    JSONObject video = videos.get(i);
                    feats = video.getJSONObject(index);
                    creatTensor(tensorMap, featureConfigItem, indKey, valueIds, valueWgt, shpKey, feats);
                }
            } else {
                String indKey = String.format("%s_0_indice", index);
                String valueIds = String.format("%s_0_value_ids", index);
                String  valueWgt = String.format("%s_0_value_wgt", index);
                String shpKey = String.format("%s_0_shape", index);
                JSONObject feats = docIrrelevantIdFeatures.getJSONObject(index);
                if (feats == null || feats.isEmpty()){
                    LOG.error("docIrrelevantIdFeatures is null , index = {}", index);
                    return new float[0][0];
                }
                creatTensor(tensorMap, featureConfigItem, indKey, valueIds, valueWgt, shpKey, feats);
            }
        }
        if (tensorMap.size() > 0){
            tensorMap.forEach(runner::feed);
        }

        Tensor maskTensor = Tensor.create(rawVideoSize);
        runner.feed("mask", maskTensor);

        Tensor posTensor = Tensor.create(posList.stream().mapToInt(Integer::intValue).toArray());
        runner.feed("pos", posTensor);

        Tensor rankPosTensor = Tensor.create(rankIndexList.stream().mapToInt(Integer::intValue).toArray());
        runner.feed("rankPos", rankPosTensor);

//        Tensor clickRankPosTensor = Tensor.create(clickRankIndexList.stream().mapToInt(Integer::intValue).toArray());
//        runner.feed("clickRankPos", clickRankPosTensor);

        createTensorTimer.stop();
        Timer.Context predictTimer = taggedMetricRegistry.timer("video-recommend.rerank.dnn.predict.latency").time();
        List<Tensor<?>> outTensors = runner.fetch("prediction").run();
        float[][] scores = outTensors.get(0).copyTo(new float[videoSize][modelTaskNum]);
        predictTimer.stop();
        if (outTensors.size() > 0){
            LOG.info("outTensors size : {}", outTensors.size());
            outTensors.forEach(Tensor::close);
        }
        maskTensor.close();
        posTensor.close();
        rankPosTensor.close();
//        clickRankPosTensor.close();21
        if (tensorMap.size() > 0){
            tensorMap.forEach((x, y) -> {
                y.close();
            });
        }
        return scores;
    }

    private void creatTensor(Map<String, Tensor> tensorMap, FeatureConfigItem featureConfigItem, String indKey, String valueIds, String valueWgt, String shpKey, JSONObject feats) {
        List<Integer> indList = Lists.newArrayList();
        List<Integer> valueIdList = Lists.newArrayList();
        List<Float> valueWgtList = Lists.newArrayList();
        if (featureConfigItem.isFedIntoModel()) {
            int y = 0;
            for (String key : feats.keySet()) {
                indList.add(0);
                indList.add(y++);
                valueIdList.add(Integer.valueOf(key));
                if (featureConfigItem.isWeighted()) {
                    valueWgtList.add(feats.getFloat(key));
                }
            }
            tensorMap.put(indKey, Tensor.create(packFeaturesInteger(indList)));
            tensorMap.put(valueIds, Tensor.create(packFeaturesInteger(valueIdList)));
            tensorMap.put(shpKey, Tensor.create(new long[]{2}, IntBuffer.wrap(new int[]{1, feats.size()})));
            if (valueWgtList.size() > 0) {
                tensorMap.put(valueWgt, Tensor.create(packFeaturesDouble((valueWgtList))));
            }
        }
    }

    public void close(){
        this.savedModelBundle.close();
    }

    private byte[] packFeaturesInteger(List<Integer> features) {
        ByteBuffer buffer = ByteBuffer.allocate(features.size() * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (Integer x : features) {
            buffer.putInt(x);
        }
        return buffer.array();
    }

    private byte[] packFeaturesDouble(List<Float> features) {
        ByteBuffer buffer = ByteBuffer.allocate(features.size() * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (Float x : features) {
            buffer.putFloat(x);
        }
        return buffer.array();
    }
}
