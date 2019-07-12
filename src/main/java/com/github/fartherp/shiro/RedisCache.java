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

import com.github.fartherp.shiro.exception.CacheManagerPrincipalIdNotAssignedException;
import com.github.fartherp.shiro.exception.PrincipalIdNullException;
import com.github.fartherp.shiro.exception.PrincipalInstanceException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.ScoredEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
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
        this.redisCacheManager = redisCacheManager;
        this.cacheKeyPrefix = keyPrefix;
        this.cacheKeys = redisCacheManager.getRedissonClient().getScoredSortedSet(this.cacheKeyPrefix, cacheKeysCodec);
		this.lruMap = new LinkedHashMap<String, Object>(cacheLruSize, 0.75F, true) {
			private static final long serialVersionUID = 8936289417496258606L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
				return size() > cacheLruSize;
			}
		};
    }

    private String getRedisCacheKey(K key) {
        if (key == null) {
            return null;
        }
        String redisKey;
        if (key instanceof PrincipalCollection) {
            redisKey = getRedisKeyFromPrincipalIdField((PrincipalCollection) key);
        } else {
            redisKey = key.toString();
        }
        return this.cacheKeyPrefix + ":" + redisKey;
    }

    private String getRedisKeyFromPrincipalIdField(PrincipalCollection key) {
        Object principalObject = key.getPrimaryPrincipal();
        Method pincipalIdGetter = getPrincipalIdGetter(principalObject);
        return getIdObj(principalObject, pincipalIdGetter);
    }

    private String getIdObj(Object principalObject, Method pincipalIdGetter) {
        String redisKey;
        try {
            Object idObj = pincipalIdGetter.invoke(principalObject);
            if (idObj == null) {
                throw new PrincipalIdNullException(principalObject.getClass(), redisCacheManager.getPrincipalIdFieldName());
            }
            redisKey = idObj.toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PrincipalInstanceException(principalObject.getClass(), redisCacheManager.getPrincipalIdFieldName(), e);
        }
        return redisKey;
    }

    private Method getPrincipalIdGetter(Object principalObject) {
        Method pincipalIdGetter;
        String principalIdMethodName = this.getPrincipalIdMethodName();
        try {
            pincipalIdGetter = principalObject.getClass().getMethod(principalIdMethodName);
        } catch (NoSuchMethodException e) {
            throw new PrincipalInstanceException(principalObject.getClass(), redisCacheManager.getPrincipalIdFieldName());
        }
        return pincipalIdGetter;
    }

    private String getPrincipalIdMethodName() {
    	String principalIdFieldName = redisCacheManager.getPrincipalIdFieldName();
        if (!StringUtils.hasText(principalIdFieldName)) {
            throw new CacheManagerPrincipalIdNotAssignedException();
        }
        return "get" + principalIdFieldName.substring(0, 1).toUpperCase() + principalIdFieldName.substring(1);
    }

	@SuppressWarnings("unchecked")
	private <T> T convertLruMap(String key, Function<String, T> function) {
		return (T) lruMap.computeIfAbsent(key, function);
	}

	private RBucket<V> getBucket(K key) {
		return convertLruMap(getRedisCacheKey(key), k -> redisCacheManager.getRedissonClient().getBucket(k, redisCacheManager.getCacheCodec()));
	}

    public V get(K key) throws CacheException {
		RBucket<V> v = getBucket(key);
        return v.get();
    }

    public V put(K key, V value) throws CacheException {
        RBucket<V> v = getBucket(key);
        long ttl = redisCacheManager.getTtl();
        v.set(value, ttl, TimeUnit.SECONDS);

        cacheKeys.add(LocalDateTimeUtilies.getTimestamp(o -> o.plusSeconds(ttl)), key);
        return value;
    }

    public V remove(K key) throws CacheException {
        RBucket<V> v = getBucket(key);
        V value = v.get();
        v.delete();
        cacheKeys.remove(key);
        return value;
    }

    public void clear() throws CacheException {
        for (K key : cacheKeys) {
            RBucket<V> v = getBucket(key);
            v.delete();
        }
        cacheKeys.delete();
    }

    public int size() {
        List<ScoredEntry<K>> keys = (List<ScoredEntry<K>>) cacheKeys.entryRange(System.currentTimeMillis(), false, Double.MAX_VALUE, true);
        return keys.size();
    }

    public Set<K> keys() {
        List<ScoredEntry<K>> keys = (List<ScoredEntry<K>>) cacheKeys.entryRange(System.currentTimeMillis(), false, Double.MAX_VALUE, true);
        return keys.stream().map(ScoredEntry::getValue).collect(Collectors.toSet());
    }

    public Collection<V> values() {
        List<ScoredEntry<K>> keys = (List<ScoredEntry<K>>) cacheKeys.entryRange(System.currentTimeMillis(), false, Double.MAX_VALUE, true);

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
