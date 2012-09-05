/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;

import java.io.Serializable;

/**
 * The wrapper for the primitive type {@code char}. This class also provides a
 * number of utility methods for working with characters.
 * <p>
 * Character data is based upon the Unicode Standard, 4.0. The Unicode
 * specification, character tables and other information are available at <a
 * href="http://www.unicode.org/">http://www.unicode.org/</a>.
 * <p>
 * Unicode characters are referred to as <i>code points</i>. The range of valid
 * code points is U+0000 to U+10FFFF. The <i>Basic Multilingual Plane (BMP)</i>
 * is the code point range U+0000 to U+FFFF. Characters above the BMP are
 * referred to as <i>Supplementary Characters</i>. On the Java platform, UTF-16
 * encoding and {@code char} pairs are used to represent code points in the
 * supplementary range. A pair of {@code char} values that represent a
 * supplementary character are made up of a <i>high surrogate</i> with a value
 * range of 0xD800 to 0xDBFF and a <i>low surrogate</i> with a value range of
 * 0xDC00 to 0xDFFF.
 * <p>
 * On the Java platform a {@code char} value represents either a single BMP code
 * point or a UTF-16 unit that's part of a surrogate pair. The {@code int} type
 * is used to represent all Unicode code points.
 *
 * @since 1.0
 */
public final class Character implements Serializable, Comparable<Character> {
    private static final long serialVersionUID = 3786198910865385080L;

    private final char value;

    /**
     * The minimum {@code Character} value.
     */
    public static final char MIN_VALUE = 0;

    /**
     * The maximum {@code Character} value.
     */
    public static final char MAX_VALUE = '\uffff';

    /**
     * The minimum radix used for conversions between characters and integers.
     */
    public static final int MIN_RADIX = 2;

    /**
     * The maximum radix used for conversions between characters and integers.
     */
    public static final int MAX_RADIX = 36;

    /**
     * The {@link Class} object that represents the primitive type {@code char}.
     */
    @SuppressWarnings("unchecked")
    public static final Class<Character> TYPE = (Class<Character>) new char[0]
            .getClass().getComponentType();

    // Note: This can't be set to "char.class", since *that* is
    // defined to be "java.lang.Character.TYPE";

    /**
     * Unicode category constant Cn.
     */
    public static final byte UNASSIGNED = 0;

    /**
     * Unicode category constant Lu.
     */
    public static final byte UPPERCASE_LETTER = 1;

    /**
     * Unicode category constant Ll.
     */
    public static final byte LOWERCASE_LETTER = 2;

    /**
     * Unicode category constant Lt.
     */
    public static final byte TITLECASE_LETTER = 3;

    /**
     * Unicode category constant Lm.
     */
    public static final byte MODIFIER_LETTER = 4;

    /**
     * Unicode category constant Lo.
     */
    public static final byte OTHER_LETTER = 5;

    /**
     * Unicode category constant Mn.
     */
    public static final byte NON_SPACING_MARK = 6;

    /**
     * Unicode category constant Me.
     */
    public static final byte ENCLOSING_MARK = 7;

    /**
     * Unicode category constant Mc.
     */
    public static final byte COMBINING_SPACING_MARK = 8;

    /**
     * Unicode category constant Nd.
     */
    public static final byte DECIMAL_DIGIT_NUMBER = 9;

    /**
     * Unicode category constant Nl.
     */
    public static final byte LETTER_NUMBER = 10;

    /**
     * Unicode category constant No.
     */
    public static final byte OTHER_NUMBER = 11;

    /**
     * Unicode category constant Zs.
     */
    public static final byte SPACE_SEPARATOR = 12;

    /**
     * Unicode category constant Zl.
     */
    public static final byte LINE_SEPARATOR = 13;

    /**
     * Unicode category constant Zp.
     */
    public static final byte PARAGRAPH_SEPARATOR = 14;

    /**
     * Unicode category constant Cc.
     */
    public static final byte CONTROL = 15;

    /**
     * Unicode category constant Cf.
     */
    public static final byte FORMAT = 16;

    /**
     * Unicode category constant Co.
     */
    public static final byte PRIVATE_USE = 18;

    /**
     * Unicode category constant Cs.
     */
    public static final byte SURROGATE = 19;

    /**
     * Unicode category constant Pd.
     */
    public static final byte DASH_PUNCTUATION = 20;

    /**
     * Unicode category constant Ps.
     */
    public static final byte START_PUNCTUATION = 21;

    /**
     * Unicode category constant Pe.
     */
    public static final byte END_PUNCTUATION = 22;

    /**
     * Unicode category constant Pc.
     */
    public static final byte CONNECTOR_PUNCTUATION = 23;

    /**
     * Unicode category constant Po.
     */
    public static final byte OTHER_PUNCTUATION = 24;

    /**
     * Unicode category constant Sm.
     */
    public static final byte MATH_SYMBOL = 25;

    /**
     * Unicode category constant Sc.
     */
    public static final byte CURRENCY_SYMBOL = 26;

    /**
     * Unicode category constant Sk.
     */
    public static final byte MODIFIER_SYMBOL = 27;

    /**
     * Unicode category constant So.
     */
    public static final byte OTHER_SYMBOL = 28;

    /**
     * Unicode category constant Pi.
     *
     * @since 1.4
     */
    public static final byte INITIAL_QUOTE_PUNCTUATION = 29;

    /**
     * Unicode category constant Pf.
     *
     * @since 1.4
     */
    public static final byte FINAL_QUOTE_PUNCTUATION = 30;

    /**
     * Unicode bidirectional constant.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_UNDEFINED = -1;

    /**
     * Unicode bidirectional constant L.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = 0;

    /**
     * Unicode bidirectional constant R.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = 1;

    /**
     * Unicode bidirectional constant AL.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;

    /**
     * Unicode bidirectional constant EN.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = 3;

    /**
     * Unicode bidirectional constant ES.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;

    /**
     * Unicode bidirectional constant ET.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;

    /**
     * Unicode bidirectional constant AN.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_ARABIC_NUMBER = 6;

    /**
     * Unicode bidirectional constant CS.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;

    /**
     * Unicode bidirectional constant NSM.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_NONSPACING_MARK = 8;

    /**
     * Unicode bidirectional constant BN.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;

    /**
     * Unicode bidirectional constant B.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;

    /**
     * Unicode bidirectional constant S.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = 11;

    /**
     * Unicode bidirectional constant WS.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_WHITESPACE = 12;

    /**
     * Unicode bidirectional constant ON.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_OTHER_NEUTRALS = 13;

    /**
     * Unicode bidirectional constant LRE.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;

    /**
     * Unicode bidirectional constant LRO.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;

    /**
     * Unicode bidirectional constant RLE.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;

    /**
     * Unicode bidirectional constant RLO.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;

    /**
     * Unicode bidirectional constant PDF.
     *
     * @since 1.4
     */
    public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;
    
    /**
     * The minimum value of a high surrogate or leading surrogate unit in UTF-16
     * encoding, {@code '\uD800'}.
     *
     * @since 1.5
     */
    public static final char MIN_HIGH_SURROGATE;

    /**
     * The maximum value of a high surrogate or leading surrogate unit in UTF-16
     * encoding, {@code '\uDBFF'}.
     *
     * @since 1.5
     */
    public static final char MAX_HIGH_SURROGATE;

    /**
     * The minimum value of a low surrogate or trailing surrogate unit in UTF-16
     * encoding, {@code '\uDC00'}.
     *
     * @since 1.5
     */
    public static final char MIN_LOW_SURROGATE;

    /**
     * The maximum value of a low surrogate or trailing surrogate unit in UTF-16
     * encoding, {@code '\uDFFF'}.
     *
     * @since 1.5
     */
    public static final char MAX_LOW_SURROGATE;

    /**
     * The minimum value of a surrogate unit in UTF-16 encoding, {@code '\uD800'}.
     *
     * @since 1.5
     */
    public static final char MIN_SURROGATE;

    /**
     * The maximum value of a surrogate unit in UTF-16 encoding, {@code '\uDFFF'}.
     *
     * @since 1.5
     */
    public static final char MAX_SURROGATE;
    
    static {
	MIN_HIGH_SURROGATE = '\uD800';
	MAX_HIGH_SURROGATE = '\uDBFF';
	MIN_LOW_SURROGATE = '\uDC00';
	MAX_LOW_SURROGATE = '\uDFFF';
	MIN_SURROGATE = '\uD800';
	MAX_SURROGATE = '\uDFFF';
    }

    /**
     * The minimum value of a supplementary code point, {@code U+010000}.
     *
     * @since 1.5
     */
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x10000;

    /**
     * The minimum code point value, {@code U+0000}.
     *
     * @since 1.5
     */
    public static final int MIN_CODE_POINT = 0x000000;

    /**
     * The maximum code point value, {@code U+10FFFF}.
     *
     * @since 1.5
     */
    public static final int MAX_CODE_POINT = 0x10FFFF;

    /**
     * The number of bits required to represent a {@code Character} value
     * unsigned form.
     *
     * @since 1.5
     */
    public static final int SIZE = 16;

    
    // Unicode 3.0.1 (same as Unicode 3.0.0)
    private static final int[] bidiKeys = {
      0x0000, 0x0009, 0x000c, 0x000e, 0x001c, 0x001f, 0x0021, 0x0023, 
      0x0026, 0x002b, 0x002f, 0x0031, 0x003a, 0x003c, 0x0041, 0x005b, 
      0x0061, 0x007b, 0x007f, 0x0085, 0x0087, 0x00a0, 0x00a2, 0x00a6, 
      0x00aa, 0x00ac, 0x00b0, 0x00b2, 0x00b4, 0x00b7, 0x00b9, 0x00bb, 
      0x00c0, 0x00d7, 0x00d9, 0x00f7, 0x00f9, 0x0222, 0x0250, 0x02b0, 
      0x02b9, 0x02bb, 0x02c2, 0x02d0, 0x02d2, 0x02e0, 0x02e5, 0x02ee, 
      0x0300, 0x0360, 0x0374, 0x037a, 0x037e, 0x0384, 0x0386, 0x0389, 
      0x038c, 0x038e, 0x03a3, 0x03d0, 0x03da, 0x0400, 0x0483, 0x0488, 
      0x048c, 0x04c7, 0x04cb, 0x04d0, 0x04f8, 0x0531, 0x0559, 0x0561, 
      0x0589, 0x0591, 0x05a3, 0x05bb, 0x05be, 0x05c2, 0x05d0, 0x05f0, 
      0x060c, 0x061b, 0x061f, 0x0621, 0x0640, 0x064b, 0x0660, 0x066a, 
      0x066c, 0x0670, 0x0672, 0x06d6, 0x06e5, 0x06e7, 0x06e9, 0x06eb, 
      0x06f0, 0x06fa, 0x0700, 0x070f, 0x0711, 0x0713, 0x0730, 0x0780, 
      0x07a6, 0x0901, 0x0903, 0x0905, 0x093c, 0x093e, 0x0941, 0x0949, 
      0x094d, 0x0950, 0x0952, 0x0958, 0x0962, 0x0964, 0x0981, 0x0983, 
      0x0985, 0x098f, 0x0993, 0x09aa, 0x09b2, 0x09b6, 0x09bc, 0x09be, 
      0x09c1, 0x09c7, 0x09cb, 0x09cd, 0x09d7, 0x09dc, 0x09df, 0x09e2, 
      0x09e6, 0x09f2, 0x09f4, 0x0a02, 0x0a05, 0x0a0f, 0x0a13, 0x0a2a, 
      0x0a32, 0x0a35, 0x0a38, 0x0a3c, 0x0a3e, 0x0a41, 0x0a47, 0x0a4b, 
      0x0a59, 0x0a5e, 0x0a66, 0x0a70, 0x0a72, 0x0a81, 0x0a83, 0x0a85, 
      0x0a8d, 0x0a8f, 0x0a93, 0x0aaa, 0x0ab2, 0x0ab5, 0x0abc, 0x0abe, 
      0x0ac1, 0x0ac7, 0x0ac9, 0x0acb, 0x0acd, 0x0ad0, 0x0ae0, 0x0ae6, 
      0x0b01, 0x0b03, 0x0b05, 0x0b0f, 0x0b13, 0x0b2a, 0x0b32, 0x0b36, 
      0x0b3c, 0x0b3e, 0x0b42, 0x0b47, 0x0b4b, 0x0b4d, 0x0b56, 0x0b5c, 
      0x0b5f, 0x0b66, 0x0b82, 0x0b85, 0x0b8e, 0x0b92, 0x0b99, 0x0b9c, 
      0x0b9e, 0x0ba3, 0x0ba8, 0x0bae, 0x0bb7, 0x0bbe, 0x0bc0, 0x0bc2, 
      0x0bc6, 0x0bca, 0x0bcd, 0x0bd7, 0x0be7, 0x0c01, 0x0c05, 0x0c0e, 
      0x0c12, 0x0c2a, 0x0c35, 0x0c3e, 0x0c41, 0x0c46, 0x0c4a, 0x0c55, 
      0x0c60, 0x0c66, 0x0c82, 0x0c85, 0x0c8e, 0x0c92, 0x0caa, 0x0cb5, 
      0x0cbe, 0x0cc1, 0x0cc6, 0x0cc8, 0x0cca, 0x0ccc, 0x0cd5, 0x0cde, 
      0x0ce0, 0x0ce6, 0x0d02, 0x0d05, 0x0d0e, 0x0d12, 0x0d2a, 0x0d3e, 
      0x0d41, 0x0d46, 0x0d4a, 0x0d4d, 0x0d57, 0x0d60, 0x0d66, 0x0d82, 
      0x0d85, 0x0d9a, 0x0db3, 0x0dbd, 0x0dc0, 0x0dca, 0x0dcf, 0x0dd2, 
      0x0dd6, 0x0dd8, 0x0df2, 0x0e01, 0x0e31, 0x0e33, 0x0e35, 0x0e3f, 
      0x0e41, 0x0e47, 0x0e4f, 0x0e81, 0x0e84, 0x0e87, 0x0e8a, 0x0e8d, 
      0x0e94, 0x0e99, 0x0ea1, 0x0ea5, 0x0ea7, 0x0eaa, 0x0ead, 0x0eb1, 
      0x0eb3, 0x0eb5, 0x0ebb, 0x0ebd, 0x0ec0, 0x0ec6, 0x0ec8, 0x0ed0, 
      0x0edc, 0x0f00, 0x0f18, 0x0f1a, 0x0f35, 0x0f3a, 0x0f3e, 0x0f49, 
      0x0f71, 0x0f7f, 0x0f81, 0x0f85, 0x0f87, 0x0f89, 0x0f90, 0x0f99, 
      0x0fbe, 0x0fc6, 0x0fc8, 0x0fcf, 0x1000, 0x1023, 0x1029, 0x102c, 
      0x102e, 0x1031, 0x1036, 0x1038, 0x1040, 0x1058, 0x10a0, 0x10d0, 
      0x10fb, 0x1100, 0x115f, 0x11a8, 0x1200, 0x1208, 0x1248, 0x124a, 
      0x1250, 0x1258, 0x125a, 0x1260, 0x1288, 0x128a, 0x1290, 0x12b0, 
      0x12b2, 0x12b8, 0x12c0, 0x12c2, 0x12c8, 0x12d0, 0x12d8, 0x12f0, 
      0x1310, 0x1312, 0x1318, 0x1320, 0x1348, 0x1361, 0x13a0, 0x1401, 
      0x1680, 0x1682, 0x169b, 0x16a0, 0x1780, 0x17b7, 0x17be, 0x17c6, 
      0x17c8, 0x17ca, 0x17d4, 0x17db, 0x17e0, 0x1800, 0x180b, 0x1810, 
      0x1820, 0x1880, 0x18a9, 0x1e00, 0x1ea0, 0x1f00, 0x1f18, 0x1f20, 
      0x1f48, 0x1f50, 0x1f59, 0x1f5b, 0x1f5d, 0x1f5f, 0x1f80, 0x1fb6, 
      0x1fbd, 0x1fc0, 0x1fc2, 0x1fc6, 0x1fcd, 0x1fd0, 0x1fd6, 0x1fdd, 
      0x1fe0, 0x1fed, 0x1ff2, 0x1ff6, 0x1ffd, 0x2000, 0x200b, 0x200e, 
      0x2010, 0x2028, 0x202a, 0x202c, 0x202e, 0x2030, 0x2035, 0x2048, 
      0x206a, 0x2070, 0x2074, 0x207a, 0x207c, 0x207f, 0x2081, 0x208a, 
      0x208c, 0x20a0, 0x20d0, 0x2100, 0x2102, 0x2104, 0x2107, 0x2109, 
      0x210b, 0x2114, 0x2117, 0x2119, 0x211e, 0x2124, 0x212b, 0x212e, 
      0x2130, 0x2132, 0x2134, 0x213a, 0x2153, 0x2160, 0x2190, 0x2200, 
      0x2212, 0x2214, 0x2300, 0x2336, 0x237b, 0x237d, 0x2395, 0x2397, 
      0x2400, 0x2440, 0x2460, 0x249c, 0x24ea, 0x2500, 0x25a0, 0x2600, 
      0x2619, 0x2701, 0x2706, 0x270c, 0x2729, 0x274d, 0x274f, 0x2756, 
      0x2758, 0x2761, 0x2776, 0x2798, 0x27b1, 0x2800, 0x2e80, 0x2e9b, 
      0x2f00, 0x2ff0, 0x3000, 0x3002, 0x3005, 0x3008, 0x3021, 0x302a, 
      0x3030, 0x3032, 0x3036, 0x3038, 0x303e, 0x3041, 0x3099, 0x309b, 
      0x309d, 0x30a1, 0x30fb, 0x30fd, 0x3105, 0x3131, 0x3190, 0x3200, 
      0x3220, 0x3260, 0x327f, 0x32c0, 0x32d0, 0x3300, 0x337b, 0x33e0, 
      0x3400, 0x4e00, 0xa000, 0xa490, 0xa4a4, 0xa4b5, 0xa4c2, 0xa4c6, 
      0xac00, 0xd800, 0xfb00, 0xfb13, 0xfb1d, 0xfb20, 0xfb29, 0xfb2b, 
      0xfb38, 0xfb3e, 0xfb40, 0xfb43, 0xfb46, 0xfb50, 0xfbd3, 0xfd3e, 
      0xfd50, 0xfd92, 0xfdf0, 0xfe20, 0xfe30, 0xfe49, 0xfe50, 0xfe54, 
      0xfe57, 0xfe5f, 0xfe61, 0xfe63, 0xfe65, 0xfe68, 0xfe6a, 0xfe70, 
      0xfe74, 0xfe76, 0xfeff, 0xff01, 0xff03, 0xff06, 0xff0b, 0xff0f, 
      0xff11, 0xff1a, 0xff1c, 0xff21, 0xff3b, 0xff41, 0xff5b, 0xff61, 
      0xff66, 0xffc2, 0xffca, 0xffd2, 0xffda, 0xffe0, 0xffe2, 0xffe5, 
      0xffe8, 0xfff9, 0xfffc
    };

