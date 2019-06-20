/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.cache.Cache;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.github.fartherp.shiro.RedisCacheManager.MINUTE;
import static org.testng.Assert.*;

/**
 * <pre>
 *  @author: cuiyuqiang
 *  @email: cuiyuqiang@ddjf.com.cn
 *  @date: 2019/6/19 9:38
 *  @project: risk-control-parent
 * </pre>
 */
public class RedisCacheManagerTest extends BaseTest {

	@BeforeMethod
	public void setUp() {
		super.setUp();
	}

    @Test
    public void testGetCache() {
        String key = "test_tmp_cache";
        Cache<Integer, String> cache = redisCacheManager.getCache(key);
        assertNotNull(cache);
        assertEquals(redisCacheManager.getCaches().size(), 1);
        assertEquals(redisCacheManager.getCaches().get(key), cache);
    }

    @Test
    public void testGetKeyPrefix() {
        redisCacheManager.setKeyPrefix("  ");
        assertEquals(redisCacheManager.getKeyPrefix(), RedisCacheManager.DEFAULT_CACHE_KEY_PREFIX);
    }

    @Test
    public void testSetKeyPrefix() {
        String keyPrefix = "shiro:cache:test:";
        redisCacheManager.setKeyPrefix(keyPrefix);
        assertEquals(redisCacheManager.getKeyPrefix(), keyPrefix);
    }

    @Test
    public void testGetRedissonClient() {
        assertNotNull(redisCacheManager.getRedissonClient());
    }

    @Test
    public void testGetTtl() {
        redisCacheManager.setTtl(0);
        assertEquals(redisCacheManager.getTtl(), 30 * MINUTE);
    }

    @Test
    public void testSetTtl() {
        long ttl = 40;
        redisCacheManager.setTtl(ttl);
        assertEquals(redisCacheManager.getTtl(), ttl);
    }

    @Test
    public void testGetPrincipalIdFieldName() {
        redisCacheManager.setPrincipalIdFieldName("  ");
        assertEquals(redisCacheManager.getPrincipalIdFieldName(), RedisCacheManager.DEFAULT_PRINCIPAL_ID_FIELD_NAME);
    }

    @Test
    public void testSetPrincipalIdFieldName() {
        String principalIdFieldName = "testId";
        redisCacheManager.setPrincipalIdFieldName(principalIdFieldName);
        assertEquals(redisCacheManager.getPrincipalIdFieldName(), principalIdFieldName);
    }

    @Test
    public void testGetCaches() {
        assertNotNull(redisCacheManager.getCaches());
    }
}
