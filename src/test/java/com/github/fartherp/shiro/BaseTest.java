/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.session.mgt.SimpleSession;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.util.Date;

/**
 * <pre>
 *  @author: cuiyuqiang
 *  @email: cuiyuqiang@ddjf.com.cn
 *  @date: 2019/6/19 10:30
 *  @project: risk-control-parent
 * </pre>
 */
public class BaseTest {

    public final Config config = new Config();

    public final RedissonClient redissonClient;

    {
        config.setCodec(new StringCodec()).useSingleServer().setAddress("redis://127.0.0.1:6379");
        redissonClient = Redisson.create(config);
    }

    public final RedisCacheManager redisCacheManager = new RedisCacheManager(redissonClient);

    public SimpleSession generateSimpleSession() {
        SimpleSession simpleSession = new SimpleSession();
        simpleSession.setStartTimestamp(new Date());
        simpleSession.setLastAccessTime(new Date());
        simpleSession.setTimeout(1000 * 60 * 30);
        simpleSession.setHost("127.0.0.1");
        simpleSession.setStopTimestamp(new Date());
        simpleSession.setExpired(true);
        return simpleSession;
    }
}
