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

import org.apache.shiro.session.mgt.SimpleSession;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.Serializable;
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

	@BeforeMethod
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

	public static class UserBase implements Serializable {
		private Integer id;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
	}

	public static class User extends RedisCacheTest.UserBase implements ShiroFieldAccess {

		@Override
		public String unique() {
			return getId().toString();
		}
	}
}
