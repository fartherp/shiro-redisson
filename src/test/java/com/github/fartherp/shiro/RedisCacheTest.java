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

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.github.fartherp.shiro.CodecType.JSON_JACKSON_CODEC;
import static com.github.fartherp.shiro.Constant.DEFAULT_CACHE_KEY_PREFIX;
import static com.github.fartherp.shiro.Constant.DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
import static com.github.fartherp.shiro.Constant.THIRTY_MINUTES;
import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/1/1
 */
public class RedisCacheTest extends BaseTest {

    private RedisCache<Object, String> redisCache;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() {
		super.setUp();
		redisCacheManager = new RedisCacheManager(redissonClient, DEFAULT_CACHE_KEY_PREFIX,
			THIRTY_MINUTES, DEFAULT_REDISSON_LRU_OBJ_CAPACITY, JSON_JACKSON_CODEC, JSON_JACKSON_CODEC);
		redisCache = (RedisCache) redisCacheManager.getCache("test_tmp_redis_cache");
	}

	@AfterMethod
	public void tearDown() {
		redisCache.clear();
		super.tearDown();
	}

	private void putAll() {
		redisCache.put("1", "111111");
		redisCache.put("2", "222222");
		redisCache.put("3", "333333");
	}

	private List<PrincipalCollection> putPrincipalCollectionAll() {
		List<PrincipalCollection> list = new ArrayList<>();
		User user1 = new User();
		user1.setId(1);
		PrincipalCollection a = new SimplePrincipalCollection(user1, "张三");
		list.add(a);
		redisCache.put(a, "111111");
		User user2 = new User();
		user2.setId(2);
		PrincipalCollection b = new SimplePrincipalCollection(user2, "李四");
		list.add(b);
		redisCache.put(b, "222222");
		User user3 = new User();
		user3.setId(3);
		PrincipalCollection c = new SimplePrincipalCollection(user3, "王五");
		list.add(c);
		redisCache.put(c, "333333");
		return list;
	}

	@Test
    public void testPut() {
        String value = "111111";
        String result = redisCache.put("1", value);
        assertEquals(result, value);
    }

	@Test
	public void testPrincipalCollectionPut() {
		String value = "111111";
		User user1 = new User();
		user1.setId(1);
		String result = redisCache.put(new SimplePrincipalCollection(user1, "张三"), value);
		assertEquals(result, value);
	}

	@Test
	public void testPrincipalObjectNullPut() {
		PrincipalCollection a = new SimplePrincipalCollection();
		try {
			redisCache.put(a, "111111");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Principal shouldn't be null");
		}
	}

	@Test
	public void testNoImplementsShiroFieldAccessPut() {
		UserBase user = new UserBase();
		user.setId(1);
		PrincipalCollection a = new SimplePrincipalCollection(user, "张三");
		try {
			redisCache.put(a, "111111");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Principal class com.github.fartherp.shiro.BaseTest$UserBase " +
				"must implements com.github.fartherp.shiro.ShiroFieldAccess");
		}
	}

    @Test
    public void testSize() {
		putAll();
        assertEquals(redisCache.size(), 3);
    }

	@Test
	public void testPrincipalCollectionSize() {
		putPrincipalCollectionAll();
		assertEquals(redisCache.size(), 3);
	}

	@Test
	public void testKeyNullGet() {
		String result = redisCache.get(null);
		assertNull(result);
	}

    @Test
    public void testGet() {
		putAll();
        String result = redisCache.get("1");
        assertEquals(result, "111111");
    }

	@Test
	public void testPrincipalCollectionGet() {
		List<PrincipalCollection> list = putPrincipalCollectionAll();
		String result = redisCache.get(list.get(0));
		assertEquals(result, "111111");
	}

    @Test
    public void testKeys() {
		putAll();
        Set<Object> set = redisCache.keys();
        assertEquals(set.size(), 3);
        assertTrue(set.contains("1"));
        assertTrue(set.contains("2"));
        assertTrue(set.contains("3"));
    }

    @Test
    public void testValues() {
		putAll();
        Collection<String> set = redisCache.values();
        assertEquals(set.size(), 3);
        assertTrue(set.contains("111111"));
        assertTrue(set.contains("222222"));
        assertTrue(set.contains("333333"));
    }

	@Test
	public void testPrincipalCollectionValues() {
		putPrincipalCollectionAll();
		Collection<String> set = redisCache.values();
		assertEquals(set.size(), 3);
		assertTrue(set.contains("111111"));
		assertTrue(set.contains("222222"));
		assertTrue(set.contains("333333"));
	}

    @Test
    public void testRemove() {
		putAll();
        redisCache.remove("1");
        assertEquals(redisCache.getCacheKeys().size(), 2);
    }

    @Test
    public void testClear() {
		putAll();
        redisCache.clear();
        assertEquals(redisCache.getCacheKeys().size(), 0);
    }
}