    private static final int[] bidiValues = {
        0x0008, 0x000a, 0x000b, 0x0c0b, 0x000d, 0x0b0d, 0x001b, 0x000a, 
        0x001e, 0x000b, 0x0020, 0x0c0d, 0x0022, 0x000e, 0x0025, 0x0006, 
        0x002a, 0x000e, 0x002e, 0x0608, 0x0030, 0x0504, 0x0039, 0x0004, 
        0x003b, 0x0e08, 0x0040, 0x000e, 0x005a, 0x0001, 0x0060, 0x000e, 
        0x007a, 0x0001, 0x007e, 0x000e, 0x0084, 0x000a, 0x0086, 0x0b0a, 
        0x009f, 0x000a, 0x00a1, 0x0e08, 0x00a5, 0x0006, 0x00a9, 0x000e, 
        0x00ab, 0x0e01, 0x00af, 0x000e, 0x00b1, 0x0006, 0x00b3, 0x0004, 
        0x00b6, 0x010e, 0x00b8, 0x000e, 0x00ba, 0x0401, 0x00bf, 0x000e, 
        0x00d6, 0x0001, 0x00d8, 0x0e01, 0x00f6, 0x0001, 0x00f8, 0x0e01, 
        0x021f, 0x0001, 0x0233, 0x0001, 0x02ad, 0x0001, 0x02b8, 0x0001, 
        0x02ba, 0x000e, 0x02c1, 0x0001, 0x02cf, 0x000e, 0x02d1, 0x0001, 
        0x02df, 0x000e, 0x02e4, 0x0001, 0x02ed, 0x000e, 0x02ee, 0x0001, 
        0x034e, 0x0009, 0x0362, 0x0009, 0x0375, 0x000e, 0x037a, 0x0001, 
        0x037e, 0x000e, 0x0385, 0x000e, 0x0388, 0x0e01, 0x038a, 0x0001, 
        0x038c, 0x0001, 0x03a1, 0x0001, 0x03ce, 0x0001, 0x03d7, 0x0001, 
        0x03f3, 0x0001, 0x0482, 0x0001, 0x0486, 0x0009, 0x0489, 0x0009, 
        0x04c4, 0x0001, 0x04c8, 0x0001, 0x04cc, 0x0001, 0x04f5, 0x0001, 
        0x04f9, 0x0001, 0x0556, 0x0001, 0x055f, 0x0001, 0x0587, 0x0001, 
        0x058a, 0x010e, 0x05a1, 0x0009, 0x05b9, 0x0009, 0x05bd, 0x0009, 
        0x05c1, 0x0902, 0x05c4, 0x0209, 0x05ea, 0x0002, 0x05f4, 0x0002, 
        0x060c, 0x0008, 0x061b, 0x0300, 0x061f, 0x0300, 0x063a, 0x0003, 
        0x064a, 0x0003, 0x0655, 0x0009, 0x0669, 0x0007, 0x066b, 0x0706, 
        0x066d, 0x0307, 0x0671, 0x0309, 0x06d5, 0x0003, 0x06e4, 0x0009, 
        0x06e6, 0x0003, 0x06e8, 0x0009, 0x06ea, 0x0e09, 0x06ed, 0x0009, 
        0x06f9, 0x0004, 0x06fe, 0x0003, 0x070d, 0x0003, 0x0710, 0x0a03, 
        0x0712, 0x0903, 0x072c, 0x0003, 0x074a, 0x0009, 0x07a5, 0x0003, 
        0x07b0, 0x0009, 0x0902, 0x0009, 0x0903, 0x0100, 0x0939, 0x0001, 
        0x093d, 0x0109, 0x0940, 0x0001, 0x0948, 0x0009, 0x094c, 0x0001, 
        0x094d, 0x0900, 0x0951, 0x0901, 0x0954, 0x0009, 0x0961, 0x0001, 
        0x0963, 0x0009, 0x0970, 0x0001, 0x0982, 0x0901, 0x0983, 0x0100, 
        0x098c, 0x0001, 0x0990, 0x0001, 0x09a8, 0x0001, 0x09b0, 0x0001, 
        0x09b2, 0x0001, 0x09b9, 0x0001, 0x09bc, 0x0009, 0x09c0, 0x0001, 
        0x09c4, 0x0009, 0x09c8, 0x0001, 0x09cc, 0x0001, 0x09cd, 0x0900, 
        0x09d7, 0x0100, 0x09dd, 0x0001, 0x09e1, 0x0001, 0x09e3, 0x0009, 
        0x09f1, 0x0001, 0x09f3, 0x0006, 0x09fa, 0x0001, 0x0a02, 0x0009, 
        0x0a0a, 0x0001, 0x0a10, 0x0001, 0x0a28, 0x0001, 0x0a30, 0x0001, 
        0x0a33, 0x0001, 0x0a36, 0x0001, 0x0a39, 0x0001, 0x0a3c, 0x0009, 
        0x0a40, 0x0001, 0x0a42, 0x0009, 0x0a48, 0x0009, 0x0a4d, 0x0009, 
        0x0a5c, 0x0001, 0x0a5e, 0x0001, 0x0a6f, 0x0001, 0x0a71, 0x0009, 
        0x0a74, 0x0001, 0x0a82, 0x0009, 0x0a83, 0x0100, 0x0a8b, 0x0001, 
        0x0a8d, 0x0100, 0x0a91, 0x0001, 0x0aa8, 0x0001, 0x0ab0, 0x0001, 
        0x0ab3, 0x0001, 0x0ab9, 0x0001, 0x0abd, 0x0109, 0x0ac0, 0x0001, 
        0x0ac5, 0x0009, 0x0ac8, 0x0009, 0x0ac9, 0x0100, 0x0acc, 0x0001, 
        0x0acd, 0x0900, 0x0ad0, 0x0001, 0x0ae0, 0x0001, 0x0aef, 0x0001, 
        0x0b02, 0x0901, 0x0b03, 0x0100, 0x0b0c, 0x0001, 0x0b10, 0x0001, 
        0x0b28, 0x0001, 0x0b30, 0x0001, 0x0b33, 0x0001, 0x0b39, 0x0001, 
        0x0b3d, 0x0109, 0x0b41, 0x0901, 0x0b43, 0x0009, 0x0b48, 0x0001, 
        0x0b4c, 0x0001, 0x0b4d, 0x0900, 0x0b57, 0x0109, 0x0b5d, 0x0001, 
        0x0b61, 0x0001, 0x0b70, 0x0001, 0x0b83, 0x0109, 0x0b8a, 0x0001, 
        0x0b90, 0x0001, 0x0b95, 0x0001, 0x0b9a, 0x0001, 0x0b9c, 0x0001, 
        0x0b9f, 0x0001, 0x0ba4, 0x0001, 0x0baa, 0x0001, 0x0bb5, 0x0001, 
        0x0bb9, 0x0001, 0x0bbf, 0x0001, 0x0bc1, 0x0109, 0x0bc2, 0x0001, 
        0x0bc8, 0x0001, 0x0bcc, 0x0001, 0x0bcd, 0x0900, 0x0bd7, 0x0100, 
        0x0bf2, 0x0001, 0x0c03, 0x0001, 0x0c0c, 0x0001, 0x0c10, 0x0001, 
        0x0c28, 0x0001, 0x0c33, 0x0001, 0x0c39, 0x0001, 0x0c40, 0x0009, 
        0x0c44, 0x0001, 0x0c48, 0x0009, 0x0c4d, 0x0009, 0x0c56, 0x0009, 
        0x0c61, 0x0001, 0x0c6f, 0x0001, 0x0c83, 0x0001, 0x0c8c, 0x0001, 
        0x0c90, 0x0001, 0x0ca8, 0x0001, 0x0cb3, 0x0001, 0x0cb9, 0x0001, 
        0x0cc0, 0x0901, 0x0cc4, 0x0001, 0x0cc7, 0x0109, 0x0cc8, 0x0001, 
        0x0ccb, 0x0001, 0x0ccd, 0x0009, 0x0cd6, 0x0001, 0x0cde, 0x0001, 
        0x0ce1, 0x0001, 0x0cef, 0x0001, 0x0d03, 0x0001, 0x0d0c, 0x0001, 
        0x0d10, 0x0001, 0x0d28, 0x0001, 0x0d39, 0x0001, 0x0d40, 0x0001, 
        0x0d43, 0x0009, 0x0d48, 0x0001, 0x0d4c, 0x0001, 0x0d4d, 0x0900, 
        0x0d57, 0x0100, 0x0d61, 0x0001, 0x0d6f, 0x0001, 0x0d83, 0x0001, 
        0x0d96, 0x0001, 0x0db1, 0x0001, 0x0dbb, 0x0001, 0x0dbd, 0x0100, 
        0x0dc6, 0x0001, 0x0dca, 0x0009, 0x0dd1, 0x0001, 0x0dd4, 0x0009, 
        0x0dd6, 0x0009, 0x0ddf, 0x0001, 0x0df4, 0x0001, 0x0e30, 0x0001, 
        0x0e32, 0x0901, 0x0e34, 0x0109, 0x0e3a, 0x0009, 0x0e40, 0x0601, 
        0x0e46, 0x0001, 0x0e4e, 0x0009, 0x0e5b, 0x0001, 0x0e82, 0x0001, 
        0x0e84, 0x0001, 0x0e88, 0x0001, 0x0e8a, 0x0001, 0x0e8d, 0x0100, 
        0x0e97, 0x0001, 0x0e9f, 0x0001, 0x0ea3, 0x0001, 0x0ea5, 0x0100, 
        0x0ea7, 0x0100, 0x0eab, 0x0001, 0x0eb0, 0x0001, 0x0eb2, 0x0901, 
        0x0eb4, 0x0109, 0x0eb9, 0x0009, 0x0ebc, 0x0009, 0x0ebd, 0x0100, 
        0x0ec4, 0x0001, 0x0ec6, 0x0001, 0x0ecd, 0x0009, 0x0ed9, 0x0001, 
        0x0edd, 0x0001, 0x0f17, 0x0001, 0x0f19, 0x0009, 0x0f34, 0x0001, 
        0x0f39, 0x0901, 0x0f3d, 0x000e, 0x0f47, 0x0001, 0x0f6a, 0x0001, 
        0x0f7e, 0x0009, 0x0f80, 0x0109, 0x0f84, 0x0009, 0x0f86, 0x0109, 
        0x0f88, 0x0901, 0x0f8b, 0x0001, 0x0f97, 0x0009, 0x0fbc, 0x0009, 
        0x0fc5, 0x0001, 0x0fc7, 0x0109, 0x0fcc, 0x0001, 0x0fcf, 0x0100, 
        0x1021, 0x0001, 0x1027, 0x0001, 0x102a, 0x0001, 0x102d, 0x0901, 
        0x1030, 0x0009, 0x1032, 0x0109, 0x1037, 0x0009, 0x1039, 0x0901, 
        0x1057, 0x0001, 0x1059, 0x0009, 0x10c5, 0x0001, 0x10f6, 0x0001, 
        0x10fb, 0x0100, 0x1159, 0x0001, 0x11a2, 0x0001, 0x11f9, 0x0001, 
        0x1206, 0x0001, 0x1246, 0x0001, 0x1248, 0x0001, 0x124d, 0x0001, 
        0x1256, 0x0001, 0x1258, 0x0001, 0x125d, 0x0001, 0x1286, 0x0001, 
        0x1288, 0x0001, 0x128d, 0x0001, 0x12ae, 0x0001, 0x12b0, 0x0001, 
        0x12b5, 0x0001, 0x12be, 0x0001, 0x12c0, 0x0001, 0x12c5, 0x0001, 
        0x12ce, 0x0001, 0x12d6, 0x0001, 0x12ee, 0x0001, 0x130e, 0x0001, 
        0x1310, 0x0001, 0x1315, 0x0001, 0x131e, 0x0001, 0x1346, 0x0001, 
        0x135a, 0x0001, 0x137c, 0x0001, 0x13f4, 0x0001, 0x1676, 0x0001, 
        0x1681, 0x010d, 0x169a, 0x0001, 0x169c, 0x000e, 0x16f0, 0x0001, 
        0x17b6, 0x0001, 0x17bd, 0x0009, 0x17c5, 0x0001, 0x17c7, 0x0109, 
        0x17c9, 0x0901, 0x17d3, 0x0009, 0x17da, 0x0001, 0x17dc, 0x0601, 
        0x17e9, 0x0001, 0x180a, 0x000e, 0x180e, 0x000a, 0x1819, 0x0001, 
        0x1877, 0x0001, 0x18a8, 0x0001, 0x18a9, 0x0900, 0x1e9b, 0x0001, 
        0x1ef9, 0x0001, 0x1f15, 0x0001, 0x1f1d, 0x0001, 0x1f45, 0x0001, 
        0x1f4d, 0x0001, 0x1f57, 0x0001, 0x1f59, 0x0100, 0x1f5b, 0x0100, 
        0x1f5d, 0x0100, 0x1f7d, 0x0001, 0x1fb4, 0x0001, 0x1fbc, 0x0001, 
        0x1fbf, 0x0e01, 0x1fc1, 0x000e, 0x1fc4, 0x0001, 0x1fcc, 0x0001, 
        0x1fcf, 0x000e, 0x1fd3, 0x0001, 0x1fdb, 0x0001, 0x1fdf, 0x000e, 
        0x1fec, 0x0001, 0x1fef, 0x000e, 0x1ff4, 0x0001, 0x1ffc, 0x0001, 
        0x1ffe, 0x000e, 0x200a, 0x000d, 0x200d, 0x000a, 0x200f, 0x0201, 
        0x2027, 0x000e, 0x2029, 0x0b0d, 0x202b, 0x110f, 0x202d, 0x1013, 
        0x202f, 0x0d12, 0x2034, 0x0006, 0x2046, 0x000e, 0x204d, 0x000e, 
        0x206f, 0x000a, 0x2070, 0x0004, 0x2079, 0x0004, 0x207b, 0x0006, 
        0x207e, 0x000e, 0x2080, 0x0104, 0x2089, 0x0004, 0x208b, 0x0006, 
        0x208e, 0x000e, 0x20af, 0x0006, 0x20e3, 0x0009, 0x2101, 0x000e, 
        0x2103, 0x0e01, 0x2106, 0x000e, 0x2108, 0x010e, 0x210a, 0x0e01, 
        0x2113, 0x0001, 0x2116, 0x010e, 0x2118, 0x000e, 0x211d, 0x0001, 
        0x2123, 0x000e, 0x212a, 0x0e01, 0x212d, 0x0001, 0x212f, 0x0106, 
        0x2131, 0x0001, 0x2133, 0x010e, 0x2139, 0x0001, 0x213a, 0x000e, 
        0x215f, 0x000e, 0x2183, 0x0001, 0x21f3, 0x000e, 0x2211, 0x000e, 
        0x2213, 0x0006, 0x22f1, 0x000e, 0x2335, 0x000e, 0x237a, 0x0001, 
        0x237b, 0x0e00, 0x2394, 0x000e, 0x2396, 0x010e, 0x239a, 0x000e, 
        0x2426, 0x000e, 0x244a, 0x000e, 0x249b, 0x0004, 0x24e9, 0x0001, 
        0x24ea, 0x0004, 0x2595, 0x000e, 0x25f7, 0x000e, 0x2613, 0x000e, 
        0x2671, 0x000e, 0x2704, 0x000e, 0x2709, 0x000e, 0x2727, 0x000e, 
        0x274b, 0x000e, 0x274d, 0x0e00, 0x2752, 0x000e, 0x2756, 0x000e, 
        0x275e, 0x000e, 0x2767, 0x000e, 0x2794, 0x000e, 0x27af, 0x000e, 
        0x27be, 0x000e, 0x28ff, 0x000e, 0x2e99, 0x000e, 0x2ef3, 0x000e, 
        0x2fd5, 0x000e, 0x2ffb, 0x000e, 0x3001, 0x0e0d, 0x3004, 0x000e, 
        0x3007, 0x0001, 0x3020, 0x000e, 0x3029, 0x0001, 0x302f, 0x0009, 
        0x3031, 0x010e, 0x3035, 0x0001, 0x3037, 0x000e, 0x303a, 0x0001, 
        0x303f, 0x000e, 0x3094, 0x0001, 0x309a, 0x0009, 0x309c, 0x000e, 
        0x309e, 0x0001, 0x30fa, 0x0001, 0x30fc, 0x0e01, 0x30fe, 0x0001, 
        0x312c, 0x0001, 0x318e, 0x0001, 0x31b7, 0x0001, 0x321c, 0x0001, 
        0x3243, 0x0001, 0x327b, 0x0001, 0x32b0, 0x0001, 0x32cb, 0x0001, 
        0x32fe, 0x0001, 0x3376, 0x0001, 0x33dd, 0x0001, 0x33fe, 0x0001, 
        0x4db5, 0x0001, 0x9fa5, 0x0001, 0xa48c, 0x0001, 0xa4a1, 0x000e, 
        0xa4b3, 0x000e, 0xa4c0, 0x000e, 0xa4c4, 0x000e, 0xa4c6, 0x000e, 
        0xd7a3, 0x0001, 0xfa2d, 0x0001, 0xfb06, 0x0001, 0xfb17, 0x0001, 
        0xfb1f, 0x0209, 0xfb28, 0x0002, 0xfb2a, 0x0602, 0xfb36, 0x0002, 
        0xfb3c, 0x0002, 0xfb3e, 0x0002, 0xfb41, 0x0002, 0xfb44, 0x0002, 
        0xfb4f, 0x0002, 0xfbb1, 0x0003, 0xfd3d, 0x0003, 0xfd3f, 0x000e, 
        0xfd8f, 0x0003, 0xfdc7, 0x0003, 0xfdfb, 0x0003, 0xfe23, 0x0009, 
        0xfe44, 0x000e, 0xfe4f, 0x000e, 0xfe52, 0x0e08, 0xfe56, 0x080e, 
        0xfe5e, 0x000e, 0xfe60, 0x060e, 0xfe62, 0x0e06, 0xfe64, 0x060e, 
        0xfe66, 0x000e, 0xfe69, 0x060e, 0xfe6b, 0x0e06, 0xfe72, 0x0003, 
        0xfe74, 0x0003, 0xfefc, 0x0003, 0xfeff, 0x0a00, 0xff02, 0x000e, 
        0xff05, 0x0006, 0xff0a, 0x000e, 0xff0e, 0x0608, 0xff10, 0x0504, 
        0xff19, 0x0004, 0xff1b, 0x0e08, 0xff20, 0x000e, 0xff3a, 0x0001, 
        0xff40, 0x000e, 0xff5a, 0x0001, 0xff5e, 0x000e, 0xff65, 0x000e, 
        0xffbe, 0x0001, 0xffc7, 0x0001, 0xffcf, 0x0001, 0xffd7, 0x0001, 
        0xffdc, 0x0001, 0xffe1, 0x0006, 0xffe4, 0x000e, 0xffe6, 0x0006, 
        0xffee, 0x000e, 0xfffb, 0x000a, 0xfffd, 0x000e
    };

