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

import org.apache.shiro.session.mgt.SimpleSession;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.github.fartherp.shiro.CodecType.JSON_JACKSON_CODEC;
import static com.github.fartherp.shiro.Constant.DEFAULT_CACHE_KEY_PREFIX;
import static com.github.fartherp.shiro.Constant.DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
import static com.github.fartherp.shiro.Constant.SECONDS;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/6/19
 */
public class ClearCacheTest extends BaseTest {

    private RedisSessionDAO redisSessionDAO;

    private ClearCache clearCache;

	@BeforeMethod
	public void setUp() {
		super.setUp();
		redisCacheManager = new RedisCacheManager(redissonClient, DEFAULT_CACHE_KEY_PREFIX,
			SECONDS, DEFAULT_REDISSON_LRU_OBJ_CAPACITY, JSON_JACKSON_CODEC, JSON_JACKSON_CODEC);
		redisSessionDAO = new RedisSessionDAO(redisCacheManager);
		clearCache = new ClearCache(redisSessionDAO);
	}

    @Test
    public void testRun() throws Exception {
        SimpleSession simpleSession = generateSimpleSession();
        simpleSession.setTimeout(SECONDS);
        redisSessionDAO.doCreate(simpleSession);

        redisCacheManager.getCache("clearCache");

        Thread.sleep(1500);
		clearCache.clearSession();
		clearCache.clearCache();
    }
}
