/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

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

    @SuppressWarnings("unchecked")
    private RedisCache<String, String> redisCache = (RedisCache) redisCacheManager.getCache("test_tmp_redis_cache");

    @Test(priority = 1)
    public void testPut() {
        String value1 = "111111";
        String result1 = redisCache.put("1", value1);
        assertEquals(result1, value1);
        String value2 = "222222";
        String result2 = redisCache.put("2", value2);
        assertEquals(result2, value2);
        String value3 = "333333";
        String result3 = redisCache.put("3", value3);
        assertEquals(result3, value3);
    }

    @Test(priority = 2)
    public void testSize() {
        assertEquals(redisCache.size(), 3);
    }

    @Test(priority = 3)
    public void testGet() {
        String resul1 = redisCache.get("1");
        assertEquals(resul1, "111111");
        String result2 = redisCache.get("2");
        assertEquals(result2, "222222");
        String result3 = redisCache.get("3");
        assertEquals(result3, "333333");
    }

    @Test(priority = 4)
    public void testKeys() {
        Set<String> set = redisCache.keys();
        assertEquals(set.size(), 3);
        assertTrue(set.contains("1"));
        assertTrue(set.contains("2"));
        assertTrue(set.contains("3"));
    }

    @Test(priority = 5)
    public void testValues() {
        Collection<String> set = redisCache.values();
        assertEquals(set.size(), 3);
        assertTrue(set.contains("111111"));
        assertTrue(set.contains("222222"));
        assertTrue(set.contains("333333"));
    }

    @Test(priority = 6)
    public void testRemove() {
        redisCache.remove("1");
        assertEquals(redisCache.getCacheKeys().size(), 2);
    }

    @Test(priority = 7)
    public void testClear() {
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