    private static final int[] mirrored = {
        0x0000, 0x0000, 0x0300, 0x5000, 0x0000, 0x2800, 0x0000, 0x2800, 
        0x0000, 0x0000, 0x0800, 0x0800, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0600, 0x0060, 0x0000, 0x0000, 0x6000, 
        0x6000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x3f1e, 0xbc62, 0xf857, 0xfa0f, 0x1fff, 0x803c, 0xcff5, 0xffff, 
        0x9fff, 0x0107, 0xffcc, 0xc1ff, 0x3e00, 0xffc3, 0x3fff, 0x0003, 
        0x0f00, 0x0000, 0x0603, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0xff00, 0x0ff3
    };

    // Unicode 3.0.1 (same as Unicode 3.0.0)
    private static final int[] typeKeys = {
        0x0000, 0x0020, 0x0022, 0x0024, 0x0026, 0x0028, 0x002a, 0x002d, 
        0x002f, 0x0031, 0x003a, 0x003c, 0x003f, 0x0041, 0x005b, 0x005d, 
        0x005f, 0x0061, 0x007b, 0x007d, 0x007f, 0x00a0, 0x00a2, 0x00a6, 
        0x00a8, 0x00aa, 0x00ac, 0x00ae, 0x00b1, 0x00b3, 0x00b5, 0x00b7, 
        0x00b9, 0x00bb, 0x00bd, 0x00bf, 0x00c1, 0x00d7, 0x00d9, 0x00df, 
        0x00f7, 0x00f9, 0x0100, 0x0138, 0x0149, 0x0179, 0x017f, 0x0181, 
        0x0183, 0x0187, 0x018a, 0x018c, 0x018e, 0x0192, 0x0194, 0x0197, 
        0x0199, 0x019c, 0x019e, 0x01a0, 0x01a7, 0x01ab, 0x01af, 0x01b2, 
        0x01b4, 0x01b8, 0x01ba, 0x01bc, 0x01be, 0x01c0, 0x01c4, 0x01c6, 
        0x01c8, 0x01ca, 0x01cc, 0x01dd, 0x01f0, 0x01f2, 0x01f4, 0x01f7, 
        0x01f9, 0x0222, 0x0250, 0x02b0, 0x02b9, 0x02bb, 0x02c2, 0x02d0, 
        0x02d2, 0x02e0, 0x02e5, 0x02ee, 0x0300, 0x0360, 0x0374, 0x037a, 
        0x037e, 0x0384, 0x0386, 0x0389, 0x038c, 0x038e, 0x0390, 0x0392, 
        0x03a3, 0x03ac, 0x03d0, 0x03d2, 0x03d5, 0x03da, 0x03f0, 0x0400, 
        0x0430, 0x0460, 0x0482, 0x0484, 0x0488, 0x048c, 0x04c1, 0x04c7, 
        0x04cb, 0x04d0, 0x04f8, 0x0531, 0x0559, 0x055b, 0x0561, 0x0589, 
        0x0591, 0x05a3, 0x05bb, 0x05be, 0x05c2, 0x05d0, 0x05f0, 0x05f3, 
        0x060c, 0x061b, 0x061f, 0x0621, 0x0640, 0x0642, 0x064b, 0x0660, 
        0x066a, 0x0670, 0x0672, 0x06d4, 0x06d6, 0x06dd, 0x06df, 0x06e5, 
        0x06e7, 0x06e9, 0x06eb, 0x06f0, 0x06fa, 0x06fd, 0x0700, 0x070f, 
        0x0711, 0x0713, 0x0730, 0x0780, 0x07a6, 0x0901, 0x0903, 0x0905, 
        0x093c, 0x093e, 0x0941, 0x0949, 0x094d, 0x0950, 0x0952, 0x0958, 
        0x0962, 0x0964, 0x0966, 0x0970, 0x0981, 0x0983, 0x0985, 0x098f, 
        0x0993, 0x09aa, 0x09b2, 0x09b6, 0x09bc, 0x09be, 0x09c1, 0x09c7, 
        0x09cb, 0x09cd, 0x09d7, 0x09dc, 0x09df, 0x09e2, 0x09e6, 0x09f0, 
        0x09f2, 0x09f4, 0x09fa, 0x0a02, 0x0a05, 0x0a0f, 0x0a13, 0x0a2a, 
        0x0a32, 0x0a35, 0x0a38, 0x0a3c, 0x0a3e, 0x0a41, 0x0a47, 0x0a4b, 
        0x0a59, 0x0a5e, 0x0a66, 0x0a70, 0x0a72, 0x0a81, 0x0a83, 0x0a85, 
        0x0a8d, 0x0a8f, 0x0a93, 0x0aaa, 0x0ab2, 0x0ab5, 0x0abc, 0x0abe, 
        0x0ac1, 0x0ac7, 0x0ac9, 0x0acb, 0x0acd, 0x0ad0, 0x0ae0, 0x0ae6, 
        0x0b01, 0x0b03, 0x0b05, 0x0b0f, 0x0b13, 0x0b2a, 0x0b32, 0x0b36, 
        0x0b3c, 0x0b3e, 0x0b42, 0x0b47, 0x0b4b, 0x0b4d, 0x0b56, 0x0b5c, 
        0x0b5f, 0x0b66, 0x0b70, 0x0b82, 0x0b85, 0x0b8e, 0x0b92, 0x0b99, 
        0x0b9c, 0x0b9e, 0x0ba3, 0x0ba8, 0x0bae, 0x0bb7, 0x0bbe, 0x0bc0, 
        0x0bc2, 0x0bc6, 0x0bca, 0x0bcd, 0x0bd7, 0x0be7, 0x0bf0, 0x0c01, 
        0x0c05, 0x0c0e, 0x0c12, 0x0c2a, 0x0c35, 0x0c3e, 0x0c41, 0x0c46, 
        0x0c4a, 0x0c55, 0x0c60, 0x0c66, 0x0c82, 0x0c85, 0x0c8e, 0x0c92, 
        0x0caa, 0x0cb5, 0x0cbe, 0x0cc1, 0x0cc6, 0x0cc8, 0x0cca, 0x0ccc, 
        0x0cd5, 0x0cde, 0x0ce0, 0x0ce6, 0x0d02, 0x0d05, 0x0d0e, 0x0d12, 
        0x0d2a, 0x0d3e, 0x0d41, 0x0d46, 0x0d4a, 0x0d4d, 0x0d57, 0x0d60, 
        0x0d66, 0x0d82, 0x0d85, 0x0d9a, 0x0db3, 0x0dbd, 0x0dc0, 0x0dca, 
        0x0dcf, 0x0dd2, 0x0dd6, 0x0dd8, 0x0df2, 0x0df4, 0x0e01, 0x0e31, 
        0x0e33, 0x0e35, 0x0e3f, 0x0e41, 0x0e46, 0x0e48, 0x0e4f, 0x0e51, 
        0x0e5a, 0x0e81, 0x0e84, 0x0e87, 0x0e8a, 0x0e8d, 0x0e94, 0x0e99, 
        0x0ea1, 0x0ea5, 0x0ea7, 0x0eaa, 0x0ead, 0x0eb1, 0x0eb3, 0x0eb5, 
        0x0ebb, 0x0ebd, 0x0ec0, 0x0ec6, 0x0ec8, 0x0ed0, 0x0edc, 0x0f00, 
        0x0f02, 0x0f04, 0x0f13, 0x0f18, 0x0f1a, 0x0f20, 0x0f2a, 0x0f34, 
        0x0f3a, 0x0f3e, 0x0f40, 0x0f49, 0x0f71, 0x0f7f, 0x0f81, 0x0f85, 
        0x0f87, 0x0f89, 0x0f90, 0x0f99, 0x0fbe, 0x0fc6, 0x0fc8, 0x0fcf, 
        0x1000, 0x1023, 0x1029, 0x102c, 0x102e, 0x1031, 0x1036, 0x1038, 
        0x1040, 0x104a, 0x1050, 0x1056, 0x1058, 0x10a0, 0x10d0, 0x10fb, 
        0x1100, 0x115f, 0x11a8, 0x1200, 0x1208, 0x1248, 0x124a, 0x1250, 
        0x1258, 0x125a, 0x1260, 0x1288, 0x128a, 0x1290, 0x12b0, 0x12b2, 
        0x12b8, 0x12c0, 0x12c2, 0x12c8, 0x12d0, 0x12d8, 0x12f0, 0x1310, 
        0x1312, 0x1318, 0x1320, 0x1348, 0x1361, 0x1369, 0x1372, 0x13a0, 
        0x1401, 0x166d, 0x166f, 0x1680, 0x1682, 0x169b, 0x16a0, 0x16eb, 
        0x16ee, 0x1780, 0x17b4, 0x17b7, 0x17be, 0x17c6, 0x17c8, 0x17ca, 
        0x17d4, 0x17db, 0x17e0, 0x1800, 0x1806, 0x1808, 0x180b, 0x1810, 
        0x1820, 0x1843, 0x1845, 0x1880, 0x18a9, 0x1e00, 0x1e96, 0x1ea0, 
        0x1f00, 0x1f08, 0x1f10, 0x1f18, 0x1f20, 0x1f28, 0x1f30, 0x1f38, 
        0x1f40, 0x1f48, 0x1f50, 0x1f59, 0x1f5b, 0x1f5d, 0x1f5f, 0x1f61, 
        0x1f68, 0x1f70, 0x1f80, 0x1f88, 0x1f90, 0x1f98, 0x1fa0, 0x1fa8, 
        0x1fb0, 0x1fb6, 0x1fb8, 0x1fbc, 0x1fbe, 0x1fc0, 0x1fc2, 0x1fc6, 
        0x1fc8, 0x1fcc, 0x1fce, 0x1fd0, 0x1fd6, 0x1fd8, 0x1fdd, 0x1fe0, 
        0x1fe8, 0x1fed, 0x1ff2, 0x1ff6, 0x1ff8, 0x1ffc, 0x1ffe, 0x2000, 
        0x200c, 0x2010, 0x2016, 0x2018, 0x201a, 0x201c, 0x201e, 0x2020, 
        0x2028, 0x202a, 0x202f, 0x2031, 0x2039, 0x203b, 0x203f, 0x2041, 
        0x2044, 0x2046, 0x2048, 0x206a, 0x2070, 0x2074, 0x207a, 0x207d, 
        0x207f, 0x2081, 0x208a, 0x208d, 0x20a0, 0x20d0, 0x20dd, 0x20e1, 
        0x20e3, 0x2100, 0x2102, 0x2104, 0x2107, 0x2109, 0x210b, 0x210e, 
        0x2110, 0x2113, 0x2115, 0x2117, 0x2119, 0x211e, 0x2124, 0x212b, 
        0x212e, 0x2130, 0x2132, 0x2134, 0x2136, 0x2139, 0x2153, 0x2160, 
        0x2190, 0x2195, 0x219a, 0x219c, 0x21a0, 0x21a2, 0x21a5, 0x21a8, 
        0x21ae, 0x21b0, 0x21ce, 0x21d0, 0x21d2, 0x21d6, 0x2200, 0x2300, 
        0x2308, 0x230c, 0x2320, 0x2322, 0x2329, 0x232b, 0x237d, 0x2400, 
        0x2440, 0x2460, 0x249c, 0x24ea, 0x2500, 0x25a0, 0x25b7, 0x25b9, 
        0x25c1, 0x25c3, 0x2600, 0x2619, 0x266f, 0x2671, 0x2701, 0x2706, 
        0x270c, 0x2729, 0x274d, 0x274f, 0x2756, 0x2758, 0x2761, 0x2776, 
        0x2794, 0x2798, 0x27b1, 0x2800, 0x2e80, 0x2e9b, 0x2f00, 0x2ff0, 
        0x3000, 0x3002, 0x3004, 0x3006, 0x3008, 0x3012, 0x3014, 0x301c, 
        0x301e, 0x3020, 0x3022, 0x302a, 0x3030, 0x3032, 0x3036, 0x3038, 
        0x303e, 0x3041, 0x3099, 0x309b, 0x309d, 0x30a1, 0x30fb, 0x30fd, 
        0x3105, 0x3131, 0x3190, 0x3192, 0x3196, 0x31a0, 0x3200, 0x3220, 
        0x322a, 0x3260, 0x327f, 0x3281, 0x328a, 0x32c0, 0x32d0, 0x3300, 
        0x337b, 0x33e0, 0x3400, 0x4e00, 0xa000, 0xa490, 0xa4a4, 0xa4b5, 
        0xa4c2, 0xa4c6, 0xac00, 0xd800, 0xe000, 0xf900, 0xfb00, 0xfb13, 
        0xfb1d, 0xfb20, 0xfb29, 0xfb2b, 0xfb38, 0xfb3e, 0xfb40, 0xfb43, 
        0xfb46, 0xfbd3, 0xfd3e, 0xfd50, 0xfd92, 0xfdf0, 0xfe20, 0xfe30, 
        0xfe32, 0xfe34, 0xfe36, 0xfe49, 0xfe4d, 0xfe50, 0xfe54, 0xfe58, 
        0xfe5a, 0xfe5f, 0xfe62, 0xfe65, 0xfe68, 0xfe6b, 0xfe70, 0xfe74, 
        0xfe76, 0xfeff, 0xff01, 0xff04, 0xff06, 0xff08, 0xff0a, 0xff0d, 
        0xff0f, 0xff11, 0xff1a, 0xff1c, 0xff1f, 0xff21, 0xff3b, 0xff3d, 
        0xff3f, 0xff41, 0xff5b, 0xff5d, 0xff61, 0xff63, 0xff65, 0xff67, 
        0xff70, 0xff72, 0xff9e, 0xffa0, 0xffc2, 0xffca, 0xffd2, 0xffda, 
        0xffe0, 0xffe2, 0xffe4, 0xffe6, 0xffe8, 0xffea, 0xffed, 0xfff9, 
        0xfffc, 
    };

