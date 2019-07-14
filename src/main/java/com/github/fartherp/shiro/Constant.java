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

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/7/11
 */
public interface Constant {

	/**
	 * 一秒
	 */
	long SECONDS = 1000L;

	/**
	 * 一分钟
	 */
	long MINUTE = 60 * SECONDS;

	/**
	 * 30分钟
	 */
	long THIRTY_MINUTES = 30 * MINUTE;

	String DEFAULT_CACHE_KEY_PREFIX = "shiro:cache:";

	String DEFAULT_PRINCIPAL_ID_FIELD_NAME = "id";

	int DEFAULT_REDISSON_LRU_OBJ_CAPACITY = 1024;

	String DEFAULT_SESSION_KEY_PREFIX = "shiro:session";

	boolean DEFAULT_SESSION_IN_MEMORY_ENABLED = true;

	long DEFAULT_SESSION_IN_MEMORY_TIMEOUT = 1000L;
}
