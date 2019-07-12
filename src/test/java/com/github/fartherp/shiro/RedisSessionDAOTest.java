/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SimpleSession;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import static com.github.fartherp.shiro.Constant.DEFAULT_SESSION_KEY_PREFIX;
import static org.testng.Assert.*;

/**
 * <pre>
 *  @author: cuiyuqiang
 *  @email: cuiyuqiang@ddjf.com.cn
 *  @date: 2019/6/19 14:10
 *  @project: risk-control-parent
 * </pre>
 */
public class RedisSessionDAOTest extends BaseTest {

    private RedisSessionDAO redisSessionDAO;

	@BeforeMethod
	public void setUp() {
		super.setUp();
		redisSessionDAO = new RedisSessionDAO(redisCacheManager);
	}

    @Test(expectedExceptions = UnknownSessionException.class)
    public void testExceptionDoCreate() {
        redisSessionDAO.doCreate(null);
    }

    @Test
    public void testDoCreate() {
        SimpleSession simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        assertNotNull(serializable);
        redisSessionDAO.delete(simpleSession);
    }

    @Test
    public void testGetActiveSessions() {
        SimpleSession simpleSession = generateSimpleSession();
        redisSessionDAO.doCreate(simpleSession);
        Collection<Session> sessions = redisSessionDAO.getActiveSessions();
        assertEquals(sessions.size(), 1);
        redisSessionDAO.delete(simpleSession);
    }

    @Test
    public void testDoReadSession() {
        SimpleSession simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session.getId(), simpleSession.getId());
        redisSessionDAO.delete(simpleSession);
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
        SimpleSession simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session.getId(), simpleSession.getId());
        SimpleSession session1 = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session1.getId(), simpleSession.getId());
        redisSessionDAO.delete(simpleSession);
    }

    @Test
    public void testRepeatGt1000DoReadSession() throws Exception {
        SimpleSession simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session.getId(), simpleSession.getId());
        Thread.sleep(1500);
        SimpleSession session1 = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session1.getId(), simpleSession.getId());
        redisSessionDAO.delete(simpleSession);
    }

    @Test(expectedExceptions = UnknownSessionException.class)
    public void testExceptionUpdate() {
        SimpleSession simpleSession = generateSimpleSession();
        redisSessionDAO.update(simpleSession);
    }

    @Test
    public void testUpdate() {
        SimpleSession simpleSession = generateSimpleSession();
        simpleSession.setId(UUID.randomUUID().toString());
        redisSessionDAO.update(simpleSession);
        redisSessionDAO.delete(simpleSession);
    }

    @Test
    public void testDelete() {
        SimpleSession simpleSession = new SimpleSession();
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
