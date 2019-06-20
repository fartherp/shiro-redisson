/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: CK
 * @date: 2019/1/1
 */
public class RedisCacheTest extends BaseTest {

    private RedisCache<String, String> redisCache;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() {
		super.setUp();
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

	@Test
    public void testPut() {
        String value = "111111";
        String result = redisCache.put("1", value);
        assertEquals(result, value);
    }

    @Test
    public void testSize() {
		putAll();
        assertEquals(redisCache.size(), 3);
    }

    @Test
    public void testGet() {
		putAll();
        String result = redisCache.get("1");
        assertEquals(result, "111111");
    }

    @Test
    public void testKeys() {
		putAll();
        Set<String> set = redisCache.keys();
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

    @Test
    public void testPincipalIdGetter() throws Exception {
        User user = new User();
        Method pincipalIdGetter = user.getClass().getMethod("getId");
        assertNotNull(pincipalIdGetter);
    }

    public static class UserBase {
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    public static class User extends UserBase {

    }
}