    private static final int[] typeValues = {
        0x001f, 0x000f, 0x0021, 0x180c, 0x0023, 0x0018, 0x0025, 0x181a, 
        0x0027, 0x0018, 0x0029, 0x1615, 0x002c, 0x1918, 0x002e, 0x1418, 
        0x0030, 0x1809, 0x0039, 0x0009, 0x003b, 0x0018, 0x003e, 0x0019, 
        0x0040, 0x0018, 0x005a, 0x0001, 0x005c, 0x1518, 0x005e, 0x161b, 
        0x0060, 0x171b, 0x007a, 0x0002, 0x007c, 0x1519, 0x007e, 0x1619, 
        0x009f, 0x000f, 0x00a1, 0x180c, 0x00a5, 0x001a, 0x00a7, 0x001c, 
        0x00a9, 0x1c1b, 0x00ab, 0x1d02, 0x00ad, 0x1419, 0x00b0, 0x1b1c, 
        0x00b2, 0x190b, 0x00b4, 0x0b1b, 0x00b6, 0x021c, 0x00b8, 0x181b, 
        0x00ba, 0x0b02, 0x00bc, 0x1e0b, 0x00be, 0x000b, 0x00c0, 0x1801, 
        0x00d6, 0x0001, 0x00d8, 0x1901, 0x00de, 0x0001, 0x00f6, 0x0002, 
        0x00f8, 0x1902, 0x00ff, 0x0002, 0x0137, 0x0201, 0x0148, 0x0102, 
        0x0178, 0x0201, 0x017e, 0x0102, 0x0180, 0x0002, 0x0182, 0x0001, 
        0x0186, 0x0201, 0x0189, 0x0102, 0x018b, 0x0001, 0x018d, 0x0002, 
        0x0191, 0x0001, 0x0193, 0x0102, 0x0196, 0x0201, 0x0198, 0x0001, 
        0x019b, 0x0002, 0x019d, 0x0001, 0x019f, 0x0102, 0x01a6, 0x0201, 
        0x01aa, 0x0102, 0x01ae, 0x0201, 0x01b1, 0x0102, 0x01b3, 0x0001, 
        0x01b7, 0x0102, 0x01b9, 0x0201, 0x01bb, 0x0502, 0x01bd, 0x0201, 
        0x01bf, 0x0002, 0x01c3, 0x0005, 0x01c5, 0x0301, 0x01c7, 0x0102, 
        0x01c9, 0x0203, 0x01cb, 0x0301, 0x01dc, 0x0102, 0x01ef, 0x0201, 
        0x01f1, 0x0102, 0x01f3, 0x0203, 0x01f6, 0x0201, 0x01f8, 0x0001, 
        0x021f, 0x0201, 0x0233, 0x0201, 0x02ad, 0x0002, 0x02b8, 0x0004, 
        0x02ba, 0x001b, 0x02c1, 0x0004, 0x02cf, 0x001b, 0x02d1, 0x0004, 
        0x02df, 0x001b, 0x02e4, 0x0004, 0x02ed, 0x001b, 0x02ee, 0x0004, 
        0x034e, 0x0006, 0x0362, 0x0006, 0x0375, 0x001b, 0x037a, 0x0004, 
        0x037e, 0x0018, 0x0385, 0x001b, 0x0388, 0x1801, 0x038a, 0x0001, 
        0x038c, 0x0001, 0x038f, 0x0001, 0x0391, 0x0102, 0x03a1, 0x0001, 
        0x03ab, 0x0001, 0x03ce, 0x0002, 0x03d1, 0x0002, 0x03d4, 0x0001, 
        0x03d7, 0x0002, 0x03ef, 0x0201, 0x03f3, 0x0002, 0x042f, 0x0001, 
        0x045f, 0x0002, 0x0481, 0x0201, 0x0483, 0x061c, 0x0486, 0x0006, 
        0x0489, 0x0007, 0x04c0, 0x0201, 0x04c4, 0x0102, 0x04c8, 0x0102, 
        0x04cc, 0x0102, 0x04f5, 0x0201, 0x04f9, 0x0201, 0x0556, 0x0001, 
        0x055a, 0x0418, 0x055f, 0x0018, 0x0587, 0x0002, 0x058a, 0x1814, 
        0x05a1, 0x0006, 0x05b9, 0x0006, 0x05bd, 0x0006, 0x05c1, 0x0618, 
        0x05c4, 0x1806, 0x05ea, 0x0005, 0x05f2, 0x0005, 0x05f4, 0x0018, 
        0x060c, 0x0018, 0x061b, 0x1800, 0x061f, 0x1800, 0x063a, 0x0005, 
        0x0641, 0x0504, 0x064a, 0x0005, 0x0655, 0x0006, 0x0669, 0x0009, 
        0x066d, 0x0018, 0x0671, 0x0506, 0x06d3, 0x0005, 0x06d5, 0x0518, 
        0x06dc, 0x0006, 0x06de, 0x0007, 0x06e4, 0x0006, 0x06e6, 0x0004, 
        0x06e8, 0x0006, 0x06ea, 0x1c06, 0x06ed, 0x0006, 0x06f9, 0x0009, 
        0x06fc, 0x0005, 0x06fe, 0x001c, 0x070d, 0x0018, 0x0710, 0x1005, 
        0x0712, 0x0605, 0x072c, 0x0005, 0x074a, 0x0006, 0x07a5, 0x0005, 
        0x07b0, 0x0006, 0x0902, 0x0006, 0x0903, 0x0800, 0x0939, 0x0005, 
        0x093d, 0x0506, 0x0940, 0x0008, 0x0948, 0x0006, 0x094c, 0x0008, 
        0x094d, 0x0600, 0x0951, 0x0605, 0x0954, 0x0006, 0x0961, 0x0005, 
        0x0963, 0x0006, 0x0965, 0x0018, 0x096f, 0x0009, 0x0970, 0x0018, 
        0x0982, 0x0608, 0x0983, 0x0800, 0x098c, 0x0005, 0x0990, 0x0005, 
        0x09a8, 0x0005, 0x09b0, 0x0005, 0x09b2, 0x0005, 0x09b9, 0x0005, 
        0x09bc, 0x0006, 0x09c0, 0x0008, 0x09c4, 0x0006, 0x09c8, 0x0008, 
        0x09cc, 0x0008, 0x09cd, 0x0600, 0x09d7, 0x0800, 0x09dd, 0x0005, 
        0x09e1, 0x0005, 0x09e3, 0x0006, 0x09ef, 0x0009, 0x09f1, 0x0005, 
        0x09f3, 0x001a, 0x09f9, 0x000b, 0x09fa, 0x001c, 0x0a02, 0x0006, 
        0x0a0a, 0x0005, 0x0a10, 0x0005, 0x0a28, 0x0005, 0x0a30, 0x0005, 
        0x0a33, 0x0005, 0x0a36, 0x0005, 0x0a39, 0x0005, 0x0a3c, 0x0006, 
        0x0a40, 0x0008, 0x0a42, 0x0006, 0x0a48, 0x0006, 0x0a4d, 0x0006, 
        0x0a5c, 0x0005, 0x0a5e, 0x0005, 0x0a6f, 0x0009, 0x0a71, 0x0006, 
        0x0a74, 0x0005, 0x0a82, 0x0006, 0x0a83, 0x0800, 0x0a8b, 0x0005, 
        0x0a8d, 0x0500, 0x0a91, 0x0005, 0x0aa8, 0x0005, 0x0ab0, 0x0005, 
        0x0ab3, 0x0005, 0x0ab9, 0x0005, 0x0abd, 0x0506, 0x0ac0, 0x0008, 
        0x0ac5, 0x0006, 0x0ac8, 0x0006, 0x0ac9, 0x0800, 0x0acc, 0x0008, 
        0x0acd, 0x0600, 0x0ad0, 0x0005, 0x0ae0, 0x0005, 0x0aef, 0x0009, 
        0x0b02, 0x0608, 0x0b03, 0x0800, 0x0b0c, 0x0005, 0x0b10, 0x0005, 
        0x0b28, 0x0005, 0x0b30, 0x0005, 0x0b33, 0x0005, 0x0b39, 0x0005, 
        0x0b3d, 0x0506, 0x0b41, 0x0608, 0x0b43, 0x0006, 0x0b48, 0x0008, 
        0x0b4c, 0x0008, 0x0b4d, 0x0600, 0x0b57, 0x0806, 0x0b5d, 0x0005, 
        0x0b61, 0x0005, 0x0b6f, 0x0009, 0x0b70, 0x001c, 0x0b83, 0x0806, 
        0x0b8a, 0x0005, 0x0b90, 0x0005, 0x0b95, 0x0005, 0x0b9a, 0x0005, 
        0x0b9c, 0x0005, 0x0b9f, 0x0005, 0x0ba4, 0x0005, 0x0baa, 0x0005, 
        0x0bb5, 0x0005, 0x0bb9, 0x0005, 0x0bbf, 0x0008, 0x0bc1, 0x0806, 
        0x0bc2, 0x0008, 0x0bc8, 0x0008, 0x0bcc, 0x0008, 0x0bcd, 0x0600, 
        0x0bd7, 0x0800, 0x0bef, 0x0009, 0x0bf2, 0x000b, 0x0c03, 0x0008, 
        0x0c0c, 0x0005, 0x0c10, 0x0005, 0x0c28, 0x0005, 0x0c33, 0x0005, 
        0x0c39, 0x0005, 0x0c40, 0x0006, 0x0c44, 0x0008, 0x0c48, 0x0006, 
        0x0c4d, 0x0006, 0x0c56, 0x0006, 0x0c61, 0x0005, 0x0c6f, 0x0009, 
        0x0c83, 0x0008, 0x0c8c, 0x0005, 0x0c90, 0x0005, 0x0ca8, 0x0005, 
        0x0cb3, 0x0005, 0x0cb9, 0x0005, 0x0cc0, 0x0608, 0x0cc4, 0x0008, 
        0x0cc7, 0x0806, 0x0cc8, 0x0008, 0x0ccb, 0x0008, 0x0ccd, 0x0006, 
        0x0cd6, 0x0008, 0x0cde, 0x0005, 0x0ce1, 0x0005, 0x0cef, 0x0009, 
        0x0d03, 0x0008, 0x0d0c, 0x0005, 0x0d10, 0x0005, 0x0d28, 0x0005, 
        0x0d39, 0x0005, 0x0d40, 0x0008, 0x0d43, 0x0006, 0x0d48, 0x0008, 
        0x0d4c, 0x0008, 0x0d4d, 0x0600, 0x0d57, 0x0800, 0x0d61, 0x0005, 
        0x0d6f, 0x0009, 0x0d83, 0x0008, 0x0d96, 0x0005, 0x0db1, 0x0005, 
        0x0dbb, 0x0005, 0x0dbd, 0x0500, 0x0dc6, 0x0005, 0x0dca, 0x0006, 
        0x0dd1, 0x0008, 0x0dd4, 0x0006, 0x0dd6, 0x0006, 0x0ddf, 0x0008, 
        0x0df3, 0x0008, 0x0df4, 0x0018, 0x0e30, 0x0005, 0x0e32, 0x0605, 
        0x0e34, 0x0506, 0x0e3a, 0x0006, 0x0e40, 0x1a05, 0x0e45, 0x0005, 
        0x0e47, 0x0604, 0x0e4e, 0x0006, 0x0e50, 0x1809, 0x0e59, 0x0009, 
        0x0e5b, 0x0018, 0x0e82, 0x0005, 0x0e84, 0x0005, 0x0e88, 0x0005, 
        0x0e8a, 0x0005, 0x0e8d, 0x0500, 0x0e97, 0x0005, 0x0e9f, 0x0005, 
        0x0ea3, 0x0005, 0x0ea5, 0x0500, 0x0ea7, 0x0500, 0x0eab, 0x0005, 
        0x0eb0, 0x0005, 0x0eb2, 0x0605, 0x0eb4, 0x0506, 0x0eb9, 0x0006, 
        0x0ebc, 0x0006, 0x0ebd, 0x0500, 0x0ec4, 0x0005, 0x0ec6, 0x0004, 
        0x0ecd, 0x0006, 0x0ed9, 0x0009, 0x0edd, 0x0005, 0x0f01, 0x1c05, 
        0x0f03, 0x001c, 0x0f12, 0x0018, 0x0f17, 0x001c, 0x0f19, 0x0006, 
        0x0f1f, 0x001c, 0x0f29, 0x0009, 0x0f33, 0x000b, 0x0f39, 0x061c, 
        0x0f3d, 0x1615, 0x0f3f, 0x0008, 0x0f47, 0x0005, 0x0f6a, 0x0005, 
        0x0f7e, 0x0006, 0x0f80, 0x0806, 0x0f84, 0x0006, 0x0f86, 0x1806, 
        0x0f88, 0x0605, 0x0f8b, 0x0005, 0x0f97, 0x0006, 0x0fbc, 0x0006, 
        0x0fc5, 0x001c, 0x0fc7, 0x1c06, 0x0fcc, 0x001c, 0x0fcf, 0x1c00, 
        0x1021, 0x0005, 0x1027, 0x0005, 0x102a, 0x0005, 0x102d, 0x0608, 
        0x1030, 0x0006, 0x1032, 0x0806, 0x1037, 0x0006, 0x1039, 0x0608, 
        0x1049, 0x0009, 0x104f, 0x0018, 0x1055, 0x0005, 0x1057, 0x0008, 
        0x1059, 0x0006, 0x10c5, 0x0001, 0x10f6, 0x0005, 0x10fb, 0x1800, 
        0x1159, 0x0005, 0x11a2, 0x0005, 0x11f9, 0x0005, 0x1206, 0x0005, 
        0x1246, 0x0005, 0x1248, 0x0005, 0x124d, 0x0005, 0x1256, 0x0005, 
        0x1258, 0x0005, 0x125d, 0x0005, 0x1286, 0x0005, 0x1288, 0x0005, 
        0x128d, 0x0005, 0x12ae, 0x0005, 0x12b0, 0x0005, 0x12b5, 0x0005, 
        0x12be, 0x0005, 0x12c0, 0x0005, 0x12c5, 0x0005, 0x12ce, 0x0005, 
        0x12d6, 0x0005, 0x12ee, 0x0005, 0x130e, 0x0005, 0x1310, 0x0005, 
        0x1315, 0x0005, 0x131e, 0x0005, 0x1346, 0x0005, 0x135a, 0x0005, 
        0x1368, 0x0018, 0x1371, 0x0009, 0x137c, 0x000b, 0x13f4, 0x0005, 
        0x166c, 0x0005, 0x166e, 0x0018, 0x1676, 0x0005, 0x1681, 0x050c, 
        0x169a, 0x0005, 0x169c, 0x1516, 0x16ea, 0x0005, 0x16ed, 0x0018, 
        0x16f0, 0x000b, 0x17b3, 0x0005, 0x17b6, 0x0008, 0x17bd, 0x0006, 
        0x17c5, 0x0008, 0x17c7, 0x0806, 0x17c9, 0x0608, 0x17d3, 0x0006, 
        0x17da, 0x0018, 0x17dc, 0x1a18, 0x17e9, 0x0009, 0x1805, 0x0018, 
        0x1807, 0x1814, 0x180a, 0x0018, 0x180e, 0x0010, 0x1819, 0x0009, 
        0x1842, 0x0005, 0x1844, 0x0405, 0x1877, 0x0005, 0x18a8, 0x0005, 
        0x18a9, 0x0600, 0x1e95, 0x0201, 0x1e9b, 0x0002, 0x1ef9, 0x0201, 
        0x1f07, 0x0002, 0x1f0f, 0x0001, 0x1f15, 0x0002, 0x1f1d, 0x0001, 
        0x1f27, 0x0002, 0x1f2f, 0x0001, 0x1f37, 0x0002, 0x1f3f, 0x0001, 
        0x1f45, 0x0002, 0x1f4d, 0x0001, 0x1f57, 0x0002, 0x1f59, 0x0100, 
        0x1f5b, 0x0100, 0x1f5d, 0x0100, 0x1f60, 0x0102, 0x1f67, 0x0002, 
        0x1f6f, 0x0001, 0x1f7d, 0x0002, 0x1f87, 0x0002, 0x1f8f, 0x0003, 
        0x1f97, 0x0002, 0x1f9f, 0x0003, 0x1fa7, 0x0002, 0x1faf, 0x0003, 
        0x1fb4, 0x0002, 0x1fb7, 0x0002, 0x1fbb, 0x0001, 0x1fbd, 0x1b03, 
        0x1fbf, 0x1b02, 0x1fc1, 0x001b, 0x1fc4, 0x0002, 0x1fc7, 0x0002, 
        0x1fcb, 0x0001, 0x1fcd, 0x1b03, 0x1fcf, 0x001b, 0x1fd3, 0x0002, 
        0x1fd7, 0x0002, 0x1fdb, 0x0001, 0x1fdf, 0x001b, 0x1fe7, 0x0002, 
        0x1fec, 0x0001, 0x1fef, 0x001b, 0x1ff4, 0x0002, 0x1ff7, 0x0002, 
        0x1ffb, 0x0001, 0x1ffd, 0x1b03, 0x1ffe, 0x001b, 0x200b, 0x000c, 
        0x200f, 0x0010, 0x2015, 0x0014, 0x2017, 0x0018, 0x2019, 0x1e1d, 
        0x201b, 0x1d15, 0x201d, 0x1e1d, 0x201f, 0x1d15, 0x2027, 0x0018, 
        0x2029, 0x0e0d, 0x202e, 0x0010, 0x2030, 0x0c18, 0x2038, 0x0018, 
        0x203a, 0x1d1e, 0x203e, 0x0018, 0x2040, 0x0017, 0x2043, 0x0018, 
        0x2045, 0x1519, 0x2046, 0x0016, 0x204d, 0x0018, 0x206f, 0x0010, 
        0x2070, 0x000b, 0x2079, 0x000b, 0x207c, 0x0019, 0x207e, 0x1516, 
        0x2080, 0x020b, 0x2089, 0x000b, 0x208c, 0x0019, 0x208e, 0x1516, 
        0x20af, 0x001a, 0x20dc, 0x0006, 0x20e0, 0x0007, 0x20e2, 0x0607, 
        0x20e3, 0x0700, 0x2101, 0x001c, 0x2103, 0x1c01, 0x2106, 0x001c, 
        0x2108, 0x011c, 0x210a, 0x1c02, 0x210d, 0x0001, 0x210f, 0x0002, 
        0x2112, 0x0001, 0x2114, 0x021c, 0x2116, 0x011c, 0x2118, 0x001c, 
        0x211d, 0x0001, 0x2123, 0x001c, 0x212a, 0x1c01, 0x212d, 0x0001, 
        0x212f, 0x021c, 0x2131, 0x0001, 0x2133, 0x011c, 0x2135, 0x0502, 
        0x2138, 0x0005, 0x213a, 0x021c, 0x215f, 0x000b, 0x2183, 0x000a, 
        0x2194, 0x0019, 0x2199, 0x001c, 0x219b, 0x0019, 0x219f, 0x001c, 
        0x21a1, 0x1c19, 0x21a4, 0x191c, 0x21a7, 0x1c19, 0x21ad, 0x001c, 
        0x21af, 0x1c19, 0x21cd, 0x001c, 0x21cf, 0x0019, 0x21d1, 0x001c, 
        0x21d5, 0x1c19, 0x21f3, 0x001c, 0x22f1, 0x0019, 0x2307, 0x001c, 
        0x230b, 0x0019, 0x231f, 0x001c, 0x2321, 0x0019, 0x2328, 0x001c, 
        0x232a, 0x1516, 0x237b, 0x001c, 0x239a, 0x001c, 0x2426, 0x001c, 
        0x244a, 0x001c, 0x249b, 0x000b, 0x24e9, 0x001c, 0x24ea, 0x000b, 
        0x2595, 0x001c, 0x25b6, 0x001c, 0x25b8, 0x191c, 0x25c0, 0x001c, 
        0x25c2, 0x191c, 0x25f7, 0x001c, 0x2613, 0x001c, 0x266e, 0x001c, 
        0x2670, 0x191c, 0x2671, 0x1c00, 0x2704, 0x001c, 0x2709, 0x001c, 
        0x2727, 0x001c, 0x274b, 0x001c, 0x274d, 0x1c00, 0x2752, 0x001c, 
        0x2756, 0x001c, 0x275e, 0x001c, 0x2767, 0x001c, 0x2793, 0x000b, 
        0x2794, 0x001c, 0x27af, 0x001c, 0x27be, 0x001c, 0x28ff, 0x001c, 
        0x2e99, 0x001c, 0x2ef3, 0x001c, 0x2fd5, 0x001c, 0x2ffb, 0x001c, 
        0x3001, 0x180c, 0x3003, 0x0018, 0x3005, 0x041c, 0x3007, 0x0a05, 
        0x3011, 0x1615, 0x3013, 0x001c, 0x301b, 0x1615, 0x301d, 0x1514, 
        0x301f, 0x0016, 0x3021, 0x0a1c, 0x3029, 0x000a, 0x302f, 0x0006, 
        0x3031, 0x0414, 0x3035, 0x0004, 0x3037, 0x001c, 0x303a, 0x000a, 
        0x303f, 0x001c, 0x3094, 0x0005, 0x309a, 0x0006, 0x309c, 0x001b, 
        0x309e, 0x0004, 0x30fa, 0x0005, 0x30fc, 0x1704, 0x30fe, 0x0004, 
        0x312c, 0x0005, 0x318e, 0x0005, 0x3191, 0x001c, 0x3195, 0x000b, 
        0x319f, 0x001c, 0x31b7, 0x0005, 0x321c, 0x001c, 0x3229, 0x000b, 
        0x3243, 0x001c, 0x327b, 0x001c, 0x3280, 0x1c0b, 0x3289, 0x000b, 
        0x32b0, 0x001c, 0x32cb, 0x001c, 0x32fe, 0x001c, 0x3376, 0x001c, 
        0x33dd, 0x001c, 0x33fe, 0x001c, 0x4db5, 0x0005, 0x9fa5, 0x0005, 
        0xa48c, 0x0005, 0xa4a1, 0x001c, 0xa4b3, 0x001c, 0xa4c0, 0x001c, 
        0xa4c4, 0x001c, 0xa4c6, 0x001c, 0xd7a3, 0x0005, 0xdfff, 0x0013, 
        0xf8ff, 0x0012, 0xfa2d, 0x0005, 0xfb06, 0x0002, 0xfb17, 0x0002, 
        0xfb1f, 0x0506, 0xfb28, 0x0005, 0xfb2a, 0x1905, 0xfb36, 0x0005, 
        0xfb3c, 0x0005, 0xfb3e, 0x0005, 0xfb41, 0x0005, 0xfb44, 0x0005, 
        0xfbb1, 0x0005, 0xfd3d, 0x0005, 0xfd3f, 0x1615, 0xfd8f, 0x0005, 
        0xfdc7, 0x0005, 0xfdfb, 0x0005, 0xfe23, 0x0006, 0xfe31, 0x1418, 
        0xfe33, 0x1714, 0xfe35, 0x1517, 0xfe44, 0x1516, 0xfe4c, 0x0018, 
        0xfe4f, 0x0017, 0xfe52, 0x0018, 0xfe57, 0x0018, 0xfe59, 0x1514, 
        0xfe5e, 0x1516, 0xfe61, 0x0018, 0xfe64, 0x1419, 0xfe66, 0x0019, 
        0xfe6a, 0x1a18, 0xfe6b, 0x1800, 0xfe72, 0x0005, 0xfe74, 0x0005, 
        0xfefc, 0x0005, 0xfeff, 0x1000, 0xff03, 0x0018, 0xff05, 0x181a, 
        0xff07, 0x0018, 0xff09, 0x1615, 0xff0c, 0x1918, 0xff0e, 0x1418, 
        0xff10, 0x1809, 0xff19, 0x0009, 0xff1b, 0x0018, 0xff1e, 0x0019, 
        0xff20, 0x0018, 0xff3a, 0x0001, 0xff3c, 0x1518, 0xff3e, 0x161b, 
        0xff40, 0x171b, 0xff5a, 0x0002, 0xff5c, 0x1519, 0xff5e, 0x1619, 
        0xff62, 0x1815, 0xff64, 0x1618, 0xff66, 0x1705, 0xff6f, 0x0005, 
        0xff71, 0x0504, 0xff9d, 0x0005, 0xff9f, 0x0004, 0xffbe, 0x0005, 
        0xffc7, 0x0005, 0xffcf, 0x0005, 0xffd7, 0x0005, 0xffdc, 0x0005, 
        0xffe1, 0x001a, 0xffe3, 0x1b19, 0xffe5, 0x1a1c, 0xffe6, 0x001a, 
        0xffe9, 0x191c, 0xffec, 0x0019, 0xffee, 0x001c, 0xfffb, 0x0010, 
        0xfffd, 0x001c, 
    };

