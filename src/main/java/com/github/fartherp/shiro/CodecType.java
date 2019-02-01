/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.AvroJacksonCodec;
import org.redisson.codec.CborJacksonCodec;
import org.redisson.codec.FstCodec;
import org.redisson.codec.IonJacksonCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.KryoCodec;
import org.redisson.codec.LZ4Codec;
import org.redisson.codec.MsgPackJacksonCodec;
import org.redisson.codec.SerializationCodec;
import org.redisson.codec.SmileJacksonCodec;
import org.redisson.codec.SnappyCodec;
import org.redisson.codec.SnappyCodecV2;

import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * Author: CK
 * Date: 2019/1/31
 */
public enum CodecType {
    FST_CODEC(FstCodec::new, "default"),
    JSON_JACKSON_CODEC(JsonJacksonCodec::new, "default"),
    SERIALIZATION_CODEC(SerializationCodec::new, "default"),
    SNAPPY_CODEC(SnappyCodec::new, "default"),
    SNAPPY_CODEC_V_2(SnappyCodecV2::new, "default"),
    STRING_CODEC(StringCodec::new, "default"),
    LONG_CODEC(o -> LongCodec.INSTANCE, "default"),
    BYTE_ARRAY_CODEC(o -> ByteArrayCodec.INSTANCE, "default"),

    AVRO_JACKSON_CODEC(AvroJacksonCodec::new, "com.fasterxml.jackson.dataformat:jackson-dataformat-avro"),
    SMILE_JACKSON_CODEC(SmileJacksonCodec::new, "com.fasterxml.jackson.dataformat:jackson-dataformat-smile"),
    CBOR_JACKSON_CODEC(CborJacksonCodec::new, "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor"),
    ION_JACKSON_CODEC(IonJacksonCodec::new, "com.fasterxml.jackson.dataformat:jackson-dataformat-ion"),
    KRYO_CODEC(KryoCodec::new, "com.esotericsoftware:kryo"),
    MSG_PACK_JACKSON_CODEC(MsgPackJacksonCodec::new, "org.msgpack:jackson-dataformat-msgpack"),
    LZ_4_CODEC(LZ4Codec::new, "net.jpountz.lz4:lz4"),
    ;
    public Codec codec;
    public Throwable throwable;
    public String dependencyPackageName;

    CodecType(Function<ClassLoader, Codec> function, String dependencyPackageName) {
        try {
            this.codec = function.apply(this.getClass().getClassLoader());
        } catch (Throwable e) {
            this.throwable = e;
        }
        this.dependencyPackageName = dependencyPackageName;
    }
}
