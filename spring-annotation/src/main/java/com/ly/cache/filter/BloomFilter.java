package com.ly.cache.filter;

import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import com.ly.cache.bean.RedisKeyPrefixConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 基于Redis的分布式布隆过滤器
 * <p>
 * 布隆算法是一个以牺牲一定的准确率来换取低内存消耗的过滤算法，可以实现大量数据的过滤、去重等操作
 * 这里使用了Redis，利用Redis的BitMap实现布隆过滤器的底层映射
 * 布隆过滤器的一个关键点就是如何根据预计插入量和可接受的错误率推导出合适的Bit数组长度和Hash函数个数，
 * 当然Hash函数的选取也能影响到过滤器的准确率和性能。为此参考了Google的guava包中有关布隆过滤器的相关实现
 *
 * @author Administrator
 * @date 2018/12/10 11:52
 */
@Component
public class BloomFilter {

    @Autowired
    private JedisPool jedisPool;

    /**
     * 预计插入量
     */
    private long expectedInsertions = 1000;

    /**
     * 可接受的错误率
     */
    private double fpp = 0.001F;

    /**
     * 布隆过滤器的键在Redis中的前缀 方便统计过滤器对Redis的使用情况
     */

    private Jedis jedis;

    /**
     * 利用该初始化方法从Redis连接池中获取一个Redis链接
     * <p>
     * 注解用来修饰一个非静态的void()方法.而且这个方法不能有抛出异常声明
     * 注解方法会在构造函数之后，init()方法之前运行
     */
    @PostConstruct
    public void init() {
        this.jedis = jedisPool.getResource();
    }

    public void setExpectedInsertions(long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public void setFpp(double fpp) {
        this.fpp = fpp;
    }


    /**
     * bit数组最佳长度
     */
    private long numBits = optimalNumOfBits(expectedInsertions, fpp);

    /**
     * hash函数最佳数量
     */
    private int numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);

    // 计算hash函数个数 方法来自guava

    private int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    // 计算bit数组长度 方法来自guava

    private long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    /**
     * 判断keys是否存在于集合where中
     */
    public boolean isExist(String where, String key) {
        long[] indexs = getIndexs(key);
        boolean result;
        //这里使用了Redis管道来降低过滤器运行当中访问Redis次数 降低Redis并发量
        Pipeline pipeline = jedis.pipelined();
        try {
            for (long index : indexs) {
                pipeline.getbit(getRedisKey(where), index);
            }
            result = !pipeline.syncAndReturnAll().contains(false);
        } finally {
            try {
                pipeline.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!result) {
            put(where, key);
        }
        return result;
    }

    /**
     * 将key存入redis bitmap
     */
    private void put(String where, String key) {
        long[] indexs = getIndexs(key);
        //这里使用了Redis管道来降低过滤器运行当中访问Redis次数 降低Redis并发量
        Pipeline pipeline = jedis.pipelined();
        try {
            for (long index : indexs) {
                pipeline.setbit(getRedisKey(where), index, true);
            }
            pipeline.sync();
        } finally {
            try {
                pipeline.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据key获取bitmap下标 方法来自guava
     */
    private long[] getIndexs(String key) {
        long hash1 = hash(key);
        long hash2 = hash1 >>> 16;
        long[] result = new long[numHashFunctions];
        for (int i = 0; i < numHashFunctions; i++) {
            long combinedHash = hash1 + i * hash2;
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }
            result[i] = combinedHash % numBits;
        }
        return result;
    }

    /**
     * 获取一个hash值 方法来自guava
     */
    private long hash(String key) {
        Charset charset = Charset.forName("UTF-8");
        return Hashing.murmur3_128().hashObject(key, Funnels.stringFunnel(charset)).asLong();
    }

    private String getRedisKey(String where) {
        return RedisKeyPrefixConstant.BLOOM_FILTER + where;
    }


}
