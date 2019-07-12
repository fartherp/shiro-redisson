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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.github.fartherp.shiro.Constant.DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
import static com.github.fartherp.shiro.Constant.DEFAULT_SESSION_IN_MEMORY_ENABLED;
import static com.github.fartherp.shiro.Constant.DEFAULT_SESSION_IN_MEMORY_TIMEOUT;
import static com.github.fartherp.shiro.Constant.DEFAULT_SESSION_KEY_PREFIX;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/1/1
 */
public class RedisSessionDAO extends AbstractSessionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSessionDAO.class);

    private final String sessionKeyPrefix;

    private final int expire;

    private final boolean sessionInMemoryEnabled;

    private final long sessionInMemoryTimeout;

    private final Codec codec;

	public static final FastThreadLocal<Map<Serializable, SessionWrapper>> sessionsInThread = new FastThreadLocal<>();

    private final RedisCacheManager redisCacheManager;

    private final RScoredSortedSet<String> sessionKeys;

    private final ClearCache clearCache;

	private final Map<String, Object> lruMap;

	public RedisSessionDAO(RedisCacheManager redisCacheManager) {
		this(redisCacheManager, DEFAULT_SESSION_KEY_PREFIX, ExpireType.DEFAULT_EXPIRE.type,
			DEFAULT_SESSION_IN_MEMORY_ENABLED, DEFAULT_SESSION_IN_MEMORY_TIMEOUT,
			CodecType.FST_CODEC.getCodec(), DEFAULT_REDISSON_LRU_OBJ_CAPACITY);
	}

    public RedisSessionDAO(RedisCacheManager redisCacheManager, String sessionKeyPrefix, int expire,
						   boolean sessionInMemoryEnabled, long sessionInMemoryTimeout, Codec codec, int sessionLruSize) {
        this.redisCacheManager = redisCacheManager;
        this.sessionKeyPrefix = StringUtils.hasText(sessionKeyPrefix) ? sessionKeyPrefix : DEFAULT_SESSION_KEY_PREFIX;
        this.expire = expire > 0 ? expire : ExpireType.DEFAULT_EXPIRE.type;
        this.sessionInMemoryEnabled = sessionInMemoryEnabled;
        this.sessionInMemoryTimeout = sessionInMemoryTimeout > 0 ? sessionInMemoryTimeout : DEFAULT_SESSION_IN_MEMORY_TIMEOUT;
        this.codec = codec != null ? codec : CodecType.FST_CODEC.getCodec();
		int tmpSessionLruSize = sessionLruSize > 0 ? sessionLruSize : DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
		this.lruMap = new LinkedHashMap<String, Object>(tmpSessionLruSize, 0.75F, true) {
			private static final long serialVersionUID = -7936195607152909097L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
				return size() > tmpSessionLruSize;
			}
		};
        this.sessionKeys = this.redisCacheManager.getRedissonClient().getScoredSortedSet(sessionKeyPrefix);
        this.clearCache = new ClearCache(this);
        this.clearCache.init();
    }

	@SuppressWarnings("unchecked")
	private <T> T convertLruMap(String key, Function<String, T> function) {
		return (T) lruMap.computeIfAbsent(key, function);
	}

	private <T> RBucket<T> getBucket(String redisSessionKey) {
		return convertLruMap(redisSessionKey, k -> redisCacheManager.getRedissonClient().getBucket(k, codec));
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
        RBucket<SessionWrapper> s = getBucket(key);
        long seconds = expire == ExpireType.DEFAULT_EXPIRE.type ? sessionWrapper.getTimeout() / 1000 : redisCacheManager.getTtl();
        s.set(sessionWrapper, seconds, TimeUnit.SECONDS);
        long timestamp = LocalDateTimeUtilies.getTimestamp(o -> o.plusSeconds(seconds));
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
        RBucket<SessionWrapper> s = getBucket(key);
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

        RBucket<SessionWrapper> s = getBucket(getRedisSessionKey(session.getId()));
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
            RBucket<SessionWrapper> v = getBucket(key.getValue());
            SessionWrapper sessionWrapper = v.get();
            if (sessionWrapper != null) {
                values.add(sessionWrapper.getSession());
            }
        }
        return Collections.unmodifiableList(values);
    }

    private void setSessionToThreadLocal(Serializable sessionId, Session s) {
    	if (s == null) {
    		return;
		}
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

    public int getExpire() {
        return expire;
    }

    public boolean isSessionInMemoryEnabled() {
        return sessionInMemoryEnabled;
    }

    public long getSessionInMemoryTimeout() {
        return sessionInMemoryTimeout;
    }

    public RedisCacheManager getRedisCacheManager() {
        return redisCacheManager;
    }

    public RScoredSortedSet<String> getSessionKeys() {
        return sessionKeys;
    }
}
