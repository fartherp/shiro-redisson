/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.StringUtils;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.github.fartherp.shiro.Constant.DEFAULT_CACHE_KEY_PREFIX;
import static com.github.fartherp.shiro.Constant.DEFAULT_PRINCIPAL_ID_FIELD_NAME;
import static com.github.fartherp.shiro.Constant.DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
import static com.github.fartherp.shiro.Constant.THIRTY_MINUTES;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/1/1
 */
public class RedisCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>();

    private final String keyPrefix;

    private final String principalIdFieldName;

    private final long ttl;

    private final int cacheLruSize;

    private final RedissonClient redissonClient;

    public RedisCacheManager(RedissonClient redissonClient) {
        this(redissonClient, DEFAULT_CACHE_KEY_PREFIX, DEFAULT_PRINCIPAL_ID_FIELD_NAME,
			THIRTY_MINUTES, DEFAULT_REDISSON_LRU_OBJ_CAPACITY);
    }

    public RedisCacheManager(RedissonClient redissonClient, String keyPrefix, String principalIdFieldName,
							 long ttl, int cacheLruSize) {
        this.redissonClient = redissonClient;
        this.keyPrefix = StringUtils.hasText(keyPrefix) ? keyPrefix : DEFAULT_CACHE_KEY_PREFIX;
        this.principalIdFieldName = StringUtils.hasText(principalIdFieldName) ? principalIdFieldName : DEFAULT_PRINCIPAL_ID_FIELD_NAME;
        this.ttl = ttl > 0 ? ttl : THIRTY_MINUTES;
        this.cacheLruSize = cacheLruSize > 0 ? cacheLruSize : DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
    }

    @SuppressWarnings("all")
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        Assert.notNull(redissonClient, "RedissonClient is no null");
        Cache cache = caches.get(name);
        if (cache == null) {
            cache = new RedisCache<K, V>(this, keyPrefix + name, principalIdFieldName,
				ttl, cacheLruSize);
            caches.put(name, cache);
        }
        return cache;
    }

	public ConcurrentMap<String, Cache> getCaches() {
		return caches;
	}

    public String getKeyPrefix() {
        return keyPrefix;
    }

	public String getPrincipalIdFieldName() {
		return principalIdFieldName;
	}

    public long getTtl() {
        return ttl;
    }

	public RedissonClient getRedissonClient() {
		return redissonClient;
	}
}
