package com.ly.cache.service.impl;

import com.ly.cache.service.CacheService;
import com.ly.dto.User;
import com.ly.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.util.Collections;


/**
 * @author Administrator
 * @date 2018/12/10 16:41
 */
@Service
public class CacheServiceRedisImpl implements CacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheServiceRedisImpl.class);

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;

    @Autowired
    private JedisPool pool;

    @Autowired
    private UserService userService;

    private Jedis jedis;


    public Jedis getJedis() {
        return jedis;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

    @PostConstruct
    private void init() {
        this.jedis = pool.getResource();
    }

    @Override
    public String getKeyWithLock(String key, String lockKey, String uniqueId, long expireTime) {

        // 通过key获取value
        String value = jedis.get(key);
        if (StringUtils.isEmpty(value)) {

            //封装的tryDistributedLock包括setnx和expire两个功能，在低版本的redis中不支持

            try {
                boolean locked = this.tryDistributedLock(lockKey, uniqueId, expireTime);
                if (locked) {
                    User user = userService.getUserById(key);
                    value = user.getName();
                    jedis.set(key, value);
                    jedis.del(lockKey);
                    return value;
                } else {
                    // 其它线程进来了没获取到锁便等待50ms后重试
                    Thread.sleep(50);
                    getKeyWithLock(key, lockKey, uniqueId, expireTime);
                }
            } catch (Exception e) {
                LOGGER.error("getWithLock exception = {}", e);
                return value;
            } finally {
                this.releaseDistributedLock(lockKey, uniqueId);
            }
        }
        return value;
    }

    @Override
    public boolean tryDistributedLock(String lockKey, String uniqueId, long expireTime) {
        // jedis options
        String result = jedis.set(lockKey, uniqueId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        if (LOCK_SUCCESS.equalsIgnoreCase(result)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean releaseDistributedLock(String lockKey, String uniqueId) {
        // jedis options

        /*
        // 判断加锁与解锁是不是同一个客户端
        if (uniqueId.equals(jedis.get(lockKey))) {
            // 若在此时，这把锁突然不是这个客户端的，则会误解锁
            jedis.del(lockKey);
        }
        */

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(uniqueId));
        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
}
