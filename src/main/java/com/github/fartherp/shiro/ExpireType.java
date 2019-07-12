/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

/**
 * Created by IntelliJ IDEA.
 * @author CK
 * @date 2019/1/14
 */
public enum ExpireType {
    DEFAULT_EXPIRE(-2),
    NO_EXPIRE(-1),
    ;
    public int type;

    ExpireType(int type) {
        this.type = type;
    }
}
