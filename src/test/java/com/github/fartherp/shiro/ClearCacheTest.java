/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.session.mgt.SimpleSession;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.github.fartherp.shiro.Constant.DEFAULT_CACHE_KEY_PREFIX;
import static com.github.fartherp.shiro.Constant.DEFAULT_PRINCIPAL_ID_FIELD_NAME;
import static com.github.fartherp.shiro.Constant.DEFAULT_REDISSON_LRU_OBJ_CAPACITY;

/**
 * <pre>
 *  @author: cuiyuqiang
 *  @email: cuiyuqiang@ddjf.com.cn
 *  @date: 2019/6/19 15:25
 *  @project: risk-control-parent
 * </pre>
 */
public class ClearCacheTest extends BaseTest {

    private RedisSessionDAO redisSessionDAO;

    private ClearCache clearCache;

	@BeforeMethod
	public void setUp() {
		super.setUp();
		redisCacheManager = new RedisCacheManager(redissonClient, DEFAULT_CACHE_KEY_PREFIX,
			DEFAULT_PRINCIPAL_ID_FIELD_NAME, 1, DEFAULT_REDISSON_LRU_OBJ_CAPACITY);
		redisSessionDAO = new RedisSessionDAO(redisCacheManager);
		clearCache = new ClearCache(redisSessionDAO);
	}

    @Test
    public void testRun() throws Exception {
        SimpleSession simpleSession = generateSimpleSession();
        simpleSession.setTimeout(1000);
        redisSessionDAO.doCreate(simpleSession);

        redisCacheManager.getCache("clearCache");

        Thread.sleep(1500);
		clearCache.clearSession();
		clearCache.clearCache();
    }
}
