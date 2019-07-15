/**
 *    Copyright (c) 2019 CK.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 冗余结构数据删除.
 *
 * @author CK
 * @date 2019/1/4
 */
public class ClearCache implements TimerTask {
    private final HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();

    private final RedisSessionDAO redisSessionDAO;

    public ClearCache(RedisSessionDAO redisSessionDAO) {
        this.redisSessionDAO = redisSessionDAO;
    }

    public void run() {
		clearSession();
		clearCache();
        hashedWheelTimer.newTimeout(this, redisSessionDAO.getRedisCacheManager().getTtl(), TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    public void clearSession() {
        RScoredSortedSet<String> sessionKeys = redisSessionDAO.getSessionKeys();
        removeAll(sessionKeys, o -> (List<ScoredEntry>) o.entryRange(0, false, System.currentTimeMillis(), true));
    }

    @SuppressWarnings("unchecked")
    public void clearCache() {
        ConcurrentMap<String, Cache> caches = redisSessionDAO.getRedisCacheManager().getCaches();
        caches.forEach((k, v) -> {
            RedisCache redisCache = (RedisCache) v;
            RScoredSortedSet cacheKeys = redisCache.getCacheKeys();
            removeAll(cacheKeys, o -> (List<ScoredEntry>) o.entryRange(0, false, System.currentTimeMillis(), true));
        });
    }

    @SuppressWarnings("unchecked")
    private void removeAll(RScoredSortedSet rScoredSortedSet, Function<RScoredSortedSet, List<ScoredEntry>> fun) {
        List<ScoredEntry> keys = fun.apply(rScoredSortedSet);
        List<Object> destroyKeys = keys.stream().map(ScoredEntry::getValue).collect(Collectors.toList());
        if (destroyKeys.size() > 0) {
            rScoredSortedSet.removeAll(destroyKeys);
        }
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        run();
    }
}
