/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.shiro.cache.Cache;
import org.redisson.api.RScoredSortedSet;
import org.redisson.client.protocol.ScoredEntry;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: CK
 * @date: 2019/1/4
 */
public class ClearCache implements TimerTask {
    private HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();

    private RedisSessionDAO redisSessionDAO;

    public ClearCache(RedisSessionDAO redisSessionDAO) {
        this.redisSessionDAO = redisSessionDAO;
    }

    public void init() {
        hashedWheelTimer.newTimeout(this, redisSessionDAO.getRedisCacheManager().getTtl(), TimeUnit.MINUTES);
    }

    public void clearSession() {
        RScoredSortedSet<String> sessionKeys = redisSessionDAO.getSessionKeys();
        List<ScoredEntry<String>> keys = (List<ScoredEntry<String>>) sessionKeys.entryRange(0, false, System.currentTimeMillis(), true);
        List<String> destroyKeys = keys.stream().map(ScoredEntry::getValue).collect(Collectors.toList());
        if (destroyKeys.size() > 0) {
            sessionKeys.removeAll(destroyKeys);
        }
    }

    @SuppressWarnings("unchecked")
    public void clearCache() {
        ConcurrentMap<String, Cache> caches = redisSessionDAO.getRedisCacheManager().getCaches();
        caches.forEach((k, v) -> {
            RedisCache redisCache = (RedisCache) v;
            RScoredSortedSet cacheKeys = redisCache.getCacheKeys();
            List<ScoredEntry> keys = (List<ScoredEntry>) cacheKeys.entryRange(0, false, System.currentTimeMillis(), true);
            List<Object> destroyKeys = keys.stream().map(ScoredEntry::getValue).collect(Collectors.toList());
            if (destroyKeys.size() > 0) {
                cacheKeys.removeAll(destroyKeys);
            }
        });
    }

    public void run(Timeout timeout) throws Exception {
        clearSession();
        clearCache();
        init();
    }
}
