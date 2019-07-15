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

import org.apache.shiro.cache.Cache;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.github.fartherp.shiro.Constant.THIRTY_MINUTES;
import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/6/19
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
    public void testGetRedissonClient() {
        assertNotNull(redisCacheManager.getRedissonClient());
    }

    @Test
    public void testGetTtl() {
        assertEquals(redisCacheManager.getTtl(), THIRTY_MINUTES);
    }

    @Test
    public void testGetCaches() {
        assertNotNull(redisCacheManager.getCaches());
    }
}
