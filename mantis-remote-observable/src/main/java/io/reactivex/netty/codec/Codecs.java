/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactivex.netty.codec;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;


/**
 * @deprecated As of release 0.601, use {@link io.mantisrx.common.codec.Codecs} instead
 */
@Deprecated
public class Codecs {

    public static Codec<Integer> integer() {

        return new Codec<Integer>() {
            @Override
            public Integer decode(byte[] bytes) {
                return ByteBuffer.wrap(bytes).getInt();
            }

            @Override
            public byte[] encode(final Integer value) {
                return ByteBuffer.allocate(4).putInt(value).array();
            }
        };
    }

    private static Codec<String> stringWithEncoding(String encoding) {
        final Charset charset = Charset.forName(encoding);

        return new Codec<String>() {
            @Override
            public String decode(byte[] bytes) {
                return new String(bytes, charset);
            }

            @Override
            public byte[] encode(final String value) {
                return value.getBytes(charset);
            }
        };
    }

    public static Codec<String> stringAscii() {

        final Charset charset = Charset.forName("US-ASCII");

        return new Codec<String>() {
            @Override
            public String decode(byte[] bytes) {
                return new String(bytes, charset);
            }

            @Override
            public byte[] encode(final String value) {
                final byte[] bytes = new byte[value.length()];
                for (int i = 0; i < value.length(); i++) {
                    bytes[i] = (byte) value.charAt(i);
                }
                return bytes;
            }
        };
    }

    public static Codec<String> stringUtf8() {
        return stringWithEncoding("UTF-8");
    }

    public static Codec<String> string() {
        return stringUtf8();
    }

    public static Codec<byte[]> bytearray() {
        return new Codec<byte[]>() {
            @Override
            public byte[] decode(byte[] bytes) {
                return bytes;
            }

            @Override
            public byte[] encode(final byte[] value) {
                return value;
            }
        };
    }
}
