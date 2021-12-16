package com.td.recommend.video.utils;

import com.typesafe.config.Config;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class RedisClientSingleton {
    private static final Logger log = LoggerFactory.getLogger(RedisClientSingleton.class);
    public static final RedisClientSingleton general = new RedisClientSingleton("general-jedis");
    public static final RedisClientSingleton boost = new RedisClientSingleton("boost-jedis");
    public static final RedisClientSingleton query = new RedisClientSingleton("query-jedis");
    public static final RedisClientSingleton search = new RedisClientSingleton("search-server");
    public static final RedisClientSingleton recreason = new RedisClientSingleton("recreason-server");
    public static final RedisClientSingleton swipe = new RedisClientSingleton("swipe-server");
    public static final RedisClientSingleton rlvtdyns = new RedisClientSingleton("rlvtdyns-server");


    private JedisPool jedisPool;

    private RedisClientSingleton(String serverConf) {
        Config redisConf = UserVideoConfig.getInstance().getRootConfig().getConfig(serverConf);
        String redisHost = redisConf.getString("jedis.host");
        int redisPort = redisConf.getInt("jedis.port");
        int timeout = redisConf.getInt("jedis.timeout");
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(redisConf.getInt("jedis.pool.config.maxTotal"));
        poolConfig.setMaxIdle(redisConf.getInt("jedis.pool.config.maxIdle"));
        poolConfig.setMinIdle(redisConf.getInt("jedis.pool.config.minIdle"));
        poolConfig.setTestOnBorrow(redisConf.getBoolean("jedis.pool.config.testOnBorrow"));
        poolConfig.setTestOnReturn(redisConf.getBoolean("jedis.pool.config.testOnReturn"));
        poolConfig.setTestWhileIdle(redisConf.getBoolean("jedis.pool.config.testWhileIdle"));
        poolConfig.setMaxWaitMillis(redisConf.getLong("jedis.pool.config.maxWaitMillis"));
        poolConfig.setMinEvictableIdleTimeMillis(redisConf.getLong("jedis.pool.config.minEvictableIdleTimeMillis"));
        poolConfig.setTimeBetweenEvictionRunsMillis(redisConf.getLong("jedis.pool.config.timeBetweenEvictionRunsMillis"));
        poolConfig.setNumTestsPerEvictionRun(redisConf.getInt("jedis.pool.config.numTestsPerEvictionRun"));
        jedisPool = new JedisPool(poolConfig, redisHost, redisPort, timeout);
        log.info("init jedis pool: {}:{} {}", new Object[]{redisHost, Integer.valueOf(redisPort), poolConfig});
    }

    public String get(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.get(key);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public byte[] get(byte[] key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.get(key);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public Long ttl(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.ttl(key);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public List<String> lrange(String key, long start, long end) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.lrange(key, start, end);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public List<String> mget(String... keys) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.mget(keys);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public Set<String> zrange(String key, long start, long end) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.zrange(key, start, end);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public Set<String> smembers(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.smembers(key);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public void expire(String key, int seconds) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.expire(key, seconds);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public Set<String> keys(String pattern) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.keys(pattern);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public long pfCountAndDel(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
            Transaction multi = jedis.multi();
            Response<Long> count = multi.pfcount(key);
            multi.del(key);
            multi.exec();
            return count.get();
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public long pfadd(String key, String... value) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.pfadd(key, value);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public long pfcount(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.pfcount(key);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public String set(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.set(key, value);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public String set(String key, String value, String nxxx, String expx, int time) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.set(key, value, nxxx, expx, time);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public long del(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.del(key);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public Boolean exists(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.exists(key);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    public Map<String, String> hgetall(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.hgetAll(key);
        }finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    public String hget(String key, String field) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.hget(key, field);
        }finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }

    public void hset(String key, String field,String value) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.hset(key, field, value);
        }finally {
            if (null != jedis) {
                jedis.close();
            }
        }
    }
    public static void main(String[] args) {
//        Map<String, String> fieldMap = RedisClientSingleton.recreason.hgetall("rec_reason");
//        for (String key: fieldMap.keySet()) {
//            System.out.println(key+" "+fieldMap.get(key));
//        }
//        List<String> top = RedisClientSingleton.general.lrange("vmp3_哎呀呀", 0, 10);//vmp3_哎呀呀"
//        System.out.println(top);
//        Boolean fit = RedisClientSingleton.search.exists("fitnessdiu_862013042477067");
//        System.out.println(fit);
//        String x = RedisClientSingleton.search.get("fitnessdiu_862013042477067");
//        System.out.println(x);
//        System.out.println(x=="");
    }

}
