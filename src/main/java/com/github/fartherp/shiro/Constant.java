/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/7/11
 */
public interface Constant {
	long MINUTE = 1000 * 60;

	long THIRTY_MINUTES = 30 * MINUTE;

	String DEFAULT_CACHE_KEY_PREFIX = "shiro:cache:";

	String DEFAULT_PRINCIPAL_ID_FIELD_NAME = "id";

	int DEFAULT_REDISSON_LRU_OBJ_CAPACITY = 1024;

	String DEFAULT_SESSION_KEY_PREFIX = "shiro:session";

	boolean DEFAULT_SESSION_IN_MEMORY_ENABLED = true;

	long DEFAULT_SESSION_IN_MEMORY_TIMEOUT = 1000L;
}
