/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import io.netty.util.concurrent.FastThreadLocal;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.ScoredEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: CK
 * @date: 2019/1/1
 */
public class RedisSessionDAO extends AbstractSessionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSessionDAO.class);

    private static final String DEFAULT_SESSION_KEY_PREFIX = "shiro:session";
    private String sessionKeyPrefix = DEFAULT_SESSION_KEY_PREFIX;

    private int expire = ExpireType.DEFAULT_EXPIRE.type;

    private static final boolean DEFAULT_SESSION_IN_MEMORY_ENABLED = true;

    private boolean sessionInMemoryEnabled = DEFAULT_SESSION_IN_MEMORY_ENABLED;

    private static final long DEFAULT_SESSION_IN_MEMORY_TIMEOUT = 1000L;

    private long sessionInMemoryTimeout = DEFAULT_SESSION_IN_MEMORY_TIMEOUT;

    public static final FastThreadLocal<Map<Serializable, SessionWrapper>> sessionsInThread = new FastThreadLocal<>();

    private RedisCacheManager redisCacheManager;

    private Codec codec = CodecType.FST_CODEC.getCodec();

    private RScoredSortedSet<String> sessionKeys;

    private ClearCache clearCache;

    public RedisSessionDAO(RedisCacheManager redisCacheManager) {
        this.redisCacheManager = redisCacheManager;
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

        SessionWrapper sessionWrapper = new SessionWrapper();
        sessionWrapper.setSession(session);

        String key = getRedisSessionKey(sessionWrapper.getId());
        RBucket<SessionWrapper> s = redisCacheManager.getRedissonClient().getBucket(key, codec);
        long timestamp;
        if (expire == ExpireType.DEFAULT_EXPIRE.type) {
            s.set(sessionWrapper, sessionWrapper.getTimeout(), TimeUnit.MILLISECONDS);
            timestamp = LocalDateTimeUtilies.getTimestamp(o -> o.plusSeconds(sessionWrapper.getTimeout() / 1000));
        } else {
            s.set(sessionWrapper, redisCacheManager.getTtl(), TimeUnit.MINUTES);
            timestamp = LocalDateTimeUtilies.getTimestamp(o -> o.plusMinutes(redisCacheManager.getTtl()));
        }
        sessionKeys.add(timestamp, key);
    }

    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            return null;
        }
        Session session;
        if (this.sessionInMemoryEnabled) {
            session = getSessionFromThreadLocal(sessionId);
            if (session != null) {
                return session;
            }
        }
        Assert.notNull(redisCacheManager, "redisCacheManager is no null");

        String key = getRedisSessionKey(sessionId);
        RBucket<SessionWrapper> s = redisCacheManager.getRedissonClient().getBucket(key, codec);
        session = Optional.ofNullable(s.get()).map(SessionWrapper::getSession).orElse(null);
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

        RBucket<SessionWrapper> s = redisCacheManager.getRedissonClient().getBucket(getRedisSessionKey(session.getId()), codec);
        s.delete();

        String key = getRedisSessionKey(session.getId());
        sessionKeys.remove(key);
    }

    public Collection<Session> getActiveSessions() {
        Assert.notNull(redisCacheManager, "redisCacheManager is no null");

        // 获取存活的session
        List<ScoredEntry<String>> keys = (List<ScoredEntry<String>>) sessionKeys.entryRange(System.currentTimeMillis(), false, Double.MAX_VALUE, true);

        List<Session> values = new ArrayList<>(keys.size());
        for (ScoredEntry<String> key : keys) {
            RBucket<SessionWrapper> v = redisCacheManager.getRedissonClient().getBucket(getRedisSessionKey(key.getValue()), codec);
            SessionWrapper sessionWrapper = v.get();
            if (sessionWrapper != null) {
                values.add(sessionWrapper.getSession());
            }
        }
        return Collections.unmodifiableList(values);
    }

    private void setSessionToThreadLocal(Serializable sessionId, Session s) {
        Map<Serializable, SessionWrapper> sessionMap = sessionsInThread.get();
        if (sessionMap == null) {
            sessionMap = new HashMap<>();
            sessionsInThread.set(sessionMap);
        }
        SessionWrapper sessionWrapper = new SessionWrapper();
        sessionWrapper.setCreateTime(new Date());
        sessionWrapper.setSession(s);
        sessionMap.put(sessionId, sessionWrapper);
    }

    private Session getSessionFromThreadLocal(Serializable sessionId) {
        Map<Serializable, SessionWrapper> sessionMap = sessionsInThread.get();
        if (sessionMap == null) {
            return null;
        }
        SessionWrapper sessionWrapper = sessionMap.get(sessionId);
        if (sessionWrapper == null) {
            return null;
        }
        long duration = System.currentTimeMillis() - sessionWrapper.getCreateTime().getTime();
        Session s = null;
        if (duration < sessionInMemoryTimeout) {
            s = sessionWrapper.getSession();
        } else {
            sessionMap.remove(sessionId);
        }

        return s;
    }

    public String getSessionKeyPrefix() {
        return sessionKeyPrefix;
    }

    public void setSessionKeyPrefix(String sessionKeyPrefix) {
        if (StringUtils.hasText(sessionKeyPrefix)) {
            this.sessionKeyPrefix = sessionKeyPrefix;
        }
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(ExpireType expireType) {
        if (expireType != null) {
            this.expire = expireType.type;
        }
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

    public void setCodec(CodecType codecType) {
        if (codecType != null) {
            Codec codec = codecType.getCodec();
            if (codec != null) {
                this.codec = codec;
            } else {
                LOGGER.info("Lack of dependency packages is {}, use default codec", codecType.dependencyPackageName);
            }
        }
    }

    public void setCodec(Codec codec) {
        if (codec != null) {
            this.codec = codec;
        }
    }

    public RedisCacheManager getRedisCacheManager() {
        return redisCacheManager;
    }

    public RScoredSortedSet<String> getSessionKeys() {
        return sessionKeys;
    }
}
