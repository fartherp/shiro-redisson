/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro.exception;

public class CacheManagerPrincipalIdNotAssignedException extends RuntimeException  {

    private static final String MESSAGE = "CacheManager didn't assign Principal Id field name!";

    public CacheManagerPrincipalIdNotAssignedException() {
        super(MESSAGE);
    }
}
