/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.cache.Cache;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.github.fartherp.shiro.Constant.DEFAULT_CACHE_KEY_PREFIX;
import static com.github.fartherp.shiro.Constant.DEFAULT_PRINCIPAL_ID_FIELD_NAME;
import static com.github.fartherp.shiro.Constant.THIRTY_MINUTES;
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
        assertEquals(redisCacheManager.getKeyPrefix(), DEFAULT_CACHE_KEY_PREFIX);
    }

    @Test
    public void testGetRedissonClient() {
        assertNotNull(redisCacheManager.getRedissonClient());
    }

    @Test
    public void testGetTtl() {
        assertEquals(redisCacheManager.getTtl(), THIRTY_MINUTES);
    }

    @Test
    public void testGetPrincipalIdFieldName() {
        assertEquals(redisCacheManager.getPrincipalIdFieldName(), DEFAULT_PRINCIPAL_ID_FIELD_NAME);
    }

    @Test
    public void testGetCaches() {
        assertNotNull(redisCacheManager.getCaches());
    }
}
