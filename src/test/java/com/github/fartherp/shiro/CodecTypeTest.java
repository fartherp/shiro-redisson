/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.redisson.client.codec.Codec;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <pre>
 *  @author: cuiyuqiang
 *  @email: cuiyuqiang@ddjf.com.cn
 *  @date: 2019/6/18 17:25
 *  @project: risk-control-parent
 * </pre>
 */
public class CodecTypeTest {

    @Test
    public void testGetCodec() {
        Codec codec = CodecType.FST_CODEC.getCodec();
        assertNotNull(codec);
    }
}