    private static final int[] typeValuesCache = {
    	15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 
    	15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 
    	12, 24, 24, 24, 26, 24, 24, 24, 21, 22, 24, 25, 24, 20, 24, 24, 
    	9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 24, 24, 25, 25, 25, 24, 
    	24, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
    	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 21, 24, 22, 27, 23, 
    	27, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 21, 25, 22, 25, 15, 
    	15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 
    	15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 
    	12, 24, 26, 26, 26, 26, 28, 28, 27, 28, 2, 29, 25, 16, 28, 27, 
    	28, 25, 11, 11, 27, 2, 28, 24, 27, 11, 2, 30, 11, 11, 11, 24, 
    	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
    	1, 1, 1, 1, 1, 1, 1, 25, 1, 1, 1, 1, 1, 1, 1, 2, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	2, 2, 2, 2, 2, 2, 2, 25, 2, 2, 2, 2, 2, 2, 2, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 
    	2, 1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 
    	2, 1, 1, 2, 1, 2, 1, 1, 2, 1, 1, 1, 2, 2, 1, 1, 
    	1, 1, 2, 1, 1, 2, 1, 1, 1, 2, 2, 2, 1, 1, 2, 1, 
    	1, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 1, 1, 
    	2, 1, 1, 1, 2, 1, 2, 1, 1, 2, 2, 5, 1, 2, 2, 2, 
    	5, 5, 5, 5, 1, 3, 2, 1, 3, 2, 1, 3, 2, 1, 2, 1, 
    	2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	2, 1, 3, 2, 1, 2, 1, 1, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
    	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 
    	4, 4, 27, 27, 27, 27, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 
    	4, 4, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 
    	4, 4, 4, 4, 4, 27, 27, 27, 27, 27, 27, 27, 27, 27, 4, 27, 
    	27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 
    	6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
    	6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
    	6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
    	6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
    	6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
    	6, 6, 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0, 6, 6, 6, 
    	6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 
    	0, 0, 0, 0, 27, 27, 0, 0, 0, 0, 4, 0, 0, 0, 24, 0, 
    	0, 0, 0, 0, 27, 27, 1, 24, 1, 1, 1, 0, 1, 0, 1, 1, 
    	2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
    	1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 
    	2, 2, 1, 1, 1, 2, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 
    	1, 2, 1, 2, 1, 2, 1, 2 };
    
    // Unicode 3.0.1 (same as Unicode 3.0.0)
    private static final int[] uppercaseKeys = {
        0x0061, 0x00b5, 0x00e0, 0x00f8, 0x00ff, 0x0101, 0x0131, 0x0133, 
        0x013a, 0x014b, 0x017a, 0x017f, 0x0183, 0x0188, 0x018c, 0x0192, 
        0x0195, 0x0199, 0x01a1, 0x01a8, 0x01ad, 0x01b0, 0x01b4, 0x01b9, 
        0x01bd, 0x01bf, 0x01c5, 0x01c6, 0x01c8, 0x01c9, 0x01cb, 0x01cc, 
        0x01ce, 0x01dd, 0x01df, 0x01f2, 0x01f3, 0x01f5, 0x01f9, 0x0223, 
        0x0253, 0x0254, 0x0256, 0x0259, 0x025b, 0x0260, 0x0263, 0x0268, 
        0x0269, 0x026f, 0x0272, 0x0275, 0x0280, 0x0283, 0x0288, 0x028a, 
        0x0292, 0x0345, 0x03ac, 0x03ad, 0x03b1, 0x03c2, 0x03c3, 0x03cc, 
        0x03cd, 0x03d0, 0x03d1, 0x03d5, 0x03d6, 0x03db, 0x03f0, 0x03f1, 
        0x03f2, 0x0430, 0x0450, 0x0461, 0x048d, 0x04c2, 0x04c8, 0x04cc, 
        0x04d1, 0x04f9, 0x0561, 0x1e01, 0x1e9b, 0x1ea1, 0x1f00, 0x1f10, 
        0x1f20, 0x1f30, 0x1f40, 0x1f51, 0x1f60, 0x1f70, 0x1f72, 0x1f76, 
        0x1f78, 0x1f7a, 0x1f7c, 0x1f80, 0x1f90, 0x1fa0, 0x1fb0, 0x1fb3, 
        0x1fbe, 0x1fc3, 0x1fd0, 0x1fe0, 0x1fe5, 0x1ff3, 0x2170, 0x24d0, 
        0xff41, 
    };

    private static final int[] uppercaseValues = {
        0x007a, 0xffe0, 0x00b5, 0x02e7, 0x00f6, 0xffe0, 0x00fe, 0xffe0, 
        0x00ff, 0x0079, 0x812f, 0xffff, 0x0131, 0xff18, 0x8137, 0xffff, 
        0x8148, 0xffff, 0x8177, 0xffff, 0x817e, 0xffff, 0x017f, 0xfed4, 
        0x8185, 0xffff, 0x0188, 0xffff, 0x018c, 0xffff, 0x0192, 0xffff, 
        0x0195, 0x0061, 0x0199, 0xffff, 0x81a5, 0xffff, 0x01a8, 0xffff, 
        0x01ad, 0xffff, 0x01b0, 0xffff, 0x81b6, 0xffff, 0x01b9, 0xffff, 
        0x01bd, 0xffff, 0x01bf, 0x0038, 0x01c5, 0xffff, 0x01c6, 0xfffe, 
        0x01c8, 0xffff, 0x01c9, 0xfffe, 0x01cb, 0xffff, 0x01cc, 0xfffe, 
        0x81dc, 0xffff, 0x01dd, 0xffb1, 0x81ef, 0xffff, 0x01f2, 0xffff, 
        0x01f3, 0xfffe, 0x01f5, 0xffff, 0x821f, 0xffff, 0x8233, 0xffff, 
        0x0253, 0xff2e, 0x0254, 0xff32, 0x0257, 0xff33, 0x0259, 0xff36, 
        0x025b, 0xff35, 0x0260, 0xff33, 0x0263, 0xff31, 0x0268, 0xff2f, 
        0x0269, 0xff2d, 0x026f, 0xff2d, 0x0272, 0xff2b, 0x0275, 0xff2a, 
        0x0280, 0xff26, 0x0283, 0xff26, 0x0288, 0xff26, 0x028b, 0xff27, 
        0x0292, 0xff25, 0x0345, 0x0054, 0x03ac, 0xffda, 0x03af, 0xffdb, 
        0x03c1, 0xffe0, 0x03c2, 0xffe1, 0x03cb, 0xffe0, 0x03cc, 0xffc0, 
        0x03ce, 0xffc1, 0x03d0, 0xffc2, 0x03d1, 0xffc7, 0x03d5, 0xffd1, 
        0x03d6, 0xffca, 0x83ef, 0xffff, 0x03f0, 0xffaa, 0x03f1, 0xffb0, 
        0x03f2, 0xffb1, 0x044f, 0xffe0, 0x045f, 0xffb0, 0x8481, 0xffff, 
        0x84bf, 0xffff, 0x84c4, 0xffff, 0x04c8, 0xffff, 0x04cc, 0xffff, 
        0x84f5, 0xffff, 0x04f9, 0xffff, 0x0586, 0xffd0, 0x9e95, 0xffff, 
        0x1e9b, 0xffc5, 0x9ef9, 0xffff, 0x1f07, 0x0008, 0x1f15, 0x0008, 
        0x1f27, 0x0008, 0x1f37, 0x0008, 0x1f45, 0x0008, 0x9f57, 0x0008, 
        0x1f67, 0x0008, 0x1f71, 0x004a, 0x1f75, 0x0056, 0x1f77, 0x0064, 
        0x1f79, 0x0080, 0x1f7b, 0x0070, 0x1f7d, 0x007e, 0x1f87, 0x0008, 
        0x1f97, 0x0008, 0x1fa7, 0x0008, 0x1fb1, 0x0008, 0x1fb3, 0x0009, 
        0x1fbe, 0xe3db, 0x1fc3, 0x0009, 0x1fd1, 0x0008, 0x1fe1, 0x0008, 
        0x1fe5, 0x0007, 0x1ff3, 0x0009, 0x217f, 0xfff0, 0x24e9, 0xffe6, 
        0xff5a, 0xffe0, 
    };

    private static final int[] uppercaseValuesCache = {
    	924, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 
    	197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 
    	213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 192, 193, 194, 195, 196, 
    	197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 
    	213, 214, 247, 216, 217, 218, 219, 220, 221, 222, 376, 256, 256, 258, 258, 260, 
    	260, 262, 262, 264, 264, 266, 266, 268, 268, 270, 270, 272, 272, 274, 274, 276, 
    	276, 278, 278, 280, 280, 282, 282, 284, 284, 286, 286, 288, 288, 290, 290, 292, 
    	292, 294, 294, 296, 296, 298, 298, 300, 300, 302, 302, 304, 73, 306, 306, 308, 
    	308, 310, 310, 312, 313, 313, 315, 315, 317, 317, 319, 319, 321, 321, 323, 323, 
    	325, 325, 327, 327, 329, 330, 330, 332, 332, 334, 334, 336, 336, 338, 338, 340, 
    	340, 342, 342, 344, 344, 346, 346, 348, 348, 350, 350, 352, 352, 354, 354, 356, 
    	356, 358, 358, 360, 360, 362, 362, 364, 364, 366, 366, 368, 368, 370, 370, 372, 
    	372, 374, 374, 376, 377, 377, 379, 379, 381, 381, 83, 384, 385, 386, 386, 388, 
    	388, 390, 391, 391, 393, 394, 395, 395, 397, 398, 399, 400, 401, 401, 403, 404, 
    	502, 406, 407, 408, 408, 410, 411, 412, 413, 544, 415, 416, 416, 418, 418, 420, 
    	420, 422, 423, 423, 425, 426, 427, 428, 428, 430, 431, 431, 433, 434, 435, 435, 
    	437, 437, 439, 440, 440, 442, 443, 444, 444, 446, 503, 448, 449, 450, 451, 452, 
    	452, 452, 455, 455, 455, 458, 458, 458, 461, 461, 463, 463, 465, 465, 467, 467, 
    	469, 469, 471, 471, 473, 473, 475, 475, 398, 478, 478, 480, 480, 482, 482, 484, 
    	484, 486, 486, 488, 488, 490, 490, 492, 492, 494, 494, 496, 497, 497, 497, 500, 
    	500, 502, 503, 504, 504, 506, 506, 508, 508, 510, 510, 512, 512, 514, 514, 516, 
    	516, 518, 518, 520, 520, 522, 522, 524, 524, 526, 526, 528, 528, 530, 530, 532, 
    	532, 534, 534, 536, 536, 538, 538, 540, 540, 542, 542, 544, 545, 546, 546, 548, 
    	548, 550, 550, 552, 552, 554, 554, 556, 556, 558, 558, 560, 560, 562, 562, 564, 
    	565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 577, 578, 579, 580, 
    	581, 582, 583, 584, 585, 586, 587, 588, 589, 590, 591, 592, 593, 594, 385, 390, 
    	597, 393, 394, 600, 399, 602, 400, 604, 605, 606, 607, 403, 609, 610, 404, 612, 
    	613, 614, 615, 407, 406, 618, 619, 620, 621, 622, 412, 624, 625, 413, 627, 628, 
    	415, 630, 631, 632, 633, 634, 635, 636, 637, 638, 639, 422, 641, 642, 425, 644, 
    	645, 646, 647, 430, 649, 433, 434, 652, 653, 654, 655, 656, 657, 439, 659, 660, 
    	661, 662, 663, 664, 665, 666, 667, 668, 669, 670, 671, 672, 673, 674, 675, 676, 
    	677, 678, 679, 680, 681, 682, 683, 684, 685, 686, 687, 688, 689, 690, 691, 692, 
    	693, 694, 695, 696, 697, 698, 699, 700, 701, 702, 703, 704, 705, 706, 707, 708, 
    	709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 
    	725, 726, 727, 728, 729, 730, 731, 732, 733, 734, 735, 736, 737, 738, 739, 740, 
    	741, 742, 743, 744, 745, 746, 747, 748, 749, 750, 751, 752, 753, 754, 755, 756, 
    	757, 758, 759, 760, 761, 762, 763, 764, 765, 766, 767, 768, 769, 770, 771, 772, 
    	773, 774, 775, 776, 777, 778, 779, 780, 781, 782, 783, 784, 785, 786, 787, 788, 
    	789, 790, 791, 792, 793, 794, 795, 796, 797, 798, 799, 800, 801, 802, 803, 804, 
    	805, 806, 807, 808, 809, 810, 811, 812, 813, 814, 815, 816, 817, 818, 819, 820, 
    	821, 822, 823, 824, 825, 826, 827, 828, 829, 830, 831, 832, 833, 834, 835, 836, 
    	921, 838, 839, 840, 841, 842, 843, 844, 845, 846, 847, 848, 849, 850, 851, 852, 
    	853, 854, 855, 856, 857, 858, 859, 860, 861, 862, 863, 864, 865, 866, 867, 868, 
    	869, 870, 871, 872, 873, 874, 875, 876, 877, 878, 879, 880, 881, 882, 883, 884, 
    	885, 886, 887, 888, 889, 890, 891, 892, 893, 894, 895, 896, 897, 898, 899, 900, 
    	901, 902, 903, 904, 905, 906, 907, 908, 909, 910, 911, 912, 913, 914, 915, 916, 
    	917, 918, 919, 920, 921, 922, 923, 924, 925, 926, 927, 928, 929, 930, 931, 932, 
    	933, 934, 935, 936, 937, 938, 939, 902, 904, 905, 906, 944, 913, 914, 915, 916, 
    	917, 918, 919, 920, 921, 922, 923, 924, 925, 926, 927, 928, 929, 931, 931, 932, 
    	933, 934, 935, 936, 937, 938, 939, 908, 910, 911, 975, 914, 920, 978, 979, 980, 
    	934, 928, 983, 984, 984, 986, 986, 988, 988, 990, 990, 992, 992, 994, 994, 996, 
    	996, 998, 998} ;
    
