/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import java.lang.reflect.Method;

import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: CK
 * @date: 2019/1/1
 */
public class RedisCacheTest {
    public static void main(String[] args) throws Exception {
        User user = new User();
        Method pincipalIdGetter = user.getClass().getMethod("getId");
        System.out.println(pincipalIdGetter != null);
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