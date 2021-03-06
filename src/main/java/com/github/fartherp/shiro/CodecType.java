/**
 *    Copyright (c) 2019 CK.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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

/**
 * Redisson编码.
 *
 * @author CK
 * @date 2019/1/31
 */
public enum CodecType {
	/**
	 * fst编码
	 */
    FST_CODEC("default") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new FstCodec(classLoader);
		}
	},
	/**
	 * json编码
	 */
    JSON_JACKSON_CODEC("default") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new JsonJacksonCodec(classLoader);
		}
	},
    SERIALIZATION_CODEC("default") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new SerializationCodec(classLoader);
		}
	},
    SNAPPY_CODEC("default") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new SnappyCodec(classLoader);
		}
	},
    SNAPPY_CODEC_V_2("default") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new SnappyCodecV2(classLoader);
		}
	},
    STRING_CODEC("default") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new StringCodec(classLoader);
		}
	},
    LONG_CODEC("default") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return LongCodec.INSTANCE;
		}
	},
    BYTE_ARRAY_CODEC("default") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return ByteArrayCodec.INSTANCE;
		}
	},

    AVRO_JACKSON_CODEC("com.fasterxml.jackson.dataformat:jackson-dataformat-avro") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new AvroJacksonCodec(classLoader);
		}
	},
    SMILE_JACKSON_CODEC("com.fasterxml.jackson.dataformat:jackson-dataformat-smile") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new SmileJacksonCodec(classLoader);
		}
	},
    CBOR_JACKSON_CODEC("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new CborJacksonCodec(classLoader);
		}
	},
    ION_JACKSON_CODEC("com.fasterxml.jackson.dataformat:jackson-dataformat-ion") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new IonJacksonCodec(classLoader);
		}
	},
    KRYO_CODEC("com.esotericsoftware:kryo") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new KryoCodec(classLoader);
		}
	},
    MSG_PACK_JACKSON_CODEC("org.msgpack:jackson-dataformat-msgpack") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new MsgPackJacksonCodec(classLoader);
		}
	},
    LZ_4_CODEC("net.jpountz.lz4:lz4") {
		@Override
		public Codec getCodec(ClassLoader classLoader) {
			return new LZ4Codec(classLoader);
		}
	};

    String dependencyPackageName;

    CodecType(String dependencyPackageName) {
        this.dependencyPackageName = dependencyPackageName;
    }

    public Codec getCodec() {
        try {
            return getCodec(this.getClass().getClassLoader());
        } catch (Exception e) {
            return JSON_JACKSON_CODEC.getCodec();
        }
    }

    protected abstract Codec getCodec(ClassLoader classLoader);
}
