package com.ly.cache.service;

/**
 * @author Administrator
 * @date 2018/12/10 16:39
 */
public interface CacheService {

    String getKeyWithLock(String key, String lockKey, String uniqueId, long expireTime);

    boolean tryDistributedLock(String lockKey, String uniqueId, long expireTime);

    boolean releaseDistributedLock(String lockKey, String uniqueId);
}
