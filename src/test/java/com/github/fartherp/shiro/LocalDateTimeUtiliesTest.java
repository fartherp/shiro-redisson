/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * Author: CK
 * Date: 2019/1/25
 */
public class LocalDateTimeUtiliesTest {

    @Test
    public void testGetTimestamp() {
        long s = LocalDateTimeUtilies.getTimestamp(o -> o.plusMinutes(5));
        System.out.println(s);
    }
}