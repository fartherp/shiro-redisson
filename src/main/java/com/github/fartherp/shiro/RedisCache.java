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

import com.github.fartherp.shiro.exception.CachePrincipalNotImplementsAssignedException;
import com.github.fartherp.shiro.exception.PrincipalNullException;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.Assert;
import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.ScoredEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.fartherp.shiro.Constant.MILLISECONDS_NANO;

/**
 * shiro cache 操作.
 *
 * @author CK
 * @date 2018/12/29
 */
public class RedisCache<K, V> implements Cache<K, V> {

    private final RedisCacheManager redisCacheManager;

    private final String cacheKeyPrefix;

    private final RScoredSortedSet<K> cacheKeys;

	private final Map<String, Object> lruMap;

    public RedisCache(RedisCacheManager redisCacheManager, String keyPrefix, int cacheLruSize, Codec cacheKeysCodec) {
		Assert.notNull(redisCacheManager, "redisCacheManager is no null");
        this.redisCacheManager = redisCacheManager;
        this.cacheKeyPrefix = keyPrefix;
        this.cacheKeys = redisCacheManager.getRedissonClient().getScoredSortedSet(this.cacheKeyPrefix, cacheKeysCodec);
		this.lruMap = new ConcurrentLinkedHashMap.Builder<String, Object>()
			.maximumWeightedCapacity(cacheLruSize).weigher(Weighers.singleton()).build();
    }

    private String getRedisCacheKey(K key) {
        StringBuilder sb = new StringBuilder(this.cacheKeyPrefix);
		sb.append(':');
        if (key instanceof PrincipalCollection) {
			sb.append(getRedisKeyFromPrincipalUnique((PrincipalCollection) key));
        } else {
			sb.append(key);
        }
        return sb.toString();
    }

    private String getRedisKeyFromPrincipalUnique(PrincipalCollection key) {
        Object principalObject = key.getPrimaryPrincipal();
		if (principalObject == null) {
			throw new PrincipalNullException();
		}
        if (principalObject instanceof ShiroFieldAccess) {
        	return ((ShiroFieldAccess) principalObject).unique();
		}
		throw new CachePrincipalNotImplementsAssignedException(principalObject.getClass());
    }

	@SuppressWarnings("unchecked")
	private <T> T convertLruMap(String key, Function<String, T> function) {
		return (T) lruMap.computeIfAbsent(key, function);
	}

	private RBucket<V> getBucket(K key) {
		return convertLruMap(getRedisCacheKey(key),
			k -> redisCacheManager.getRedissonClient().getBucket(k, redisCacheManager.getCacheCodec()));
	}

	@Override
    public V get(K key) throws CacheException {
		RBucket<V> v = getBucket(key);
        return v.get();
    }

	@Override
    public V put(K key, V value) throws CacheException {
        RBucket<V> v = getBucket(key);
        long ttl = redisCacheManager.getTtl();
        v.set(value, ttl, TimeUnit.MILLISECONDS);

        cacheKeys.add(LocalDateTimeUtilies.getTimestamp(o -> o.plusNanos(ttl * MILLISECONDS_NANO)), key);
        return value;
    }

	@Override
    public V remove(K key) throws CacheException {
        RBucket<V> v = getBucket(key);
        V value = v.get();
        v.delete();
        cacheKeys.removeAsync(key);
        return value;
    }

	@Override
    public void clear() throws CacheException {
        for (K key : cacheKeys) {
            RBucket<V> v = getBucket(key);
            v.deleteAsync();
        }
        cacheKeys.deleteAsync();
    }

	@Override
    public int size() {
        List<ScoredEntry<K>> keys = (List<ScoredEntry<K>>) cacheKeys.entryRange(System.currentTimeMillis(),
			false, Double.MAX_VALUE, true);
        return keys.size();
    }

	@Override
    public Set<K> keys() {
        List<ScoredEntry<K>> keys = (List<ScoredEntry<K>>) cacheKeys.entryRange(System.currentTimeMillis(),
			false, Double.MAX_VALUE, true);
        return keys.stream().map(ScoredEntry::getValue).collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        List<ScoredEntry<K>> keys = (List<ScoredEntry<K>>) cacheKeys.entryRange(System.currentTimeMillis(),
			false, Double.MAX_VALUE, true);

        List<V> values = new ArrayList<>(keys.size());
        for (ScoredEntry<K> key : keys) {
            RBucket<V> v = getBucket(key.getValue());
            V value = v.get();
            if (value != null) {
                values.add(value);
            }
        }

        return Collections.unmodifiableList(values);
    }

    public RScoredSortedSet<K> getCacheKeys() {
        return cacheKeys;
    }
}