    private static final int[] lowercaseKeys = {
        0x0041, 0x00c0, 0x00d8, 0x0100, 0x0130, 0x0132, 0x0139, 0x014a, 
        0x0178, 0x0179, 0x0181, 0x0182, 0x0186, 0x0187, 0x0189, 0x018b, 
        0x018e, 0x018f, 0x0190, 0x0191, 0x0193, 0x0194, 0x0196, 0x0197, 
        0x0198, 0x019c, 0x019d, 0x019f, 0x01a0, 0x01a6, 0x01a7, 0x01a9, 
        0x01ac, 0x01ae, 0x01af, 0x01b1, 0x01b3, 0x01b7, 0x01b8, 0x01bc, 
        0x01c4, 0x01c5, 0x01c7, 0x01c8, 0x01ca, 0x01cb, 0x01de, 0x01f1, 
        0x01f2, 0x01f6, 0x01f7, 0x01f8, 0x0222, 0x0386, 0x0388, 0x038c, 
        0x038e, 0x0391, 0x03a3, 0x03da, 0x0400, 0x0410, 0x0460, 0x048c, 
        0x04c1, 0x04c7, 0x04cb, 0x04d0, 0x04f8, 0x0531, 0x1e00, 0x1ea0, 
        0x1f08, 0x1f18, 0x1f28, 0x1f38, 0x1f48, 0x1f59, 0x1f68, 0x1f88, 
        0x1f98, 0x1fa8, 0x1fb8, 0x1fba, 0x1fbc, 0x1fc8, 0x1fcc, 0x1fd8, 
        0x1fda, 0x1fe8, 0x1fea, 0x1fec, 0x1ff8, 0x1ffa, 0x1ffc, 0x2126, 
        0x212a, 0x212b, 0x2160, 0x24b6, 0xff21, 
    };

    private static final int[] lowercaseValues = {
        0x005a, 0x0020, 0x00d6, 0x0020, 0x00de, 0x0020, 0x812e, 0x0001, 
        0x0130, 0xff39, 0x8136, 0x0001, 0x8147, 0x0001, 0x8176, 0x0001, 
        0x0178, 0xff87, 0x817d, 0x0001, 0x0181, 0x00d2, 0x8184, 0x0001, 
        0x0186, 0x00ce, 0x0187, 0x0001, 0x018a, 0x00cd, 0x018b, 0x0001, 
        0x018e, 0x004f, 0x018f, 0x00ca, 0x0190, 0x00cb, 0x0191, 0x0001, 
        0x0193, 0x00cd, 0x0194, 0x00cf, 0x0196, 0x00d3, 0x0197, 0x00d1, 
        0x0198, 0x0001, 0x019c, 0x00d3, 0x019d, 0x00d5, 0x019f, 0x00d6, 
        0x81a4, 0x0001, 0x01a6, 0x00da, 0x01a7, 0x0001, 0x01a9, 0x00da, 
        0x01ac, 0x0001, 0x01ae, 0x00da, 0x01af, 0x0001, 0x01b2, 0x00d9, 
        0x81b5, 0x0001, 0x01b7, 0x00db, 0x01b8, 0x0001, 0x01bc, 0x0001, 
        0x01c4, 0x0002, 0x01c5, 0x0001, 0x01c7, 0x0002, 0x01c8, 0x0001, 
        0x01ca, 0x0002, 0x81db, 0x0001, 0x81ee, 0x0001, 0x01f1, 0x0002, 
        0x81f4, 0x0001, 0x01f6, 0xff9f, 0x01f7, 0xffc8, 0x821e, 0x0001, 
        0x8232, 0x0001, 0x0386, 0x0026, 0x038a, 0x0025, 0x038c, 0x0040, 
        0x038f, 0x003f, 0x03a1, 0x0020, 0x03ab, 0x0020, 0x83ee, 0x0001, 
        0x040f, 0x0050, 0x042f, 0x0020, 0x8480, 0x0001, 0x84be, 0x0001, 
        0x84c3, 0x0001, 0x04c7, 0x0001, 0x04cb, 0x0001, 0x84f4, 0x0001, 
        0x04f8, 0x0001, 0x0556, 0x0030, 0x9e94, 0x0001, 0x9ef8, 0x0001, 
        0x1f0f, 0xfff8, 0x1f1d, 0xfff8, 0x1f2f, 0xfff8, 0x1f3f, 0xfff8, 
        0x1f4d, 0xfff8, 0x9f5f, 0xfff8, 0x1f6f, 0xfff8, 0x1f8f, 0xfff8, 
        0x1f9f, 0xfff8, 0x1faf, 0xfff8, 0x1fb9, 0xfff8, 0x1fbb, 0xffb6, 
        0x1fbc, 0xfff7, 0x1fcb, 0xffaa, 0x1fcc, 0xfff7, 0x1fd9, 0xfff8, 
        0x1fdb, 0xff9c, 0x1fe9, 0xfff8, 0x1feb, 0xff90, 0x1fec, 0xfff9, 
        0x1ff9, 0xff80, 0x1ffb, 0xff82, 0x1ffc, 0xfff7, 0x2126, 0xe2a3, 
        0x212a, 0xdf41, 0x212b, 0xdfba, 0x216f, 0x0010, 0x24cf, 0x001a, 
        0xff3a, 0x0020, 
    };

    private static final int[] lowercaseValuesCache = {
    	224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 
    	240, 241, 242, 243, 244, 245, 246, 215, 248, 249, 250, 251, 252, 253, 254, 223, 
    	224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 
    	240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255, 
    	257, 257, 259, 259, 261, 261, 263, 263, 265, 265, 267, 267, 269, 269, 271, 271, 
    	273, 273, 275, 275, 277, 277, 279, 279, 281, 281, 283, 283, 285, 285, 287, 287, 
    	289, 289, 291, 291, 293, 293, 295, 295, 297, 297, 299, 299, 301, 301, 303, 303, 
    	105, 305, 307, 307, 309, 309, 311, 311, 312, 314, 314, 316, 316, 318, 318, 320, 
    	320, 322, 322, 324, 324, 326, 326, 328, 328, 329, 331, 331, 333, 333, 335, 335, 
    	337, 337, 339, 339, 341, 341, 343, 343, 345, 345, 347, 347, 349, 349, 351, 351, 
    	353, 353, 355, 355, 357, 357, 359, 359, 361, 361, 363, 363, 365, 365, 367, 367, 
    	369, 369, 371, 371, 373, 373, 375, 375, 255, 378, 378, 380, 380, 382, 382, 383, 
    	384, 595, 387, 387, 389, 389, 596, 392, 392, 598, 599, 396, 396, 397, 477, 601, 
    	603, 402, 402, 608, 611, 405, 617, 616, 409, 409, 410, 411, 623, 626, 414, 629, 
    	417, 417, 419, 419, 421, 421, 640, 424, 424, 643, 426, 427, 429, 429, 648, 432, 
    	432, 650, 651, 436, 436, 438, 438, 658, 441, 441, 442, 443, 445, 445, 446, 447, 
    	448, 449, 450, 451, 454, 454, 454, 457, 457, 457, 460, 460, 460, 462, 462, 464, 
    	464, 466, 466, 468, 468, 470, 470, 472, 472, 474, 474, 476, 476, 477, 479, 479, 
    	481, 481, 483, 483, 485, 485, 487, 487, 489, 489, 491, 491, 493, 493, 495, 495, 
    	496, 499, 499, 499, 501, 501, 405, 447, 505, 505, 507, 507, 509, 509, 511, 511, 
    	513, 513, 515, 515, 517, 517, 519, 519, 521, 521, 523, 523, 525, 525, 527, 527, 
    	529, 529, 531, 531, 533, 533, 535, 535, 537, 537, 539, 539, 541, 541, 543, 543, 
    	414, 545, 547, 547, 549, 549, 551, 551, 553, 553, 555, 555, 557, 557, 559, 559, 
    	561, 561, 563, 563, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 
    	576, 577, 578, 579, 580, 581, 582, 583, 584, 585, 586, 587, 588, 589, 590, 591, 
    	592, 593, 594, 595, 596, 597, 598, 599, 600, 601, 602, 603, 604, 605, 606, 607, 
    	608, 609, 610, 611, 612, 613, 614, 615, 616, 617, 618, 619, 620, 621, 622, 623, 
    	624, 625, 626, 627, 628, 629, 630, 631, 632, 633, 634, 635, 636, 637, 638, 639, 
    	640, 641, 642, 643, 644, 645, 646, 647, 648, 649, 650, 651, 652, 653, 654, 655, 
    	656, 657, 658, 659, 660, 661, 662, 663, 664, 665, 666, 667, 668, 669, 670, 671, 
    	672, 673, 674, 675, 676, 677, 678, 679, 680, 681, 682, 683, 684, 685, 686, 687, 
    	688, 689, 690, 691, 692, 693, 694, 695, 696, 697, 698, 699, 700, 701, 702, 703, 
    	704, 705, 706, 707, 708, 709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 
    	720, 721, 722, 723, 724, 725, 726, 727, 728, 729, 730, 731, 732, 733, 734, 735, 
    	736, 737, 738, 739, 740, 741, 742, 743, 744, 745, 746, 747, 748, 749, 750, 751, 
    	752, 753, 754, 755, 756, 757, 758, 759, 760, 761, 762, 763, 764, 765, 766, 767, 
    	768, 769, 770, 771, 772, 773, 774, 775, 776, 777, 778, 779, 780, 781, 782, 783, 
    	784, 785, 786, 787, 788, 789, 790, 791, 792, 793, 794, 795, 796, 797, 798, 799, 
    	800, 801, 802, 803, 804, 805, 806, 807, 808, 809, 810, 811, 812, 813, 814, 815, 
    	816, 817, 818, 819, 820, 821, 822, 823, 824, 825, 826, 827, 828, 829, 830, 831, 
    	832, 833, 834, 835, 836, 837, 838, 839, 840, 841, 842, 843, 844, 845, 846, 847, 
    	848, 849, 850, 851, 852, 853, 854, 855, 856, 857, 858, 859, 860, 861, 862, 863, 
    	864, 865, 866, 867, 868, 869, 870, 871, 872, 873, 874, 875, 876, 877, 878, 879, 
    	880, 881, 882, 883, 884, 885, 886, 887, 888, 889, 890, 891, 892, 893, 894, 895, 
    	896, 897, 898, 899, 900, 901, 940, 903, 941, 942, 943, 907, 972, 909, 973, 974, 
    	912, 945, 946, 947, 948, 949, 950, 951, 952, 953, 954, 955, 956, 957, 958, 959, 
    	960, 961, 930, 963, 964, 965, 966, 967, 968, 969, 970, 971, 940, 941, 942, 943, 
    	944, 945, 946, 947, 948, 949, 950, 951, 952, 953, 954, 955, 956, 957, 958, 959, 
    	960, 961, 962, 963, 964, 965, 966, 967, 968, 969, 970, 971, 972, 973, 974, 975, 
    	976, 977, 978, 979, 980, 981, 982, 983, 985, 985, 987, 987, 989, 989, 991, 991, 
    	993, 993, 995, 995, 997, 997, 999, 999};
    
    private static final int[] digitKeys = {
        0x0030, 0x0041, 0x0061, 0x0660, 0x06f0, 0x0966, 0x09e6, 0x0a66, 
        0x0ae6, 0x0b66, 0x0be7, 0x0c66, 0x0ce6, 0x0d66, 0x0e50, 0x0ed0, 
        0x0f20, 0x1040, 0x1369, 0x17e0, 0x1810, 0xff10, 0xff21, 0xff41, 
    };

    private static final int[] digitValues = {
        0x0039, 0x0030, 0x005a, 0x0037, 0x007a, 0x0057, 0x0669, 0x0660, 
        0x06f9, 0x06f0, 0x096f, 0x0966, 0x09ef, 0x09e6, 0x0a6f, 0x0a66, 
        0x0aef, 0x0ae6, 0x0b6f, 0x0b66, 0x0bef, 0x0be6, 0x0c6f, 0x0c66, 
        0x0cef, 0x0ce6, 0x0d6f, 0x0d66, 0x0e59, 0x0e50, 0x0ed9, 0x0ed0, 
        0x0f29, 0x0f20, 0x1049, 0x1040, 0x1371, 0x1368, 0x17e9, 0x17e0, 
        0x1819, 0x1810, 0xff19, 0xff10, 0xff3a, 0xff17, 0xff5a, 0xff37, 
    };

    private static final char[] typeTags = {
        2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 2, 2, 
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 
        0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 
        0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 3, 
        0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 2, 
    };

    private static final int ISJAVASTART = 1;

    private static final int ISJAVAPART = 2;

    // Unicode 3.0.1 (same as Unicode 3.0.0)
    private static final String titlecaseKeys = "\u01c4\u01c6\u01c7\u01c9\u01ca\u01cc\u01f1\u01f3";

    private static final char[] titlecaseValues = 
        "\u01c5\u01c5\u01c8\u01c8\u01cb\u01cb\u01f2\u01f2".toCharArray();

    // Unicode 3.0.0 (NOT the same as Unicode 3.0.1)
    private static final int[] numericKeys = {
        0x0030, 0x0041, 0x0061, 0x00b2, 0x00b9, 0x00bc, 0x0660, 0x06f0, 
        0x0966, 0x09e6, 0x09f4, 0x09f9, 0x0a66, 0x0ae6, 0x0b66, 0x0be7, 
        0x0bf1, 0x0bf2, 0x0c66, 0x0ce6, 0x0d66, 0x0e50, 0x0ed0, 0x0f20, 
        0x1040, 0x1369, 0x1373, 0x1374, 0x1375, 0x1376, 0x1377, 0x1378, 
        0x1379, 0x137a, 0x137b, 0x137c, 0x16ee, 0x17e0, 0x1810, 0x2070, 
        0x2074, 0x2080, 0x2153, 0x215f, 0x2160, 0x216c, 0x216d, 0x216e, 
        0x216f, 0x2170, 0x217c, 0x217d, 0x217e, 0x217f, 0x2180, 0x2181, 
        0x2182, 0x2460, 0x2474, 0x2488, 0x24ea, 0x2776, 0x2780, 0x278a, 
        0x3007, 0x3021, 0x3038, 0x3039, 0x303a, 0x3280, 0xff10, 0xff21, 
        0xff41, 
    };

    private static final char[] numericValues = {
        0x0039, 0x0030, 0x005a, 0x0037, 0x007a, 0x0057, 0x00b3, 0x00b0, 
        0x00b9, 0x00b8, 0x00be, 0x0000, 0x0669, 0x0660, 0x06f9, 0x06f0, 
        0x096f, 0x0966, 0x09ef, 0x09e6, 0x09f7, 0x09f3, 0x09f9, 0x09e9, 
        0x0a6f, 0x0a66, 0x0aef, 0x0ae6, 0x0b6f, 0x0b66, 0x0bf0, 0x0be6, 
        0x0bf1, 0x0b8d, 0x0bf2, 0x080a, 0x0c6f, 0x0c66, 0x0cef, 0x0ce6, 
        0x0d6f, 0x0d66, 0x0e59, 0x0e50, 0x0ed9, 0x0ed0, 0x0f29, 0x0f20, 
        0x1049, 0x1040, 0x1372, 0x1368, 0x1373, 0x135f, 0x1374, 0x1356, 
        0x1375, 0x134d, 0x1376, 0x1344, 0x1377, 0x133b, 0x1378, 0x1332, 
        0x1379, 0x1329, 0x137a, 0x1320, 0x137b, 0x1317, 0x137c, 0xec6c, 
        0x16f0, 0x16dd, 0x17e9, 0x17e0, 0x1819, 0x1810, 0x2070, 0x2070, 
        0x2079, 0x2070, 0x2089, 0x2080, 0x215e, 0x0000, 0x215f, 0x215e, 
        0x216b, 0x215f, 0x216c, 0x213a, 0x216d, 0x2109, 0x216e, 0x1f7a, 
        0x216f, 0x1d87, 0x217b, 0x216f, 0x217c, 0x214a, 0x217d, 0x2119, 
        0x217e, 0x1f8a, 0x217f, 0x1d97, 0x2180, 0x1d98, 0x2181, 0x0df9, 
        0x2182, 0xfa72, 0x2473, 0x245f, 0x2487, 0x2473, 0x249b, 0x2487, 
        0x24ea, 0x24ea, 0x277f, 0x2775, 0x2789, 0x277f, 0x2793, 0x2789, 
        0x3007, 0x3007, 0x3029, 0x3020, 0x3038, 0x302e, 0x3039, 0x3025, 
        0x303a, 0x301c, 0x3289, 0x327f, 0xff19, 0xff10, 0xff3a, 0xff17, 
        0xff5a, 0xff37, 
    };

    /**
     * Constructs a new {@code Character} with the specified primitive char
     * value.
     * 
     * @param value
     *            the primitive char value to store in the new instance.
     */
    public Character(char value) {
        this.value = value;
    }

    /**
     * Gets the primitive value of this character.
     * 
     * @return this object's primitive value.
     */
    public char charValue() {
        return value;
    }

    /**
     * Compares this object to the specified character object to determine their
     * relative order.
     * 
     * @param c
     *            the character object to compare this object to.
     * @return {@code 0} if the value of this character and the value of
     *         {@code c} are equal; a positive value if the value of this
     *         character is greater than the value of {@code c}; a negative
     *         value if the value of this character is less than the value of
     *         {@code c}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Character c) {
        return value - ((Character) c).value;
    }
    
    /**
     * Returns a {@code Character} instance for the {@code char} value passed.
     * For ASCII/Latin-1 characters (and generally all characters with a Unicode
     * value up to 512), this method should be used instead of the constructor,
     * as it maintains a cache of corresponding {@code Character} instances.
     *
     * @param c
     *            the char value for which to get a {@code Character} instance.
     * @return the {@code Character} instance for {@code c}.
     * @since 1.5
     */
    public static Character valueOf(char c) {
        if (c >= CACHE_LEN ) {
            return new Character(c);
        }
        return valueOfCache.CACHE[c];
    }

    private static final int CACHE_LEN = 512;

