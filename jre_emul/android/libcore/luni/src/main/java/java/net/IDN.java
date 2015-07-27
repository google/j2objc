/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.net;

import libcore.icu.NativeIDN;

/**
 * Converts internationalized domain names between Unicode and the ASCII Compatible Encoding
 * (ACE) representation.
 *
 * <p>See <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a> for full details.
 *
 * @since 1.6
 */
public final class IDN {
    /**
     * When set, allows IDN to process unassigned unicode points.
     */
    public static final int ALLOW_UNASSIGNED = 1;

    /**
     * When set, ASCII strings are checked against
     * <a href="http://www.ietf.org/rfc/rfc1122.txt">RFC 1122</a> and
     * <a href="http://www.ietf.org/rfc/rfc1123.txt">RFC 1123</a>.
     */
    public static final int USE_STD3_ASCII_RULES = 2;

    private IDN() {
    }

    /**
     * Transform a Unicode String to ASCII Compatible Encoding String according
     * to the algorithm defined in <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>.
     *
     * <p>If the transformation fails (because the input is not a valid IDN), an
     * exception will be thrown.
     *
     * <p>This method can handle either an individual label or an entire domain name.
     * In the latter case, the separators are: U+002E (full stop), U+3002 (ideographic full stop),
     * U+FF0E (fullwidth full stop), and U+FF61 (halfwidth ideographic full stop).
     * All of these will become U+002E (full stop) in the result.
     *
     * @param input the Unicode name
     * @param flags 0, {@code ALLOW_UNASSIGNED}, {@code USE_STD3_ASCII_RULES},
     *         or {@code ALLOW_UNASSIGNED | USE_STD3_ASCII_RULES}
     * @return the ACE name
     * @throws IllegalArgumentException if {@code input} does not conform to <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>
     */
    public static String toASCII(String input, int flags) {
        return NativeIDN.toASCII(input, flags);
    }

    /**
     * Equivalent to {@code toASCII(input, 0)}.
     *
     * @param input the Unicode name
     * @return the ACE name
     * @throws IllegalArgumentException if {@code input} does not conform to <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>
     */
    public static String toASCII(String input) {
        return toASCII(input, 0);
    }

    /**
     * Translates a string from ASCII Compatible Encoding (ACE) to Unicode
     * according to the algorithm defined in <a href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>.
     *
     * <p>Unlike {@code toASCII}, this transformation cannot fail.
     *
     * <p>This method can handle either an individual label or an entire domain name.
     * In the latter case, the separators are: U+002E (full stop), U+3002 (ideographic full stop),
     * U+FF0E (fullwidth full stop), and U+FF61 (halfwidth ideographic full stop).
     *
     * @param input the ACE name
     * @return the Unicode name
     * @param flags 0, {@code ALLOW_UNASSIGNED}, {@code USE_STD3_ASCII_RULES},
     *         or {@code ALLOW_UNASSIGNED | USE_STD3_ASCII_RULES}
     */
    public static String toUnicode(String input, int flags) {
        return NativeIDN.toUnicode(input, flags);
    }

    /**
     * Equivalent to {@code toUnicode(input, 0)}.
     *
     * @param input the ACE name
     * @return the Unicode name
     */
    public static String toUnicode(String input) {
        return NativeIDN.toUnicode(input, 0);
    }
}
