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
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/6/18
 */
public class CodecTypeTest {

    @Test
    public void testFstCodecGetCodec() {
		CodecType fst = CodecType.FST_CODEC;
        Codec codec = fst.getCodec();
		assertTrue(codec instanceof FstCodec);
		assertEquals(fst.dependencyPackageName, "default");
    }

	@Test
	public void testJsonJacksonCodecGetCodec() {
		Codec codec = CodecType.JSON_JACKSON_CODEC.getCodec();
		assertTrue(codec instanceof JsonJacksonCodec);
	}

	@Test
	public void testSerializationCodecGetCodec() {
		Codec codec = CodecType.SERIALIZATION_CODEC.getCodec();
		assertTrue(codec instanceof SerializationCodec);
	}

	@Test
	public void testSnappyCodecGetCodec() {
		Codec codec = CodecType.SNAPPY_CODEC.getCodec();
		assertTrue(codec instanceof SnappyCodec);
	}

	@Test
	public void testSnappyCodecV2GetCodec() {
		Codec codec = CodecType.SNAPPY_CODEC_V_2.getCodec();
		assertTrue(codec instanceof SnappyCodecV2);
	}

	@Test
	public void testStringCodecGetCodec() {
		Codec codec = CodecType.STRING_CODEC.getCodec();
		assertTrue(codec instanceof StringCodec);
	}

    @Test
    public void testLongCodecGetCodec() {
        Codec codec = CodecType.LONG_CODEC.getCodec();
        assertEquals(codec, LongCodec.INSTANCE);
    }

    @Test
    public void testByteArrayCodecGetCodec() {
        Codec codec = CodecType.BYTE_ARRAY_CODEC.getCodec();
        assertEquals(codec, ByteArrayCodec.INSTANCE);
    }

	@Test
	public void testAvroJacksonCodecGetCodec() {
		Codec codec = CodecType.AVRO_JACKSON_CODEC.getCodec();
		assertTrue(codec instanceof AvroJacksonCodec);
	}

	@Test
	public void testSmileJacksonCodecGetCodec() {
		Codec codec = CodecType.SMILE_JACKSON_CODEC.getCodec();
		assertTrue(codec instanceof SmileJacksonCodec);
	}

	@Test
	public void testCborJacksonCodecGetCodec() {
		Codec codec = CodecType.CBOR_JACKSON_CODEC.getCodec();
		assertTrue(codec instanceof CborJacksonCodec);
	}

	@Test
	public void testIonJacksonCodecGetCodec() {
		Codec codec = CodecType.ION_JACKSON_CODEC.getCodec();
		assertTrue(codec instanceof IonJacksonCodec);
	}

	@Test
	public void testKryoCodecGetCodec() {
		Codec codec = CodecType.KRYO_CODEC.getCodec();
		assertTrue(codec instanceof KryoCodec);
	}

	@Test
	public void testMsgPackJacksonCodecGetCodec() {
		Codec codec = CodecType.MSG_PACK_JACKSON_CODEC.getCodec();
		assertTrue(codec instanceof MsgPackJacksonCodec);
	}

	@Test
	public void testLZ4CodecGetCodec() {
		Codec codec = CodecType.LZ_4_CODEC.getCodec();
		assertTrue(codec instanceof LZ4Codec);
	}
}