    static class valueOfCache {
        /*
        * Provides a cache for the 'valueOf' method. A size of 512 should cache the
        * first couple pages of Unicode, which includes the ASCII/Latin-1
        * characters, which other parts of this class are optimized for.
        */
        private static final Character[] CACHE = new Character[CACHE_LEN ];

        static {
            for(int i=0; i<CACHE.length; i++){
                CACHE[i] =  new Character((char)i);
            }
        }
    }
    /**
     * Indicates whether {@code codePoint} is a valid Unicode code point.
     *
     * @param codePoint
     *            the code point to test.
     * @return {@code true} if {@code codePoint} is a valid Unicode code point;
     *         {@code false} otherwise.
     * @since 1.5
     */
    public static boolean isValidCodePoint(int codePoint) {
        return (MIN_CODE_POINT <= codePoint && MAX_CODE_POINT >= codePoint);
    }

    /**
     * Indicates whether {@code codePoint} is within the supplementary code
     * point range.
     *
     * @param codePoint
     *            the code point to test.
     * @return {@code true} if {@code codePoint} is within the supplementary
     *         code point range; {@code false} otherwise.
     * @since 1.5
     */
    public static boolean isSupplementaryCodePoint(int codePoint) {
        return (MIN_SUPPLEMENTARY_CODE_POINT <= codePoint && MAX_CODE_POINT >= codePoint);
    }

    /**
     * Indicates whether {@code ch} is a high- (or leading-) surrogate code unit
     * that is used for representing supplementary characters in UTF-16
     * encoding.
     *
     * @param ch
     *            the character to test.
     * @return {@code true} if {@code ch} is a high-surrogate code unit;
     *         {@code false} otherwise.
     * @see #isLowSurrogate(char)
     * @since 1.5
     */
    public static boolean isHighSurrogate(char ch) {
        return (MIN_HIGH_SURROGATE <= ch && MAX_HIGH_SURROGATE >= ch);
    }

    /**
     * Indicates whether {@code ch} is a low- (or trailing-) surrogate code unit
     * that is used for representing supplementary characters in UTF-16
     * encoding.
     *
     * @param ch
     *            the character to test.
     * @return {@code true} if {@code ch} is a low-surrogate code unit;
     *         {@code false} otherwise.
     * @see #isHighSurrogate(char)
     * @since 1.5
     */
    public static boolean isLowSurrogate(char ch) {
        return (MIN_LOW_SURROGATE <= ch && MAX_LOW_SURROGATE >= ch);
    }

    /**
     * Indicates whether the specified character pair is a valid surrogate pair.
     *
     * @param high
     *            the high surrogate unit to test.
     * @param low
     *            the low surrogate unit to test.
     * @return {@code true} if {@code high} is a high-surrogate code unit and
     *         {@code low} is a low-surrogate code unit; {@code false}
     *         otherwise.
     * @see #isHighSurrogate(char)
     * @see #isLowSurrogate(char)
     * @since 1.5
     */
    public static boolean isSurrogatePair(char high, char low) {
        return (isHighSurrogate(high) && isLowSurrogate(low));
    }

    /**
     * Calculates the number of {@code char} values required to represent the
     * specified Unicode code point. This method checks if the {@code codePoint}
     * is greater than or equal to {@code 0x10000}, in which case {@code 2} is
     * returned, otherwise {@code 1}. To test if the code point is valid, use
     * the {@link #isValidCodePoint(int)} method.
     *
     * @param codePoint
     *            the code point for which to calculate the number of required
     *            chars.
     * @return {@code 2} if {@code codePoint >= 0x10000}; {@code 1} otherwise.
     * @see #isValidCodePoint(int)
     * @see #isSupplementaryCodePoint(int)
     * @since 1.5
     */
    public static int charCount(int codePoint) {
        return (codePoint >= 0x10000 ? 2 : 1);
    }

    /**
     * Converts a surrogate pair into a Unicode code point. This method assumes
     * that the pair are valid surrogates. If the pair are <i>not</i> valid
     * surrogates, then the result is indeterminate. The
     * {@link #isSurrogatePair(char, char)} method should be used prior to this
     * method to validate the pair.
     *
     * @param high
     *            the high surrogate unit.
     * @param low
     *            the low surrogate unit.
     * @return the Unicode code point corresponding to the surrogate unit pair.
     * @see #isSurrogatePair(char, char)
     * @since 1.5
     */
    public static int toCodePoint(char high, char low) {
        // See RFC 2781, Section 2.2
        // http://www.faqs.org/rfcs/rfc2781.html
        int h = (high & 0x3FF) << 10;
        int l = low & 0x3FF;
        return (h | l) + 0x10000;
    }

    /**
     * Returns the code point at {@code index} in the specified sequence of
     * character units. If the unit at {@code index} is a high-surrogate unit,
     * {@code index + 1} is less than the length of the sequence and the unit at
     * {@code index + 1} is a low-surrogate unit, then the supplementary code
     * point represented by the pair is returned; otherwise the {@code char}
     * value at {@code index} is returned.
     *
     * @param seq
     *            the source sequence of {@code char} units.
     * @param index
     *            the position in {@code seq} from which to retrieve the code
     *            point.
     * @return the Unicode code point or {@code char} value at {@code index} in
     *         {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index} is negative or greater than or equal to
     *             the length of {@code seq}.
     * @since 1.5
     */
    public static int codePointAt(CharSequence seq, int index) {
        if (seq == null) {
            throw new NullPointerException();
        }
        int len = seq.length();
        if (index < 0 || index >= len) {
            throw new StringIndexOutOfBoundsException(index);
        }

        char high = seq.charAt(index++);
        if (index >= len) {
            return high;
        }
        char low = seq.charAt(index);
        if (isSurrogatePair(high, low)) {
            return toCodePoint(high, low);
        }
        return high;
    }

    /**
     * Returns the code point at {@code index} in the specified array of
     * character units. If the unit at {@code index} is a high-surrogate unit,
     * {@code index + 1} is less than the length of the array and the unit at
     * {@code index + 1} is a low-surrogate unit, then the supplementary code
     * point represented by the pair is returned; otherwise the {@code char}
     * value at {@code index} is returned.
     *
     * @param seq
     *            the source array of {@code char} units.
     * @param index
     *            the position in {@code seq} from which to retrieve the code
     *            point.
     * @return the Unicode code point or {@code char} value at {@code index} in
     *         {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index} is negative or greater than or equal to
     *             the length of {@code seq}.
     * @since 1.5
     */
    public static int codePointAt(char[] seq, int index) {
        if (seq == null) {
            throw new NullPointerException();
        }
        int len = seq.length;
        if (index < 0 || index >= len) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        char high = seq[index++];
        if (index >= len) {
            return high;
        }
        char low = seq[index];
        if (isSurrogatePair(high, low)) {
            return toCodePoint(high, low);
        }
        return high;
    }

    /**
     * Returns the code point at {@code index} in the specified array of
     * character units, where {@code index} has to be less than {@code limit}.
     * If the unit at {@code index} is a high-surrogate unit, {@code index + 1}
     * is less than {@code limit} and the unit at {@code index + 1} is a
     * low-surrogate unit, then the supplementary code point represented by the
     * pair is returned; otherwise the {@code char} value at {@code index} is
     * returned.
     *
     * @param seq
     *            the source array of {@code char} units.
     * @param index
     *            the position in {@code seq} from which to get the code point.
     * @param limit
     *            the index after the last unit in {@code seq} that can be used.
     * @return the Unicode code point or {@code char} value at {@code index} in
     *         {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code index < 0}, {@code index >= limit},
     *             {@code limit < 0} or if {@code limit} is greater than the
     *             length of {@code seq}.
     * @since 1.5
     */
    public static int codePointAt(char[] seq, int index, int limit) {
        if (index < 0 || index >= limit || limit < 0 || limit > seq.length) {
            throw new ArrayIndexOutOfBoundsException();
        }       

        char high = seq[index++];
        if (index >= limit) {
            return high;
        }
        char low = seq[index];
        if (isSurrogatePair(high, low)) {
            return toCodePoint(high, low);
        }
        return high;
    }

    /**
     * Returns the code point that preceds {@code index} in the specified
     * sequence of character units. If the unit at {@code index - 1} is a
     * low-surrogate unit, {@code index - 2} is not negative and the unit at
     * {@code index - 2} is a high-surrogate unit, then the supplementary code
     * point represented by the pair is returned; otherwise the {@code char}
     * value at {@code index - 1} is returned.
     *
     * @param seq
     *            the source sequence of {@code char} units.
     * @param index
     *            the position in {@code seq} following the code
     *            point that should be returned.
     * @return the Unicode code point or {@code char} value before {@code index}
     *         in {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index} is less than 1 or greater than the
     *             length of {@code seq}.
     * @since 1.5
     */
    public static int codePointBefore(CharSequence seq, int index) {
        if (seq == null) {
            throw new NullPointerException();
        }
        int len = seq.length();
        if (index < 1 || index > len) {
            throw new StringIndexOutOfBoundsException(index);
        }

        char low = seq.charAt(--index);
        if (--index < 0) {
            return low;
        }
        char high = seq.charAt(index);
        if (isSurrogatePair(high, low)) {
            return toCodePoint(high, low);
        }
        return low;
    }

    /**
     * Returns the code point that preceds {@code index} in the specified
     * array of character units. If the unit at {@code index - 1} is a
     * low-surrogate unit, {@code index - 2} is not negative and the unit at
     * {@code index - 2} is a high-surrogate unit, then the supplementary code
     * point represented by the pair is returned; otherwise the {@code char}
     * value at {@code index - 1} is returned.
     *
     * @param seq
     *            the source array of {@code char} units.
     * @param index
     *            the position in {@code seq} following the code
     *            point that should be returned.
     * @return the Unicode code point or {@code char} value before {@code index}
     *         in {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index} is less than 1 or greater than the
     *             length of {@code seq}.
     * @since 1.5
     */
    public static int codePointBefore(char[] seq, int index) {
        if (seq == null) {
            throw new NullPointerException();
        }
        int len = seq.length;
        if (index < 1 || index > len) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        char low = seq[--index];
        if (--index < 0) {
            return low;
        }
        char high = seq[index];
        if (isSurrogatePair(high, low)) {
            return toCodePoint(high, low);
        }
        return low;
    }

    /**
     * Returns the code point that preceds the {@code index} in the specified
     * array of character units and is not less than {@code start}. If the unit
     * at {@code index - 1} is a low-surrogate unit, {@code index - 2} is not
     * less than {@code start} and the unit at {@code index - 2} is a
     * high-surrogate unit, then the supplementary code point represented by the
     * pair is returned; otherwise the {@code char} value at {@code index - 1}
     * is returned.
     *
     * @param seq
     *            the source array of {@code char} units.
     * @param index
     *            the position in {@code seq} following the code point that
     *            should be returned.
     * @param start
     *            the index of the first element in {@code seq}.
     * @return the Unicode code point or {@code char} value before {@code index}
     *         in {@code seq}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if the {@code index <= start}, {@code start < 0},
     *             {@code index} is greater than the length of {@code seq}, or
     *             if {@code start} is equal or greater than the length of
     *             {@code seq}.
     * @since 1.5
     */
    public static int codePointBefore(char[] seq, int index, int start) {
        if (seq == null) {
            throw new NullPointerException();
        }
        int len = seq.length;
        if (index <= start || index > len || start < 0 || start >= len) {
            throw new ArrayIndexOutOfBoundsException();
        }

        char low = seq[--index];
        if (--index < start) {
            return low;
        }
        char high = seq[index];
        if (isSurrogatePair(high, low)) {
            return toCodePoint(high, low);
        }
        return low;
    }

    /**
     * Converts the specified Unicode code point into a UTF-16 encoded sequence
     * and copies the value(s) into the char array {@code dst}, starting at
     * index {@code dstIndex}.
     *
     * @param codePoint
     *            the Unicode code point to encode.
     * @param dst
     *            the destination array to copy the encoded value into.
     * @param dstIndex
     *            the index in {@code dst} from where to start copying.
     * @return the number of {@code char} value units copied into {@code dst}.
     * @throws IllegalArgumentException
     *             if {@code codePoint} is not a valid Unicode code point.
     * @throws NullPointerException
     *             if {@code dst} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code dstIndex} is negative, greater than or equal to
     *             {@code dst.length} or equals {@code dst.length - 1} when
     *             {@code codePoint} is a
     *             {@link #isSupplementaryCodePoint(int) supplementary code point}.
     * @since 1.5
     */
    public static int toChars(int codePoint, char[] dst, int dstIndex) {
        if (!isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException();
        }
        if (dst == null) {
            throw new NullPointerException();
        }
        if (dstIndex < 0 || dstIndex >= dst.length) {
            throw new IndexOutOfBoundsException();
        }

        if (isSupplementaryCodePoint(codePoint)) {
            if (dstIndex == dst.length - 1) {
                throw new IndexOutOfBoundsException();
            }
            // See RFC 2781, Section 2.1
            // http://www.faqs.org/rfcs/rfc2781.html
            int cpPrime = codePoint - 0x10000;
            int high = 0xD800 | ((cpPrime >> 10) & 0x3FF);
            int low = 0xDC00 | (cpPrime & 0x3FF);
            dst[dstIndex] = (char) high;
            dst[dstIndex + 1] = (char) low;
            return 2;
        }

        dst[dstIndex] = (char) codePoint;
        return 1;
    }

    /**
     * Converts the specified Unicode code point into a UTF-16 encoded sequence
     * and returns it as a char array.
     * 
     * @param codePoint
     *            the Unicode code point to encode.
     * @return the UTF-16 encoded char sequence. If {@code codePoint} is a
     *         {@link #isSupplementaryCodePoint(int) supplementary code point},
     *         then the returned array contains two characters, otherwise it
     *         contains just one character.
     * @throws IllegalArgumentException
     *             if {@code codePoint} is not a valid Unicode code point.
     * @since 1.5
     */
    public static char[] toChars(int codePoint) {
        if (!isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException();
        }

        if (isSupplementaryCodePoint(codePoint)) {
            int cpPrime = codePoint - 0x10000;
            int high = 0xD800 | ((cpPrime >> 10) & 0x3FF);
            int low = 0xDC00 | (cpPrime & 0x3FF);
            return new char[] { (char) high, (char) low };
        }
        return new char[] { (char) codePoint };
    }

