/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.session.mgt.SimpleSession;
import org.testng.annotations.Test;

/**
 * <pre>
 *  @author: cuiyuqiang
 *  @email: cuiyuqiang@ddjf.com.cn
 *  @date: 2019/6/19 15:25
 *  @project: risk-control-parent
 * </pre>
 */
public class ClearCacheTest extends BaseTest {

    private RedisSessionDAO redisSessionDAO = new RedisSessionDAO(redisCacheManager);

    private ClearCache clearCache = new ClearCache(redisSessionDAO);

    @Test
    public void testRun() throws Exception {
        SimpleSession simpleSession = generateSimpleSession();
        simpleSession.setTimeout(1000);
        redisSessionDAO.doCreate(simpleSession);

        redisCacheManager.setTtl(1);
        redisCacheManager.getCache("clearCache");

        Thread.sleep(1500);
        clearCache.run(null);
    }
}