/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
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
    public void testFstGetCodec() {
        Codec codec = CodecType.FST_CODEC.getCodec();
        assertNotNull(codec);
    }

    @Test
    public void testLongGetCodec() {
        Codec codec = CodecType.LONG_CODEC.getCodec();
        assertEquals(codec, LongCodec.INSTANCE);
    }

    @Test
    public void testByteArrayGetCodec() {
        Codec codec = CodecType.BYTE_ARRAY_CODEC.getCodec();
        assertEquals(codec, ByteArrayCodec.INSTANCE);
    }
}