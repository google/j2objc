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

package java.util;

import java.io.Serializable;

/**
 * An {@code IllegalFormatCodePointException} will be thrown if an invalid
 * Unicode code point (defined by {@link Character#isValidCodePoint(int)}) is
 * passed as a parameter to a Formatter.
 *
 * @see java.lang.RuntimeException
 */
public class IllegalFormatCodePointException extends IllegalFormatException
        implements Serializable {
    private static final long serialVersionUID = 19080630L;

    private final int c;

    /**
     * Constructs a new {@code IllegalFormatCodePointException} which is
     * specified by the invalid Unicode code point.
     *
     * @param c
     *           the invalid Unicode code point.
     */
    public IllegalFormatCodePointException(int c) {
        this.c = c;
    }

    /**
     * Returns the invalid Unicode code point.
     *
     * @return the invalid Unicode code point.
     */
    public int getCodePoint() {
        return c;
    }

    @Override
    public String getMessage() {
        return Integer.toHexString(c);
    }
}
