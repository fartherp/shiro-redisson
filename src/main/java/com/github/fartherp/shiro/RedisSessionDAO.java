/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import com.github.fartherp.framework.common.util.DateUtil;
import io.netty.util.concurrent.FastThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.util.Assert;
import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.codec.SerializationCodec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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

    private int expire = ExpireType.DEFAULT_EXPIRE.type;

    private static final boolean DEFAULT_SESSION_IN_MEMORY_ENABLED = true;

    private boolean sessionInMemoryEnabled = DEFAULT_SESSION_IN_MEMORY_ENABLED;

    private static final long DEFAULT_SESSION_IN_MEMORY_TIMEOUT = 1000L;

    private long sessionInMemoryTimeout = DEFAULT_SESSION_IN_MEMORY_TIMEOUT;

    public static final FastThreadLocal<Map<Serializable, SessionInMemory>> sessionsInThread = new FastThreadLocal<>();

    private RedisCacheManager redisCacheManager;

    private SerializationCodec serializationCodec;

    private RScoredSortedSet<String> sessionKeys;

    private ClearCache clearCache;

    public RedisSessionDAO(RedisCacheManager redisCacheManager) {
        this.redisCacheManager = redisCacheManager;
        this.serializationCodec = new SerializationCodec(this.getClass().getClassLoader());
        this.sessionKeys = this.redisCacheManager.getRedissonClient().getScoredSortedSet(sessionKeyPrefix);
        this.clearCache = new ClearCache(this);
        this.clearCache.init();
    }

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
        Date date;
        if (expire == ExpireType.DEFAULT_EXPIRE.type) {
            s.set(session, session.getTimeout(), TimeUnit.MILLISECONDS);
            date = DateUtil.getDateByCalendar(new Date(), Calendar.MILLISECOND, (int) session.getTimeout());
        } else {
            s.set(session, redisCacheManager.getTtl(), TimeUnit.MINUTES);
            date = DateUtil.getDateByCalendar(new Date(), Calendar.MINUTE, (int) redisCacheManager.getTtl());
        }
        sessionKeys.add(date.getTime(), key);
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

        String key = getRedisSessionKey(session.getId());
        sessionKeys.remove(key);
    }

    public Collection<Session> getActiveSessions() {
        Assert.notNull(redisCacheManager, "redisCacheManager is no null");

        // 获取存活的session
        List<ScoredEntry<String>> keys = (List<ScoredEntry<String>>) sessionKeys.entryRange(new Date().getTime(), false, Double.MAX_VALUE, true);

        List<Session> values = new ArrayList<>(keys.size());
        for (ScoredEntry<String> key : keys) {
            RBucket<Session> v = redisCacheManager.getRedissonClient().getBucket(getRedisSessionKey(key.getValue()), serializationCodec);
            Session session = v.get();
            if (session != null) {
                values.add(session);
            }
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
        if (StringUtils.isNotBlank(sessionKeyPrefix)) {
            this.sessionKeyPrefix = sessionKeyPrefix;
        }
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(ExpireType expireType) {
        this.expire = expireType.type;
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
        if (sessionInMemoryTimeout > 0) {
            this.sessionInMemoryTimeout = sessionInMemoryTimeout;
        }
    }

    public RedisCacheManager getRedisCacheManager() {
        return redisCacheManager;
    }

    public RScoredSortedSet<String> getSessionKeys() {
        return sessionKeys;
    }
}
