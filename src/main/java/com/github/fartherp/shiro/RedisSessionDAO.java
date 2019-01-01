/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import io.netty.util.concurrent.FastThreadLocal;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.util.Assert;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.codec.SerializationCodec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: CK
 * @date: 2019/1/1
 */
public class RedisSessionDAO extends AbstractSessionDAO {

    private static final String DEFAULT_SESSION_KEY_PREFIX = "shiro:session";
    private String sessionKeyPrefix = DEFAULT_SESSION_KEY_PREFIX;

    private static final int DEFAULT_EXPIRE = -2;

    private static final int NO_EXPIRE = -1;

    private int expire = DEFAULT_EXPIRE;

    private static final boolean DEFAULT_SESSION_IN_MEMORY_ENABLED = true;

    private boolean sessionInMemoryEnabled = DEFAULT_SESSION_IN_MEMORY_ENABLED;

    private static final long DEFAULT_SESSION_IN_MEMORY_TIMEOUT = 1000L;

    private long sessionInMemoryTimeout = DEFAULT_SESSION_IN_MEMORY_TIMEOUT;

    public static final FastThreadLocal<Map<Serializable, SessionInMemory>> sessionsInThread = new FastThreadLocal<>();

    private RedisCacheManager redisCacheManager;

    private SerializationCodec serializationCodec = new SerializationCodec();

    private String getRedisSessionKey(Serializable sessionId) {
        return this.sessionKeyPrefix + ":" + sessionId;
    }

    protected Serializable doCreate(Session session) {
        if (session == null) {
            throw new UnknownSessionException("session is null");
        }
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        this.saveSession(session);
        return sessionId;
    }

    private void saveSession(Session session) throws UnknownSessionException {
        if (session == null || session.getId() == null) {
            throw new UnknownSessionException("session or session id is null");
        }
        Assert.notNull(redisCacheManager, "redisCacheManager is no null");

        String key = getRedisSessionKey(session.getId());
        RBucket<Session> s = redisCacheManager.getRedissonClient().getBucket(key, serializationCodec);
        if (expire == DEFAULT_EXPIRE) {
            s.set(session, session.getTimeout(), TimeUnit.MILLISECONDS);
        } else {
            s.set(session);
        }
        RSet<String> keys = redisCacheManager.getRedissonClient().getSet(sessionKeyPrefix);
        keys.add(key);
    }

    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            return null;
        }
        Session session = null;
        if (this.sessionInMemoryEnabled) {
            session = getSessionFromThreadLocal(sessionId);
            if (session != null) {
                return session;
            }
        }
        Assert.notNull(redisCacheManager, "redisCacheManager is no null");

        String key = getRedisSessionKey(sessionId);
        RBucket<Session> s = redisCacheManager.getRedissonClient().getBucket(key, serializationCodec);
        session = s.get();
        if (this.sessionInMemoryEnabled) {
            setSessionToThreadLocal(sessionId, session);
        }
        return session;
    }

    public void update(Session session) throws UnknownSessionException {
        this.saveSession(session);
        if (this.sessionInMemoryEnabled) {
            this.setSessionToThreadLocal(session.getId(), session);
        }
    }

    public void delete(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }
        Assert.notNull(redisCacheManager, "redisCacheManager is no null");

        RBucket<Session> s = redisCacheManager.getRedissonClient().getBucket(getRedisSessionKey(session.getId()), serializationCodec);
        s.delete();

        RSet<String> keys = redisCacheManager.getRedissonClient().getSet(sessionKeyPrefix);
        String key = getRedisSessionKey(session.getId());
        keys.remove(key);
    }

    public Collection<Session> getActiveSessions() {
        Assert.notNull(redisCacheManager, "redisCacheManager is no null");

        RSet<String> keys = redisCacheManager.getRedissonClient().getSet(sessionKeyPrefix);
        List<Session> values = new ArrayList<>(keys.size());
        for (String key : keys) {
            RBucket<Session> v = redisCacheManager.getRedissonClient().getBucket(getRedisSessionKey(key), serializationCodec);
            values.add(v.get());
        }
        return Collections.unmodifiableList(values);
    }

    private void setSessionToThreadLocal(Serializable sessionId, Session s) {
        Map<Serializable, SessionInMemory> sessionMap = sessionsInThread.get();
        if (sessionMap == null) {
            sessionMap = new HashMap<>();
            sessionsInThread.set(sessionMap);
        }
        SessionInMemory sessionInMemory = new SessionInMemory();
        sessionInMemory.setCreateTime(new Date());
        sessionInMemory.setSession(s);
        sessionMap.put(sessionId, sessionInMemory);
    }

    private Session getSessionFromThreadLocal(Serializable sessionId) {
        Map<Serializable, SessionInMemory> sessionMap = sessionsInThread.get();
        if (sessionMap == null) {
            return null;
        }
        SessionInMemory sessionInMemory = sessionMap.get(sessionId);
        if (sessionInMemory == null) {
            return null;
        }
        Date now = new Date();
        long duration = now.getTime() - sessionInMemory.getCreateTime().getTime();
        Session s = null;
        if (duration < sessionInMemoryTimeout) {
            s = sessionInMemory.getSession();
        } else {
            sessionMap.remove(sessionId);
        }

        return s;
    }

    public String getSessionKeyPrefix() {
        return sessionKeyPrefix;
    }

    public void setSessionKeyPrefix(String sessionKeyPrefix) {
        this.sessionKeyPrefix = sessionKeyPrefix;
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public boolean isSessionInMemoryEnabled() {
        return sessionInMemoryEnabled;
    }

    public void setSessionInMemoryEnabled(boolean sessionInMemoryEnabled) {
        this.sessionInMemoryEnabled = sessionInMemoryEnabled;
    }

    public long getSessionInMemoryTimeout() {
        return sessionInMemoryTimeout;
    }

    public void setSessionInMemoryTimeout(long sessionInMemoryTimeout) {
        this.sessionInMemoryTimeout = sessionInMemoryTimeout;
    }

    public RedisCacheManager getRedisCacheManager() {
        return redisCacheManager;
    }

    public void setRedisCacheManager(RedisCacheManager redisCacheManager) {
        this.redisCacheManager = redisCacheManager;
    }
}
