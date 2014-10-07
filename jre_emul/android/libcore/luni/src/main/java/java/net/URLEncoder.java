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

package java.net;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import libcore.net.UriCodec;

/**
 * This class is used to encode a string using the format required by
 * {@code application/x-www-form-urlencoded} MIME content type.
 *
 * <p>All characters except letters ('a'..'z', 'A'..'Z') and numbers ('0'..'9')
 * and characters '.', '-', '*', '_' are converted into their hexadecimal value
 * prepended by '%'. For example: '#' -> %23. In addition, spaces are
 * substituted by '+'.
 */
public class URLEncoder {
    private URLEncoder() {}

    static UriCodec ENCODER = new UriCodec() {
        @Override protected boolean isRetained(char c) {
            return " .-*_".indexOf(c) != -1;
        }
    };

    /**
     * Equivalent to {@code encode(s, "UTF-8")}.
     *
     * @deprecated Use {@link #encode(String, String)} instead.
     */
    @Deprecated
    public static String encode(String s) {
        return ENCODER.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * Encodes {@code s} using the {@link Charset} named by {@code charsetName}.
     */
    public static String encode(String s, String charsetName) throws UnsupportedEncodingException {
        return ENCODER.encode(s, Charset.forName(charsetName));
    }
}
