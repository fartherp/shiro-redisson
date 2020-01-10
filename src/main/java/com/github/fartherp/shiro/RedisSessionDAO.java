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

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
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
import java.util.function.Function;

import static com.github.fartherp.shiro.Constant.DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
import static com.github.fartherp.shiro.Constant.DEFAULT_SESSION_IN_MEMORY_ENABLED;
import static com.github.fartherp.shiro.Constant.DEFAULT_SESSION_KEY_PREFIX;
import static com.github.fartherp.shiro.Constant.MILLISECONDS_NANO;
import static com.github.fartherp.shiro.Constant.SECONDS;

/**
 * shiro session 操作.
 *
 * @author CK
 * @date 2019/1/1
 */
public class RedisSessionDAO extends AbstractSessionDAO {

	/**
	 * session前缀
	 */
    private final String sessionKeyPrefix;

	/**
	 * 过期类型
	 */
	private final int expire;

	/**
	 * 本地缓存启用标志
	 */
    private final boolean sessionInMemoryEnabled;

	/**
	 * 本地缓存超时时间
	 */
	private final long sessionInMemoryTimeout;

	/**
	 * session存储在redis编码
	 */
	private final Codec codec;

	private static final FastThreadLocal<Map<Serializable, SessionWrapper>> SESSIONS_IN_THREAD;

    private final RedisCacheManager redisCacheManager;

	private final RScoredSortedSet<String> sessionKeys;

    private final ClearCache clearCache;

	private final Map<String, Object> lruMap;

	static {
		SESSIONS_IN_THREAD = new FastThreadLocal<>();
	}

	public RedisSessionDAO(RedisCacheManager redisCacheManager) {
		this(redisCacheManager, DEFAULT_SESSION_KEY_PREFIX, ExpireType.DEFAULT_EXPIRE,
			DEFAULT_SESSION_IN_MEMORY_ENABLED, SECONDS, CodecType.FST_CODEC,
			DEFAULT_REDISSON_LRU_OBJ_CAPACITY);
	}

    public RedisSessionDAO(RedisCacheManager redisCacheManager, String sessionKeyPrefix, ExpireType expireType,
		boolean sessionInMemoryEnabled, long sessionInMemoryTimeout, CodecType codecType, int sessionLruSize) {
		Assert.notNull(redisCacheManager, "redisCacheManager is no null");
        this.redisCacheManager = redisCacheManager;
        this.sessionKeyPrefix = StringUtils.hasText(sessionKeyPrefix) ? sessionKeyPrefix : DEFAULT_SESSION_KEY_PREFIX;
        this.expire = expireType != null ? expireType.type : ExpireType.DEFAULT_EXPIRE.type;
        this.sessionInMemoryEnabled = sessionInMemoryEnabled;
        this.sessionInMemoryTimeout = sessionInMemoryTimeout > 0 ? sessionInMemoryTimeout : SECONDS;
        this.codec = codecType != null ? codecType.getCodec() : CodecType.FST_CODEC.getCodec();
		int tmpSessionLruSize = sessionLruSize > 0 ? sessionLruSize : DEFAULT_REDISSON_LRU_OBJ_CAPACITY;
		this.lruMap = new ConcurrentLinkedHashMap.Builder<String, Object>()
			.maximumWeightedCapacity(tmpSessionLruSize).weigher(Weighers.singleton()).build();
		this.sessionKeys = this.redisCacheManager.getRedissonClient()
			.getScoredSortedSet(this.sessionKeyPrefix, CodecType.STRING_CODEC.getCodec());
        this.clearCache = new ClearCache(this);
        this.clearCache.run();
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

	@Override
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

        SessionWrapper sessionWrapper = new SessionWrapper();
        sessionWrapper.setSession(session);

        String key = getRedisSessionKey(sessionWrapper.getId());
        RBucket<SessionWrapper> s = getBucket(key);
		long timestamp;
		if (expire == ExpireType.DEFAULT_EXPIRE.type) {
			long milliSeconds = sessionWrapper.getTimeout();
			s.set(sessionWrapper, milliSeconds, TimeUnit.MILLISECONDS);
			timestamp = LocalDateTimeUtilies
				.getTimestamp(o -> o.plusNanos(milliSeconds * MILLISECONDS_NANO));
		} else if (expire == ExpireType.CUSTOM_EXPIRE.type) {
			long milliSeconds = redisCacheManager.getTtl();
			s.set(sessionWrapper, milliSeconds, TimeUnit.MILLISECONDS);
			timestamp = LocalDateTimeUtilies
				.getTimestamp(o -> o.plusNanos(milliSeconds * MILLISECONDS_NANO));
		} else {
			s.set(sessionWrapper);
			timestamp = Long.MAX_VALUE;
		}
		sessionKeys.add(timestamp, key);
    }

	@Override
    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            return null;
        }
        Session session;
        if (this.sessionInMemoryEnabled && (session = getSessionFromThreadLocal(sessionId)) != null) {
			return session;
        }

        String key = getRedisSessionKey(sessionId);
        RBucket<SessionWrapper> s = getBucket(key);
        session = Optional.ofNullable(s.get()).map(SessionWrapper::getSession).orElse(null);
        if (this.sessionInMemoryEnabled) {
            setSessionToThreadLocal(sessionId, session);
        }
        return session;
    }

	@Override
    public void update(Session session) throws UnknownSessionException {
        this.saveSession(session);
        if (this.sessionInMemoryEnabled) {
            this.setSessionToThreadLocal(session.getId(), session);
        }
    }

	@Override
    public void delete(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }

        String key = getRedisSessionKey(session.getId());

        RBucket<SessionWrapper> s = getBucket(key);
        s.delete();

		sessionKeys.remove(key);
    }

	@Override
    public Collection<Session> getActiveSessions() {
        // 获取存活的session
        List<ScoredEntry<String>> keys = (List<ScoredEntry<String>>) sessionKeys
			.entryRange(System.currentTimeMillis(), false, Double.MAX_VALUE, true);

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
        Map<Serializable, SessionWrapper> sessionMap = SESSIONS_IN_THREAD.get();
        if (sessionMap == null) {
            sessionMap = new HashMap<>(4);
            SESSIONS_IN_THREAD.set(sessionMap);
        }
        SessionWrapper sessionWrapper = new SessionWrapper();
        sessionWrapper.setCreateTime(new Date());
        sessionWrapper.setSession(s);
        sessionMap.put(sessionId, sessionWrapper);
    }

    private Session getSessionFromThreadLocal(Serializable sessionId) {
        Map<Serializable, SessionWrapper> sessionMap = SESSIONS_IN_THREAD.get();
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
