/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: CK
 * @date: 2019/1/1
 */
public class RedisCacheTest {
    @Test
    public void main() throws Exception {
        User user = new User();
        Method pincipalIdGetter = user.getClass().getMethod("getId");
        assertNotNull(pincipalIdGetter);
    }

    public static class UserBase {
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    public static class User extends UserBase {

    }
}