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

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SimpleSession;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import static com.github.fartherp.shiro.CodecType.FST_CODEC;
import static com.github.fartherp.shiro.CodecType.JSON_JACKSON_CODEC;
import static com.github.fartherp.shiro.Constant.DEFAULT_CACHE_KEY_PREFIX;
import static com.github.fartherp.shiro.Constant.DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
import static com.github.fartherp.shiro.Constant.DEFAULT_SESSION_IN_MEMORY_ENABLED;
import static com.github.fartherp.shiro.Constant.DEFAULT_SESSION_KEY_PREFIX;
import static com.github.fartherp.shiro.Constant.SECONDS;
import static com.github.fartherp.shiro.Constant.THIRTY_MINUTES;
import static com.github.fartherp.shiro.ExpireType.CUSTOM_EXPIRE;
import static com.github.fartherp.shiro.ExpireType.NO_EXPIRE;
import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/6/19
 */
public class RedisSessionDAOTest extends BaseTest {

    private RedisSessionDAO redisSessionDAO;

	private SimpleSession simpleSession;

	@BeforeMethod
	public void setUp() {
		super.setUp();
		redisSessionDAO = new RedisSessionDAO(redisCacheManager);
	}

	@AfterMethod
	public void tearDown() {
		redisSessionDAO.delete(simpleSession);
	}

    @Test(expectedExceptions = UnknownSessionException.class)
    public void testExceptionDoCreate() {
        redisSessionDAO.doCreate(null);
    }

    @Test
    public void testDoCreate() {
        simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        assertNotNull(serializable);
    }

    @Test
    public void testGetActiveSessions() {
        simpleSession = generateSimpleSession();
        redisSessionDAO.doCreate(simpleSession);
        Collection<Session> sessions = redisSessionDAO.getActiveSessions();
        assertEquals(sessions.size(), 1);
    }

    @Test
    public void testDoReadSession() {
        simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session.getId(), simpleSession.getId());
    }

    @Test
    public void testNullDoReadSession() {
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(null);
        assertNull(session);
    }

	@Test
	public void testRedisNullDoReadSession() {
		SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession("11111");
		assertNull(session);
	}

    @Test
    public void testRepeatDoReadSession() {
        simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session.getId(), simpleSession.getId());
        SimpleSession session1 = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session1.getId(), simpleSession.getId());
    }

    @Test
    public void testRepeatGt1000DoReadSession() throws Exception {
        simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session.getId(), simpleSession.getId());
        Thread.sleep(1500);
        SimpleSession session1 = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session1.getId(), simpleSession.getId());
    }

	@Test
	public void testCustomExpireRepeatGt1000DoReadSession() throws Exception {
		redisCacheManager = new RedisCacheManager(redissonClient, DEFAULT_CACHE_KEY_PREFIX,
			SECONDS, DEFAULT_REDISSON_LRU_OBJ_CAPACITY, FST_CODEC, FST_CODEC);
		redisSessionDAO = new RedisSessionDAO(redisCacheManager, DEFAULT_SESSION_KEY_PREFIX,
			CUSTOM_EXPIRE, DEFAULT_SESSION_IN_MEMORY_ENABLED, SECONDS, JSON_JACKSON_CODEC,
			DEFAULT_REDISSON_LRU_OBJ_CAPACITY);

		simpleSession = generateSimpleSession();
		Serializable serializable = redisSessionDAO.doCreate(simpleSession);
		SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
		assertEquals(session.getId(), simpleSession.getId());
		Thread.sleep(1500);
		SimpleSession session1 = (SimpleSession) redisSessionDAO.doReadSession(serializable);
		assertNull(session1);
	}

	@Test
	public void testNoExpireRepeatGt1000DoReadSession() throws Exception {
		redisCacheManager = new RedisCacheManager(redissonClient, DEFAULT_CACHE_KEY_PREFIX,
			THIRTY_MINUTES, DEFAULT_REDISSON_LRU_OBJ_CAPACITY, FST_CODEC, FST_CODEC);
		redisSessionDAO = new RedisSessionDAO(redisCacheManager, DEFAULT_SESSION_KEY_PREFIX,
			NO_EXPIRE, DEFAULT_SESSION_IN_MEMORY_ENABLED, SECONDS, JSON_JACKSON_CODEC,
			DEFAULT_REDISSON_LRU_OBJ_CAPACITY);

		simpleSession = generateSimpleSession();
		Serializable serializable = redisSessionDAO.doCreate(simpleSession);
		SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
		assertEquals(session.getId(), simpleSession.getId());
		Thread.sleep(1500);
		SimpleSession session1 = (SimpleSession) redisSessionDAO.doReadSession(serializable);
		assertEquals(session1.getId(), session.getId());
	}

    @Test(expectedExceptions = UnknownSessionException.class)
    public void testExceptionUpdate() {
        simpleSession = generateSimpleSession();
        redisSessionDAO.update(simpleSession);
    }

    @Test
    public void testUpdate() {
        simpleSession = generateSimpleSession();
        simpleSession.setId(UUID.randomUUID().toString());
        redisSessionDAO.update(simpleSession);
    }

    @Test
    public void testDelete() {
        simpleSession = new SimpleSession();
        redisSessionDAO.delete(simpleSession);
    }

    @Test
    public void testGetSessionKeyPrefix() {
        assertEquals(redisSessionDAO.getSessionKeyPrefix(), DEFAULT_SESSION_KEY_PREFIX);
    }

    @Test
    public void testGetExpire() {
        assertEquals(redisSessionDAO.getExpire(), ExpireType.DEFAULT_EXPIRE.type);
    }

    @Test
    public void testIsSessionInMemoryEnabled() {
        assertTrue(redisSessionDAO.isSessionInMemoryEnabled());
    }

    @Test
    public void testGetSessionInMemoryTimeout() {
        assertEquals(redisSessionDAO.getSessionInMemoryTimeout(), 1000L);
    }

    @Test
    public void testGetRedisCacheManager() {
        assertEquals(redisSessionDAO.getRedisCacheManager(), redisCacheManager);
    }

    @Test
    public void testGetSessionKeys() {
        assertNotNull(redisSessionDAO.getSessionKeys());
    }
}
