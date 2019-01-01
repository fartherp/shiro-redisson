/*
 * Copyright (c) 2018. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import com.github.fartherp.shiro.exception.CacheManagerPrincipalIdNotAssignedException;
import com.github.fartherp.shiro.exception.PrincipalIdNullException;
import com.github.fartherp.shiro.exception.PrincipalInstanceException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: CK
 * @date: 2018/12/29
 */
public class RedisCache<K, V> implements Cache<K, V> {

    private RedisCacheManager redisCacheManager;

    private String cacheKeyPrefix;

    private String principalIdFieldName;

    public RedisCache(RedisCacheManager redisCacheManager, String keyPrefix, String principalIdFieldName) {
        this.redisCacheManager = redisCacheManager;
        this.cacheKeyPrefix = keyPrefix;
        this.principalIdFieldName = principalIdFieldName;
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
                throw new PrincipalIdNullException(principalObject.getClass(), this.principalIdFieldName);
            }
            redisKey = idObj.toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PrincipalInstanceException(principalObject.getClass(), this.principalIdFieldName, e);
        }
        return redisKey;
    }

    private Method getPrincipalIdGetter(Object principalObject) {
        Method pincipalIdGetter = null;
        String principalIdMethodName = this.getPrincipalIdMethodName();
        try {
            pincipalIdGetter = principalObject.getClass().getMethod(principalIdMethodName);
        } catch (NoSuchMethodException e) {
            throw new PrincipalInstanceException(principalObject.getClass(), this.principalIdFieldName);
        }
        return pincipalIdGetter;
    }

    private String getPrincipalIdMethodName() {
        if (this.principalIdFieldName == null || "".equals(this.principalIdFieldName)) {
            throw new CacheManagerPrincipalIdNotAssignedException();
        }
        return "get" + this.principalIdFieldName.substring(0, 1).toUpperCase() + this.principalIdFieldName.substring(1);
    }

    public V get(K key) throws CacheException {
        RBucket<V> v = redisCacheManager.getRedissonClient().getBucket(getRedisCacheKey(key));
        return v.get();
    }

    public V put(K key, V value) throws CacheException {
        RSet<K> keys = redisCacheManager.getRedissonClient().getSet(cacheKeyPrefix);
        keys.add(key);
        RBucket<V> v = redisCacheManager.getRedissonClient().getBucket(getRedisCacheKey(key));
        v.set(value);
        return value;
    }

    public V remove(K key) throws CacheException {
        RBucket<V> v = redisCacheManager.getRedissonClient().getBucket(getRedisCacheKey(key));
        V value = v.get();
        v.delete();
        return value;
    }

    public void clear() throws CacheException {
        RSet<String> keys = redisCacheManager.getRedissonClient().getSet(cacheKeyPrefix);
        keys.delete();
    }

    public int size() {
        RSet<String> keys = redisCacheManager.getRedissonClient().getSet(cacheKeyPrefix);
        return keys.size();
    }

    public Set<K> keys() {
        RSet<K> keys = redisCacheManager.getRedissonClient().getSet(cacheKeyPrefix);
        return keys;
    }

    public Collection<V> values() {
        RSet<K> keys = redisCacheManager.getRedissonClient().getSet(cacheKeyPrefix);

        List<V> values = new ArrayList<>(keys.size());
        for (K key : keys) {
            RBucket<V> v = redisCacheManager.getRedissonClient().getBucket(getRedisCacheKey(key));
            values.add(v.get());
        }

        return Collections.unmodifiableList(values);
    }
}
