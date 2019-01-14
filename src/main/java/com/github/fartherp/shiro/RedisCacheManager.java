/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Assert;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: CK
 * @date: 2019/1/1
 */
public class RedisCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>();

    public static final String DEFAULT_CACHE_KEY_PREFIX = "shiro:cache:";
    private String keyPrefix = DEFAULT_CACHE_KEY_PREFIX;
    public static final String DEFAULT_PRINCIPAL_ID_FIELD_NAME = "id";
    private String principalIdFieldName = DEFAULT_PRINCIPAL_ID_FIELD_NAME;

    private long ttl = 30L;

    private RedissonClient redissonClient;

    public RedisCacheManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @SuppressWarnings("all")
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        Assert.notNull(redissonClient, "RedissonClient is no null");
        Cache cache = caches.get(name);
        if (cache == null) {
            cache = new RedisCache<K, V>(this, keyPrefix + name, principalIdFieldName, ttl);
            caches.put(name, cache);
        }
        return cache;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        if (StringUtils.isNotBlank(keyPrefix)) {
            this.keyPrefix = keyPrefix;
        }
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        if (ttl > 0) {
            this.ttl = ttl;
        }
    }

    public String getPrincipalIdFieldName() {
        return principalIdFieldName;
    }

    public void setPrincipalIdFieldName(String principalIdFieldName) {
        if (StringUtils.isNotBlank(principalIdFieldName)) {
            this.principalIdFieldName = principalIdFieldName;
        }
    }

    public ConcurrentMap<String, Cache> getCaches() {
        return caches;
    }
}
