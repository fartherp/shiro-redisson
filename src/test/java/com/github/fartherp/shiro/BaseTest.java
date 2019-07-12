/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.session.mgt.SimpleSession;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.testng.annotations.AfterMethod;

import java.util.Date;

import static com.github.fartherp.shiro.Constant.THIRTY_MINUTES;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/6/19
 */
public abstract class BaseTest {

    public RedissonClient redissonClient;

    public RedisCacheManager redisCacheManager;

	public void setUp() {
		Config config = new Config();
		config.setCodec(new StringCodec()).useSingleServer().setAddress("redis://127.0.0.1:6379");
		redissonClient = Redisson.create(config);
		redisCacheManager = new RedisCacheManager(redissonClient);
	}

	@AfterMethod
	public void tearDown() {
		if (redissonClient != null && !redissonClient.isShutdown()) {
			redissonClient.shutdown();
		}
	}

	public SimpleSession generateSimpleSession() {
        SimpleSession simpleSession = new SimpleSession();
        simpleSession.setStartTimestamp(new Date());
        simpleSession.setLastAccessTime(new Date());
        simpleSession.setTimeout(THIRTY_MINUTES);
        simpleSession.setHost("127.0.0.1");
        simpleSession.setStopTimestamp(new Date());
        simpleSession.setExpired(true);
		simpleSession.setAttribute("simpleSession", "simpleSession");
        return simpleSession;
    }
}
