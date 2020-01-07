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

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.StringUtils;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;

import java.util.Map;

import static com.github.fartherp.shiro.Constant.DEFAULT_CACHE_KEY_PREFIX;
import static com.github.fartherp.shiro.Constant.DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
import static com.github.fartherp.shiro.Constant.THIRTY_MINUTES;

/**
 * shiro cache manager.
 *
 * @author CK
 * @date 2019/1/1
 */
public class RedisCacheManager implements CacheManager {

    private final Map<String, Cache> caches;

    private final String keyPrefix;

	/**
	 * 毫秒
	 */
	private final long ttl;

	/**
	 * 缓存大小
	 */
    private final int cacheLruSize;

    private final Codec cacheCodec;

    private final Codec cacheKeysCodec;

    private final RedissonClient redissonClient;

    public RedisCacheManager(RedissonClient redissonClient) {
        this(redissonClient, DEFAULT_CACHE_KEY_PREFIX, THIRTY_MINUTES,
			DEFAULT_REDISSON_LRU_OBJ_CAPACITY, CodecType.FST_CODEC, CodecType.FST_CODEC);
    }

    public RedisCacheManager(RedissonClient redissonClient, String keyPrefix, long ttl,
			int cacheLruSize, CodecType cacheCodecType, CodecType cacheKeysCodecType) {
		Assert.notNull(redissonClient, "RedissonClient is no null");
    	this.redissonClient = redissonClient;
        this.keyPrefix = StringUtils.hasText(keyPrefix) ? keyPrefix : DEFAULT_CACHE_KEY_PREFIX;
        this.ttl = ttl > 0 ? ttl : THIRTY_MINUTES;
        this.cacheLruSize = cacheLruSize > 0 ? cacheLruSize : DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
        this.cacheCodec = cacheCodecType != null ? cacheCodecType.getCodec() : CodecType.FST_CODEC.getCodec();
        this.cacheKeysCodec = cacheKeysCodecType != null ? cacheKeysCodecType.getCodec()
			: CodecType.FST_CODEC.getCodec();
        this.caches = new ConcurrentLinkedHashMap.Builder<String, Cache>()
			.maximumWeightedCapacity(this.cacheLruSize).weigher(Weighers.singleton()).build();
    }

    @SuppressWarnings("all")
	@Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        return caches.computeIfAbsent(name,
			k -> new RedisCache<K, V>(this, keyPrefix + name, cacheLruSize, cacheKeysCodec));
    }

	public Map<String, Cache> getCaches() {
		return caches;
	}

    public long getTtl() {
        return ttl;
    }

	public Codec getCacheCodec() {
		return cacheCodec;
	}

	public RedissonClient getRedissonClient() {
		return redissonClient;
	}
}
