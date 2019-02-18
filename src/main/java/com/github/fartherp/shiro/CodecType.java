/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.util.ClassUtils;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;

import java.lang.reflect.Constructor;

/**
 * Created by IntelliJ IDEA.
 * Author: CK
 * Date: 2019/1/31
 */
public enum CodecType {
    FST_CODEC("org.redisson.codec.FstCodec", "default"),
    JSON_JACKSON_CODEC("org.redisson.codec.JsonJacksonCodec", "default"),
    SERIALIZATION_CODEC("org.redisson.codec.SerializationCodec", "default"),
    SNAPPY_CODEC("org.redisson.codec.SnappyCodec", "default"),
    SNAPPY_CODEC_V_2("org.redisson.codec.SnappyCodecV2", "default"),
    STRING_CODEC("org.redisson.client.codec.StringCodec", "default"),
    LONG_CODEC("org.redisson.client.codec.LongCodec", "default"),
    BYTE_ARRAY_CODEC("org.redisson.client.codec.ByteArrayCodec", "default"),

    AVRO_JACKSON_CODEC("org.redisson.codec.AvroJacksonCodec", "com.fasterxml.jackson.dataformat:jackson-dataformat-avro"),
    SMILE_JACKSON_CODEC("org.redisson.codec.SmileJacksonCodec", "com.fasterxml.jackson.dataformat:jackson-dataformat-smile"),
    CBOR_JACKSON_CODEC("org.redisson.codec.CborJacksonCodec", "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor"),
    ION_JACKSON_CODEC("org.redisson.codec.IonJacksonCodec", "com.fasterxml.jackson.dataformat:jackson-dataformat-ion"),
    KRYO_CODEC("org.redisson.codec.KryoCodec", "com.esotericsoftware:kryo"),
    MSG_PACK_JACKSON_CODEC("org.redisson.codec.MsgPackJacksonCodec", "org.msgpack:jackson-dataformat-msgpack"),
    LZ_4_CODEC("org.redisson.codec.LZ4Codec", "net.jpountz.lz4:lz4"),
    ;

    public String className;
    public String dependencyPackageName;

    CodecType(String className, String dependencyPackageName) {
        this.className = className;
        this.dependencyPackageName = dependencyPackageName;
    }

    public Codec getCodec() {
        try {
            if ("org.redisson.client.codec.LongCodec".equals(this.className)) {
                return LongCodec.INSTANCE;
            } else if  ("org.redisson.client.codec.ByteArrayCodec".equals(this.className)) {
                return ByteArrayCodec.INSTANCE;
            }
            Class clazz = ClassUtils.forName(this.className);
            Constructor constructor = ClassUtils.getConstructor(clazz, ClassLoader.class);
            return (Codec) ClassUtils.instantiate(constructor, this.getClass().getClassLoader());
        } catch (Throwable e) {
            return null;
        }
    }
}
