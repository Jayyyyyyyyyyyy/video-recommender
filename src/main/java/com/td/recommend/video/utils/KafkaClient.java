package com.td.recommend.video.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class KafkaClient {
    private static final Logger logger = LoggerFactory.getLogger(KafkaClient.class);
    private static volatile KafkaClient instance;
    private volatile boolean isInit = false;
    private int num = 4;
    private KafkaProducer<String, String>[] kafkaProd = new KafkaProducer[num];
    private KafkaClient() {

    }

    public void Init() {
        Properties props = new Properties();
//		UserNewsConfig userNewsConfig = UserNewsConfig.getInstance();
//		Config rootConfig = userNewsConfig.getRootConfig();

//		Config kafkaConfig = rootConfig.getConfig("feature-dump-kafa");

//		for (Map.Entry<String, ConfigValue> configEntry : kafkaConfig.entrySet()) {
//			String value = (String) configEntry.getValue().unwrapped();
//			props.put(configEntry.getKey(), value);
//		}

//		ProducerConfig.ACKS_CONFIG
//		props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("metadata.broker.list", "10.19.105.105:9092,10.19.66.142:9092,10.19.119.120:9092,10.19.173.201:9092,10.19.135.108:9092");

        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        props.put("partitioner.class", "org.apache.kafka.clients.producer.internals.DefaultPartitioner");

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.19.105.105:9092,10.19.66.142:9092,10.19.119.120:9092,10.19.173.201:9092,10.19.135.108:9092");
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put("message.send.max.retries", 1);
        props.put("queue.buffering.max.messages", 2000);


        for (int i = 0; i < num; i++) {
            kafkaProd[i] = new KafkaProducer<>(props);
        }
        //kafkaProd1 = new KafkaProducer<String, String>(props);
        isInit = true;
    }

    public void send(String topic, String key, String value) {
        ProducerRecord<String, String> data = new ProducerRecord<>(topic, key, value);
        if (isInit) {
            int i = ThreadLocalRandom.current().nextInt(0, num);
            kafkaProd[i].send(data);
        } else {
            logger.error("kafka produce have not init.");
        }
    }

    public byte[] compress(String value) {
        try {
            return Snappy.compress(value);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static KafkaClient getInstance() {
        if (instance == null) {
            synchronized (KafkaClient.class) {
                if (instance == null) {
                    instance = new KafkaClient();
                    instance.Init();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        KafkaClient.getInstance().send("video_features", "a", "hello world3");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}