    /**
     * Counts the number of Unicode code points in the subsequence of the
     * specified character sequence, as delineated by {@code beginIndex} and
     * {@code endIndex}. Any surrogate values with missing pair values will be
     * counted as one code point.
     *
     * @param seq
     *            the {@code CharSequence} to look through.
     * @param beginIndex
     *            the inclusive index to begin counting at.
     * @param endIndex
     *            the exclusive index to stop counting at.
     * @return the number of Unicode code points.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code beginIndex < 0}, {@code beginIndex > endIndex} or
     *             if {@code endIndex} is greater than the length of {@code seq}.
     * @since 1.5
     */
    public static int codePointCount(CharSequence seq, int beginIndex,
            int endIndex) {
        if (seq == null) {
            throw new NullPointerException();
        }
        int len = seq.length();
        if (beginIndex < 0 || endIndex > len || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }

        int result = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            char c = seq.charAt(i);
            if (isHighSurrogate(c)) {
                if (++i < endIndex) {
                    c = seq.charAt(i);
                    if (!isLowSurrogate(c)) {
                        result++;
                    }
                }
            }
            result++;
        }
        return result;
    }

    /**
     * Counts the number of Unicode code points in the subsequence of the
     * specified char array, as delineated by {@code offset} and {@code count}.
     * Any surrogate values with missing pair values will be counted as one code
     * point.
     *
     * @param seq
     *            the char array to look through
     * @param offset
     *            the inclusive index to begin counting at.
     * @param count
     *            the number of {@code char} values to look through in
     *            {@code seq}.
     * @return the number of Unicode code points.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0}, {@code count < 0} or if
     *             {@code offset + count} is greater than the length of
     *             {@code seq}.
     * @since 1.5
     */
    public static int codePointCount(char[] seq, int offset, int count) {
        if (seq == null) {
            throw new NullPointerException();
        }
        int len = seq.length;
        int endIndex = offset + count;
        if (offset < 0 || count < 0 || endIndex > len) {
            throw new IndexOutOfBoundsException();
        }

        int result = 0;
        for (int i = offset; i < endIndex; i++) {
            char c = seq[i];
            if (isHighSurrogate(c)) {
                if (++i < endIndex) {
                    c = seq[i];
                    if (!isLowSurrogate(c)) {
                        result++;
                    }
                }
            }
            result++;
        }
        return result;
    }

    /**
     * Determines the index in the specified character sequence that is offset
     * {@code codePointOffset} code points from {@code index}.
     *
     * @param seq
     *            the character sequence to find the index in.
     * @param index
     *            the start index in {@code seq}.
     * @param codePointOffset
     *            the number of code points to look backwards or forwards; may
     *            be a negative or positive value.
     * @return the index in {@code seq} that is {@code codePointOffset} code
     *         points away from {@code index}.
     * @throws NullPointerException
     *             if {@code seq} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code index < 0}, {@code index} is greater than the
     *             length of {@code seq}, or if there are not enough values in
     *             {@code seq} to skip {@code codePointOffset} code points
     *             forwards or backwards (if {@code codePointOffset} is
     *             negative) from {@code index}.
     * @since 1.5
     */
    public static int offsetByCodePoints(CharSequence seq, int index,
            int codePointOffset) {
        if (seq == null) {
            throw new NullPointerException();
        }
        int len = seq.length();
        if (index < 0 || index > len) {
            throw new IndexOutOfBoundsException();
        }

        if (codePointOffset == 0) {
            return index;
        }

        if (codePointOffset > 0) {
            int codePoints = codePointOffset;
            int i = index;
            while (codePoints > 0) {
                codePoints--;
                if (i >= len) {
                    throw new IndexOutOfBoundsException();
                }
                if (isHighSurrogate(seq.charAt(i))) {
                    int next = i + 1;
                    if (next < len && isLowSurrogate(seq.charAt(next))) {
                        i++;
                    }
                }
                i++;
            }
            return i;
        }

        assert codePointOffset < 0;
        int codePoints = -codePointOffset;
        int i = index;
        while (codePoints > 0) {
            codePoints--;
            i--;
            if (i < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (isLowSurrogate(seq.charAt(i))) {
                int prev = i - 1;
                if (prev >= 0 && isHighSurrogate(seq.charAt(prev))) {
                    i--;
                }
            }
        }
        return i;
    }

    /**
     * Convenience method to determine the value of the specified character
     * {@code c} in the supplied radix. The value of {@code radix} must be
     * between MIN_RADIX and MAX_RADIX.
     * 
     * @param c
     *            the character to determine the value of.
     * @param radix
     *            the radix.
     * @return the value of {@code c} in {@code radix} if {@code radix} lies
     *         between {@link #MIN_RADIX} and {@link #MAX_RADIX}; -1 otherwise.
     */
    public static int digit(char c, int radix) {
        if (radix >= MIN_RADIX && radix <= MAX_RADIX) {
            if (c < 128) {
                // Optimized for ASCII
                int result = -1;
                if ('0' <= c && c <= '9') {
                    result = c - '0';
                } else if ('a' <= c && c <= 'z') {
                    result = c - ('a' - 10);
                } else if ('A' <= c && c <= 'Z') {
                    result = c - ('A' - 10);
                }
                return result < radix ? result : -1;
            }
            int result = indexOfChar(digitKeys, c);
            if (result >= 0 && c <= digitValues[result * 2]) {
                int value = (char) (c - digitValues[result * 2 + 1]);
                if (value >= radix) {
                    return -1;
                }
                return value;
            }
        }
        return -1;
    }

    /**
     * Compares this object with the specified object and indicates if they are
     * equal. In order to be equal, {@code object} must be an instance of
     * {@code Character} and have the same char value as this object.
     * 
     * @param object
     *            the object to compare this double with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Character}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        return (object instanceof Character)
                && (value == ((Character) object).value);
    }

    /**
     * Returns the character which represents the specified digit in the
     * specified radix. The {@code radix} must be between {@code MIN_RADIX} and
     * {@code MAX_RADIX} inclusive; {@code digit} must not be negative and
     * smaller than {@code radix}. If any of these conditions does not hold, 0
     * is returned.
     * 
     * @param digit
     *            the integer value.
     * @param radix
     *            the radix.
     * @return the character which represents the {@code digit} in the
     *         {@code radix}.
     */
    public static char forDigit(int digit, int radix) {
        if (MIN_RADIX <= radix && radix <= MAX_RADIX) {
            if (0 <= digit && digit < radix) {
                return (char) (digit < 10 ? digit + '0' : digit + 'a' - 10);
            }
        }
        return 0;
    }

    /**
     * Gets the numeric value of the specified Unicode character.
     * 
     * @param c
     *            the Unicode character to get the numeric value of.
     * @return a non-negative numeric integer value if a numeric value for
     *         {@code c} exists, -1 if there is no numeric value for {@code c},
     *         -2 if the numeric value can not be represented with an integer.
     */
    public static int getNumericValue(char c) {
        if (c < 128) {
            // Optimized for ASCII
            if (c >= '0' && c <= '9') {
                return c - '0';
            }
            if (c >= 'a' && c <= 'z') {
                return c - ('a' - 10);
            }
            if (c >= 'A' && c <= 'Z') {
                return c - ('A' - 10);
            }
            return -1;
        }
        int result = indexOfChar(numericKeys, c);
        if (result >= 0 && c <= numericValues[result * 2]) {
            char difference = numericValues[result * 2 + 1];
            if (difference == 0) {
                return -2;
            }
            // Value is always positive, must be negative value
            if (difference > c) {
                return c - (short) difference;
            }
            return c - difference;
        }
        return -1;
    }

    /**
     * Gets the general Unicode category of the specified character.
     * 
     * @param c
     *            the character to get the category of.
     * @return the Unicode category of {@code c}.
     */
    public static int getType(char c) {
      if(c < 1000) {
        return typeValuesCache[(int) c];
      }
      int result = indexOfChar(typeKeys, c);
      if (result >= 0) {
        int high = typeValues[result * 2];
        if (c <= high) {
          int code = typeValues[result * 2 + 1];
          if (code < 0x100) {
            return code;
          }
          return (c & 1) == 1 ? code >> 8 : code & 0xff;
        }
      }
      return UNASSIGNED;
    }

    /**
     * Gets the Unicode directionality of the specified character.
     * 
     * @param c
     *            the character to get the directionality of.
     * @return the Unicode directionality of {@code c}.
     */
    public static byte getDirectionality(char c) {
      int result = indexOfChar(bidiKeys, c);
      if (result >= 0) {
        int high = bidiValues[result * 2];
        if (c <= high) {
          int code = bidiValues[result * 2 + 1];
          if (code < 0x100) {
            return (byte) (code - 1);
          }
          return (byte) (((c & 1) == 1 ? code >> 8 : code & 0xff) - 1);
        }
      }
      return DIRECTIONALITY_UNDEFINED;
    }

    /**
     * Indicates whether the specified character is mirrored.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is mirrored; {@code false}
     *         otherwise.
     */
    public static boolean isMirrored(char c) {
        int value = c / 16;
        if (value >= mirrored.length) {
            return false;
        }
        int bit = 1 << (c % 16);
        return (mirrored[value] & bit) != 0;
    }

    @Override
    public int hashCode() {
        return value;
    }

    /**
     * Indicates whether the specified character is defined in the Unicode
     * specification.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if the general Unicode category of the character is
     *         not {@code UNASSIGNED}; {@code false} otherwise.
     */
    public static boolean isDefined(char c) {
        return getType(c) != UNASSIGNED;
    }

    /**
     * Indicates whether the specified character is a digit.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a digit; {@code false}
     *         otherwise.
     */
    public static boolean isDigit(char c) {
        // Optimized case for ASCII
        if ('0' <= c && c <= '9') {
            return true;
        }
        if (c < 1632) {
            return false;
        }
        return getType(c) == DECIMAL_DIGIT_NUMBER;
    }

    /**
     * Indicates whether the specified character is ignorable in a Java or
     * Unicode identifier.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is ignorable; {@code false} otherwise.
     */
    public static boolean isIdentifierIgnorable(char c) {
        return (c <= 8) || (c >= 0xe && c <= 0x1b)
                || (c >= 0x7f && c <= 0x9f) || getType(c) == FORMAT;
    }

    /**
     * Indicates whether the specified character is an ISO control character.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is an ISO control character;
     *         {@code false} otherwise.
     */
    public static boolean isISOControl(char c) {
        return isISOControl((int)c);
    }

    /**
     * Indicates whether the specified code point is an ISO control character.
     * 
     * @param c
     *            the code point to check.
     * @return {@code true} if {@code c} is an ISO control character;
     *         {@code false} otherwise.
     */
    public static boolean isISOControl(int c) {
        return (c <= 0x1f) || (c >= 0x7f && c <= 0x9f);
    }

    /**
     * Indicates whether the specified character is a valid part of a Java
     * identifier other than the first character.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is valid as part of a Java identifier;
     *         {@code false} otherwise.
     */
    public static boolean isJavaIdentifierPart(char c) {
        // Optimized case for ASCII
        if (c < 128) {
            return (typeTags[c] & ISJAVAPART) != 0;
        }

        int type = getType(c);
        return (type >= UPPERCASE_LETTER && type <= OTHER_LETTER)
                || type == CURRENCY_SYMBOL || type == CONNECTOR_PUNCTUATION
                || (type >= DECIMAL_DIGIT_NUMBER && type <= LETTER_NUMBER)
                || type == NON_SPACING_MARK || type == COMBINING_SPACING_MARK
                || (c >= 0x80 && c <= 0x9f) || type == FORMAT;
    }

    /**
     * Indicates whether the specified character is a valid first character for
     * a Java identifier.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a valid first character of a Java
     *         identifier; {@code false} otherwise.
     */
    public static boolean isJavaIdentifierStart(char c) {
        // Optimized case for ASCII
        if (c < 128) {
            return (typeTags[c] & ISJAVASTART) != 0;
        }

        int type = getType(c);
        return (type >= UPPERCASE_LETTER && type <= OTHER_LETTER)
                || type == CURRENCY_SYMBOL || type == CONNECTOR_PUNCTUATION
                || type == LETTER_NUMBER;
    }

    /**
     * Indicates whether the specified character is a Java letter.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a Java letter; {@code false}
     *         otherwise.
     * @deprecated Use {@link #isJavaIdentifierStart(char)}
     */
    @Deprecated
    public static boolean isJavaLetter(char c) {
        return isJavaIdentifierStart(c);
    }

    /**
     * Indicates whether the specified character is a Java letter or digit
     * character.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a Java letter or digit;
     *         {@code false} otherwise.
     * @deprecated Use {@link #isJavaIdentifierPart(char)}
     */
    @Deprecated
    public static boolean isJavaLetterOrDigit(char c) {
        return isJavaIdentifierPart(c);
    }

    /**
     * Indicates whether the specified character is a letter.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a letter; {@code false} otherwise.
     */
    public static boolean isLetter(char c) {
        if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
            return true;
        }
        if (c < 128) {
            return false;
        }
        int type = getType(c);
        return type >= UPPERCASE_LETTER && type <= OTHER_LETTER;
    }

    /**
     * Indicates whether the specified character is a letter or a digit.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a letter or a digit; {@code false}
     *         otherwise.
     */
    public static boolean isLetterOrDigit(char c) {
        int type = getType(c);
        return (type >= UPPERCASE_LETTER && type <= OTHER_LETTER)
                || type == DECIMAL_DIGIT_NUMBER;
    }

    /**
     * Indicates whether the specified character is a lower case letter.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a lower case letter; {@code false}
     *         otherwise.
     */
    public static boolean isLowerCase(char c) {
        // Optimized case for ASCII
        if ('a' <= c && c <= 'z') {
            return true;
        }
        if (c < 128) {
            return false;
        }

        return getType(c) == LOWERCASE_LETTER;
    }

    /**
     * Indicates whether the specified character is a Java space.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a Java space; {@code false}
     *         otherwise.
     * @deprecated Use {@link #isWhitespace(char)}
     */
    @Deprecated
    public static boolean isSpace(char c) {
        return c == '\n' || c == '\t' || c == '\f' || c == '\r' || c == ' ';
    }

    /**
     * Indicates whether the specified character is a Unicode space character.
     * That is, if it is a member of one of the Unicode categories Space
     * Separator, Line Separator, or Paragraph Separator.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a Unicode space character,
     *         {@code false} otherwise.
     */
    public static boolean isSpaceChar(char c) {
        if (c == 0x20 || c == 0xa0 || c == 0x1680) {
            return true;
        }
        if (c < 0x2000) {
            return false;
        }
        return c <= 0x200b || c == 0x2028 || c == 0x2029 || c == 0x202f
                || c == 0x3000;
    }

    /**
     * Indicates whether the specified character is a titlecase character.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a titlecase character, {@code false}
     *         otherwise.
     */
    public static boolean isTitleCase(char c) {
        if (c == '\u01c5' || c == '\u01c8' || c == '\u01cb' || c == '\u01f2') {
            return true;
        }
        if (c >= '\u1f88' && c <= '\u1ffc') {
            // 0x1f88 - 0x1f8f, 0x1f98 - 0x1f9f, 0x1fa8 - 0x1faf
            if (c > '\u1faf') {
                return c == '\u1fbc' || c == '\u1fcc' || c == '\u1ffc';
            }
            int last = c & 0xf;
            return last >= 8 && last <= 0xf;
        }
        return false;
    }

    /**
     * Indicates whether the specified character is valid as part of a Unicode
     * identifier other than the first character.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is valid as part of a Unicode
     *         identifier; {@code false} otherwise.
     */
    public static boolean isUnicodeIdentifierPart(char c) {
        int type = getType(c);
        return (type >= UPPERCASE_LETTER && type <= OTHER_LETTER)
                || type == CONNECTOR_PUNCTUATION
                || (type >= DECIMAL_DIGIT_NUMBER && type <= LETTER_NUMBER)
                || type == NON_SPACING_MARK || type == COMBINING_SPACING_MARK
                || isIdentifierIgnorable(c);
    }

    /**
     * Indicates whether the specified character is a valid initial character
     * for a Unicode identifier.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a valid first character for a
     *         Unicode identifier; {@code false} otherwise.
     */
    public static boolean isUnicodeIdentifierStart(char c) {
        int type = getType(c);
        return (type >= UPPERCASE_LETTER && type <= OTHER_LETTER)
                || type == LETTER_NUMBER;
    }
    
    /**
     * Indicates whether the specified character is an upper case letter.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if {@code c} is a upper case letter; {@code false}
     *         otherwise.
     */
    public static boolean isUpperCase(char c) {
        // Optimized case for ASCII
        if ('A' <= c && c <= 'Z') {
            return true;
        }
        if (c < 128) {
            return false;
        }

        return getType(c) == UPPERCASE_LETTER;
    }

    /**
     * Indicates whether the specified character is a whitespace character in
     * Java.
     * 
     * @param c
     *            the character to check.
     * @return {@code true} if the supplied {@code c} is a whitespace character
     *         in Java; {@code false} otherwise.
     */
    public static boolean isWhitespace(char c) {
        // Optimized case for ASCII
        if ((c >= 0x1c && c <= 0x20) || (c >= 0x9 && c <= 0xd)) {
            return true;
        }
        if (c == 0x1680) {
            return true;
        }
        if (c < 0x2000 || c == 0x2007) {
            return false;
        }
        return c <= 0x200b || c == 0x2028 || c == 0x2029 || c == 0x3000;
    }

    /**
     * Reverses the order of the first and second byte in the specified
     * character.
     *
     * @param c
     *            the character to reverse.
     * @return the character with reordered bytes.
     */
    public static char reverseBytes(char c) {
        return (char)((c<<8) | (c>>8));
    }

    /**
     * Returns the lower case equivalent for the specified character if the
     * character is an upper case letter. Otherwise, the specified character is
     * returned unchanged.
     * 
     * @param c
     *            the character
     * @return if {@code c} is an upper case character then its lower case
     *         counterpart, otherwise just {@code c}.
     */
    public static char toLowerCase(char c) {
        // Optimized case for ASCII
        if ('A' <= c && c <= 'Z') {
            return (char) (c + ('a' - 'A'));
        }
        if (c < 192) {// || c == 215 || (c > 222 && c < 256)) {
            return c;
        } 
        if (c<1000) {
            return (char)lowercaseValuesCache[c-192];
        }

        int result = indexOfChar(lowercaseKeys, c);
        if (result >= 0) {
            boolean by2 = false;
            int start = lowercaseKeys[result];
            int end = lowercaseValues[result * 2];
            if ((start & 0x8000) != (end & 0x8000)) {
                end ^= 0x8000;
                by2 = true;
            }
            if (c <= end) {
                if (by2 && (c & 1) != (start & 1)) {
                    return c;
                }
                int mapping = lowercaseValues[result * 2 + 1];
                return (char) (c + mapping);
            }
        }
        return c;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Converts the specified character to its string representation.
     * 
     * @param value
     *            the character to convert.
     * @return the character converted to a string.
     */
    public static String toString(char value) {
        return String.valueOf(value);
    }

    /**
     * Returns the title case equivalent for the specified character if it
     * exists. Otherwise, the specified character is returned unchanged.
     * 
     * @param c
     *            the character to convert.
     * @return the title case equivalent of {@code c} if it exists, otherwise
     *         {@code c}.
     */
    public static char toTitleCase(char c) {
        if (isTitleCase(c)) {
            return c;
        }
        int result = titlecaseKeys.indexOf(c);
        if (result >= 0) {
            return titlecaseValues[result];
        }
        return toUpperCase(c);
    }

    /**
     * Returns the upper case equivalent for the specified character if the
     * character is a lower case letter. Otherwise, the specified character is
     * returned unchanged.
     * 
     * @param c
     *            the character to convert.
     * @return if {@code c} is a lower case character then its upper case
     *         counterpart, otherwise just {@code c}.
     */
    public static char toUpperCase(char c) {
        // Optimized case for ASCII
        if ('a' <= c && c <= 'z') {
            return (char) (c - ('a' - 'A'));
        }
        if (c < 181) {
            return c;
        }
        if (c<1000) {
            return (char)uppercaseValuesCache[(int)c-181];
        }
        int result = indexOfChar(uppercaseKeys, c);
        if (result >= 0) {
            boolean by2 = false;
            int start = uppercaseKeys[result];
            int end = uppercaseValues[result * 2];
            if ((start & 0x8000) != (end & 0x8000)) {
                end ^= 0x8000;
                by2 = true;
            }
            if (c <= end) {
                if (by2 && (c & 1) != (start & 1)) {
                    return c;
                }
                int mapping = uppercaseValues[result * 2 + 1];
                return (char) (c + mapping);
            }
        }
        return c;
    }
    
    private static int indexOfChar(int[] table, char c) {
      for (int i = 0; i < table.length; i++) {
        if (table[i] == (int) c) {
          return i;
        }
      }
      return -1;
    }

    // Not implemented
    // public static int offsetByCodePoints(char[] seq, int start, int count,
    //                                     int index, int codePointOffset) {
    // public static int digit(int codePoint, int radix)
    // public static int getNumericValue(int codePoint)
    // public static int getType(int codePoint)
    // public static byte getDirectionality(int codePoint)
    // public static boolean isMirrored(int codePoint)
    // public static boolean isDefined(int codePoint)
    // public static boolean isJavaIdentifierPart(int codePoint)
    // public static boolean isJavaIdentifierStart(int codePoint)
    // public static boolean isLetter(int codePoint)
    // public static boolean isLetterOrDigit(int codePoint)
    // public static boolean isLowerCase(int codePoint)
    // public static boolean isSpaceChar(int codePoint)
    // public static boolean isTitleCase(int codePoint)
    // public static boolean isUnicodeIdentifierPart(int codePoint)
    // public static boolean isUnicodeIdentifierStart(int codePoint)
    // public static boolean isUpperCase(int codePoint)
    // public static boolean isWhitespace(int codePoint)
    // public static int toLowerCase(int codePoint)
    // public static int toTitleCase(int codePoint)
    // public static int toUpperCase(int codePoint)
}
