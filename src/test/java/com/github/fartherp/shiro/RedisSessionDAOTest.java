/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SimpleSession;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

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

    private RedisSessionDAO redisSessionDAO = new RedisSessionDAO(redisCacheManager);

    @Test(expectedExceptions = UnknownSessionException.class, priority = 1)
    public void testExceptionDoCreate() {
        redisSessionDAO.doCreate(null);
    }

    @Test(priority = 2)
    public void testDoCreate() {
        SimpleSession simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        assertNotNull(serializable);
        redisSessionDAO.delete(simpleSession);
    }

    @Test(priority = 3)
    public void testGetActiveSessions() {
        SimpleSession simpleSession = generateSimpleSession();
        redisSessionDAO.doCreate(simpleSession);
        Collection<Session> sessions = redisSessionDAO.getActiveSessions();
        assertEquals(sessions.size(), 1);
        redisSessionDAO.delete(simpleSession);
    }

    @Test(priority = 4)
    public void testDoReadSession() {
        SimpleSession simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session.getId(), simpleSession.getId());
        redisSessionDAO.delete(simpleSession);
    }

    @Test(priority = 5)
    public void testNullDoReadSession() {
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(null);
        assertNull(session);
    }

    @Test(priority = 6)
    public void testRepeatDoReadSession() {
        SimpleSession simpleSession = generateSimpleSession();
        Serializable serializable = redisSessionDAO.doCreate(simpleSession);
        SimpleSession session = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session.getId(), simpleSession.getId());
        SimpleSession session1 = (SimpleSession) redisSessionDAO.doReadSession(serializable);
        assertEquals(session1.getId(), simpleSession.getId());
        redisSessionDAO.delete(simpleSession);
    }

    @Test(priority = 7)
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

    @Test(expectedExceptions = UnknownSessionException.class, priority = 8)
    public void testExceptionUpdate() {
        SimpleSession simpleSession = generateSimpleSession();
        redisSessionDAO.update(simpleSession);
    }

    @Test(priority = 9)
    public void testUpdate() {
        SimpleSession simpleSession = generateSimpleSession();
        simpleSession.setId(UUID.randomUUID().toString());
        redisSessionDAO.update(simpleSession);
        redisSessionDAO.delete(simpleSession);
    }

    @Test(priority = 10)
    public void testDelete() {
        SimpleSession simpleSession = new SimpleSession();
        redisSessionDAO.delete(simpleSession);
    }

    @Test(priority = 11)
    public void testGetSessionKeyPrefix() {
        redisSessionDAO.setSessionKeyPrefix("  ");
        assertEquals(redisSessionDAO.getSessionKeyPrefix(), RedisSessionDAO.DEFAULT_SESSION_KEY_PREFIX);
    }

    @Test(priority = 12)
    public void testSetSessionKeyPrefix() {
        String sessionKeyPrefix = "shiro:session:test";
        redisSessionDAO.setSessionKeyPrefix(sessionKeyPrefix);
        assertEquals(redisSessionDAO.getSessionKeyPrefix(), sessionKeyPrefix);
    }

    @Test(priority = 13)
    public void testGetExpire() {
        assertEquals(redisSessionDAO.getExpire(), ExpireType.DEFAULT_EXPIRE.type);
    }

    @Test(priority = 14)
    public void testSetExpire() {
        redisSessionDAO.setExpire(ExpireType.NO_EXPIRE);
        assertEquals(redisSessionDAO.getExpire(), ExpireType.NO_EXPIRE.type);
    }

    @Test(priority = 15)
    public void testIsSessionInMemoryEnabled() {
        assertTrue(redisSessionDAO.isSessionInMemoryEnabled());
    }

    @Test(priority = 16)
    public void testSetSessionInMemoryEnabled() {
        redisSessionDAO.setSessionInMemoryEnabled(false);
        assertFalse(redisSessionDAO.isSessionInMemoryEnabled());
    }

    @Test(priority = 17)
    public void testGetSessionInMemoryTimeout() {
        assertEquals(redisSessionDAO.getSessionInMemoryTimeout(), 1000L);
    }

    @Test(priority = 18)
    public void testSetSessionInMemoryTimeout() {
        long timeout = 2000L;
        redisSessionDAO.setSessionInMemoryTimeout(timeout);
        assertEquals(redisSessionDAO.getSessionInMemoryTimeout(), timeout);
    }

    @Test(priority = 19)
    public void testSetCodecType() {
        redisSessionDAO.setCodec(CodecType.LONG_CODEC);
        assertEquals(redisSessionDAO.getCodec(), LongCodec.INSTANCE);
    }

    @Test(priority = 20)
    public void testSetCodec() {
        StringCodec codec = new StringCodec();
        redisSessionDAO.setCodec(codec);
        assertEquals(redisSessionDAO.getCodec(), codec);
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